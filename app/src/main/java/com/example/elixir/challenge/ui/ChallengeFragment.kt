package com.example.elixir.challenge.ui

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import java.math.BigInteger
import com.example.elixir.R
import androidx.fragment.app.Fragment
import com.example.elixir.databinding.FragmentChallengeBinding
import com.example.elixir.databinding.DialogChallengeCompletedBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.elixir.challenge.viewmodel.ChallengeViewModel
import com.example.elixir.challenge.data.ChallengeEntity
import com.example.elixir.challenge.data.StageItem
import com.example.elixir.challenge.network.ChallengeDB
import com.example.elixir.challenge.network.ChallengeRepository
import java.util.Calendar
import com.example.elixir.RetrofitClient

class ChallengeFragment : Fragment() {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChallengeViewModel
    private lateinit var stageAdapter: ChallengeStageListAdapter
    private var lastSelectedChallengeId: Int? = null
    private var isInitialLoad = true  // 초기 로드 여부를 추적

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stageAdapter = ChallengeStageListAdapter(requireContext(), mutableListOf(), 1)
        binding.listStageChallenge.adapter = stageAdapter

        initializeViewModel()
        setupBottomSheet()
        observeViewModel()
    }

    private fun initializeViewModel() {
        try {
            val db = ChallengeDB.getInstance(requireContext())
            val api = RetrofitClient.instanceChallengeApi
            val repository = ChallengeRepository(api, db.challengeDao())

            viewModel = ChallengeViewModel(repository)

            if (isInitialLoad) {
                loadInitialData()
                isInitialLoad = false
            }
        } catch (e: Exception) {
            Log.e("ChallengeFragment", "ViewModel 초기화 실패", e)
            handleError("데이터 초기화 중 오류 발생: ${e.message}")
        }
    }

    private fun loadInitialData() {
        try {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            Log.d("ChallengeFragment", "현재 연도($currentYear)의 챌린지 목록 로드 시작")

            // 데이터 로딩 순서 보장
            viewModel.loadChallengesByYear(currentYear)
        } catch (e: Exception) {
            Log.e("ChallengeFragment", "초기 데이터 로드 실패", e)
            handleError("데이터 로드 실패: ${e.message}")
        }
    }

    private fun setupBottomSheet() {
        BottomSheetBehavior.from(binding.cvBottomSheet).apply {
            peekHeight = 130
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}
                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }
    }

    private fun observeViewModel() {
        // 챌린지 목록 관찰 (스피너용)
        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            if (challenges.isNotEmpty()) {
                Log.d("ChallengeFragment", "챌린지 목록 업데이트: ${challenges.size}개")
                val sortedChallenges = challenges.sortedByDescending { it.month }
                updateSpinner(sortedChallenges)

            } else {
                Log.d("ChallengeFragment", "챌린지 목록 없음")
                showEmptyState()
            }
        }

        // 선택된 챌린지 상세 정보 관찰
        viewModel.selectedChallenge.observe(viewLifecycleOwner) { challenge ->
            challenge?.let {
                // lastSelectedChallengeId를 업데이트하기 전에 currentStage 계산
                val currentStageForCheck = calculateCurrentStage(it)
                if (it.id != lastSelectedChallengeId || currentStageForCheck == 5) { // 중복 로드 방지 & 스테이지 5일 때도 업데이트
                    Log.d("ChallengeFragment", "선택된 챌린지 상세 정보 업데이트: ${it.name} (id: ${it.id})")
                    updateChallengeUI(it)
                    lastSelectedChallengeId = it.id
                }
            }
        }

        // 에러 처리
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("ChallengeFragment", "데이터 로드 실패: $it")
                handleError(it)
            }
        }
    }

    private fun showEmptyState() {
        binding.apply {
            listStageChallenge.adapter = null
            // TODO: Implement proper empty state UI
        }
    }

    private fun handleError(error: String) {
        when {
            error.contains("HTTP 500") -> {
                Toast.makeText(requireContext(), "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
            }
            error.contains("NullPointerException") -> {
                Toast.makeText(requireContext(), "데이터 처리 중 오류가 발생했습니다. 앱을 재시작해주세요.", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(requireContext(), "오류 발생: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateSpinner(challenges: List<ChallengeEntity>) {
        if (challenges.isEmpty()) {
            showEmptyState()
            return
        }

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.item_challenge_spinner,
            challenges.map { it.name }
        ).apply {
            setDropDownViewResource(R.layout.item_challenge_spinner_dropdown)
        }

        binding.spinnerChallenge.apply {
            adapter = spinnerAdapter
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedChallenge = challenges.getOrNull(position) ?: return
                    if (lastSelectedChallengeId != selectedChallenge.id) {
                        lastSelectedChallengeId = selectedChallenge.id
                        Log.d("ChallengeFragment", "스피너 선택: ${selectedChallenge.name} (id: ${selectedChallenge.id})")
                        viewModel.loadChallengeWithProgress(selectedChallenge.id)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    Log.d("ChallengeFragment", "스피너에서 아무것도 선택되지 않음")
                }
            }
        }
    }

    // 챌린지에 맞게 UI 갱신
    private fun updateChallengeUI(challenge: ChallengeEntity) {
        Log.d("ChallengeFragment", "UI 업데이트 시작: ${challenge.name} (id: ${challenge.id})")

        val currentStage = calculateCurrentStage(challenge)
        val stageGoals = createStageGoalsList(challenge)

        stageAdapter.updateData(stageGoals, currentStage)

        // 스테이지에 따라 대표 이미지 변경
        val imageRes = when (currentStage) {
            1 -> R.drawable.png_challenge_1
            2 -> R.drawable.png_challenge_2
            3 -> R.drawable.png_challenge_3
            4 -> R.drawable.png_challenge_4
            else -> R.drawable.png_challenge_5
        }
        binding.ivChallenge.setImageResource(imageRes)

        // 챌린지 상세 정보 표시
        binding.apply {
            tvChallengeSub2.text = getString(R.string.challenge_sub1)
            tvTitleChallenge.text = challenge.name
            tvPeriodChallenge.text = challenge.period ?: ""
            tvChallengeSub1.text = getString(R.string.challenge_sub1_format, challenge.name)
            tvGoalChallenge.text = challenge.purpose ?: ""
            tvDescriptionChallenge.text = challenge.description ?: ""
            tvChallengeSub3.text = getString(R.string.challenge_sub2_format, challenge.name, challenge.achievementName ?: "")
        }

        if (currentStage == 5) {
            Log.d("ChallengeFragment", "모든 스테이지 클리어! 최종 완료 여부 API 호출 시작...")
            viewModel.loadChallengeCompletionForPopup { completed, achievementName, achievementImageUrl ->
                if (completed) {
                    Log.d("ChallengeFragment", "API 응답: 챌린지 최종 완료! 다이얼로그 표시: ${achievementName}")
                    showCompletionDialog(challenge, achievementName, achievementImageUrl)
                } else {
                    Log.d("ChallengeFragment", "API 응답: 챌린지 최종 완료 아님. 다이얼로그 미표시.")
                }
            }
        } else {
            Log.d("ChallengeFragment", "챌린지 완료 다이얼로그 미표시. 조건 불만족: currentStage != 5")
            Log.d("ChallengeFragment", " - currentStage: $currentStage")
        }
    }

    // 클리어된 단계 수에 따라 현재 스테이지 계산
    private fun calculateCurrentStage(challenge: ChallengeEntity): Int {
        // 각 스테이지의 목표 달성 여부 확인
        val stage1Complete = challenge.step1Goal1Achieved && challenge.step1Goal2Achieved
        val stage2Complete = challenge.step2Goal1Achieved && challenge.step2Goal2Achieved
        val stage3Complete = challenge.step3Goal1Achieved && challenge.step3Goal2Achieved
        val stage4Complete = challenge.step4Goal1Achieved && challenge.step4Goal2Achieved

        return when {
            !stage1Complete -> 1
            !stage2Complete -> 2
            !stage3Complete -> 3
            !stage4Complete -> 4
            else -> 5 // 모든 스테이지 완료
        }
    }

    // ChallengeEntity에서 StageItem 목록 생성
    private fun createStageGoalsList(challenge: ChallengeEntity): MutableList<StageItem> {
        val stageGoals = mutableListOf<StageItem>()

        // Stage 1
        stageGoals.add(
            StageItem(
                id = BigInteger.valueOf(1),
                challengeId = BigInteger.valueOf(challenge.id.toLong()),
                stepNumber = 1,
                stepName = challenge.step1Goal1Desc,
                progressDate = "",
                isComplete = challenge.step1Goal1Achieved
            )
        )
        stageGoals.add(
            StageItem(
                id = BigInteger.valueOf(2),
                challengeId = BigInteger.valueOf(challenge.id.toLong()),
                stepNumber = 1,
                stepName = challenge.step1Goal2Desc ?: "",
                progressDate = "",
                isComplete = challenge.step1Goal2Achieved
            )
        )

        // Stage 2
        if (challenge.step2Goal1Active) {
            stageGoals.add(
                StageItem(
                    id = BigInteger.valueOf(3),
                    challengeId = BigInteger.valueOf(challenge.id.toLong()),
                    stepNumber = 2,
                    stepName = challenge.step2Goal1Desc ?: "",
                    progressDate = "",
                    isComplete = challenge.step2Goal1Achieved
                )
            )
        }
        if (challenge.step2Goal2Active) {
            stageGoals.add(
                StageItem(
                    id = BigInteger.valueOf(4),
                    challengeId = BigInteger.valueOf(challenge.id.toLong()),
                    stepNumber = 2,
                    stepName = challenge.step2Goal2Desc ?: "",
                    progressDate = "",
                    isComplete = challenge.step2Goal2Achieved
                )
            )
        }

        // Stage 3
        if (challenge.step3Goal1Active) {
            stageGoals.add(
                StageItem(
                    id = BigInteger.valueOf(5),
                    challengeId = BigInteger.valueOf(challenge.id.toLong()),
                    stepNumber = 3,
                    stepName = challenge.step3Goal1Desc ?: "",
                    progressDate = "",
                    isComplete = challenge.step3Goal1Achieved
                )
            )
        }
        if (challenge.step3Goal2Active) {
            stageGoals.add(
                StageItem(
                    id = BigInteger.valueOf(6),
                    challengeId = BigInteger.valueOf(challenge.id.toLong()),
                    stepNumber = 3,
                    stepName = challenge.step3Goal2Desc ?: "",
                    progressDate = "",
                    isComplete = challenge.step3Goal2Achieved
                )
            )
        }

        // Stage 4
        if (challenge.step4Goal1Active) {
            stageGoals.add(
                StageItem(
                    id = BigInteger.valueOf(7),
                    challengeId = BigInteger.valueOf(challenge.id.toLong()),
                    stepNumber = 4,
                    stepName = challenge.step4Goal1Desc ?: "",
                    progressDate = "",
                    isComplete = challenge.step4Goal1Achieved
                )
            )
        }
        if (challenge.step4Goal2Active) {
            stageGoals.add(
                StageItem(
                    id = BigInteger.valueOf(8),
                    challengeId = BigInteger.valueOf(challenge.id.toLong()),
                    stepNumber = 4,
                    stepName = challenge.step4Goal2Desc ?: "",
                    progressDate = "",
                    isComplete = challenge.step4Goal2Achieved
                )
            )
        }

        return stageGoals
    }

    private fun showCompletionDialog(challenge: ChallengeEntity, achievementName: String?, achievementImageUrl: String?) {
        val dialogBinding = DialogChallengeCompletedBinding.inflate(layoutInflater)

        Log.d("ChallengeFragment", "챌린지 완료 다이얼로그 표시")

        dialogBinding.tvDialogTitle.text = achievementName ?: getString(R.string.challenge_completion_title)
        dialogBinding.tvDialogMessage.text = getString(R.string.challenge_completion_message, challenge.name)
        if (!achievementImageUrl.isNullOrEmpty()) {
            try {
                // Glide를 사용하여 이미지 로드 (badgeImage 로드 로직과 유사하게)
                Glide.with(dialogBinding.root)
                    .load(achievementImageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_badge_empty) // 기본 이미지 (필요시 뱃지 관련 이미지로 변경)
                    .error(R.drawable.bg_badge_empty) // 에러 이미지 (필요시 뱃지 관련 이미지로 변경)
                    .fitCenter() // 다이얼로그 이미지에도 fitCenter 적용
                    .into(dialogBinding.imgDialog)
            } catch (e: Exception) {
                Log.e("ChallengeFragment", "이미지 URI 로딩 실패", e)
                dialogBinding.imgDialog.setImageResource(R.drawable.bg_badge_empty) // 실패 시 기본 이미지
            }
        } else {
            dialogBinding.imgDialog.setImageResource(R.drawable.bg_badge_empty) // URL이 없으면 기본 이미지
        }

        val alertDialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnDialog.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}