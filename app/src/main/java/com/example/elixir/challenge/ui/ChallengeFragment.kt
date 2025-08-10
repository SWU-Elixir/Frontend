package com.example.elixir.challenge.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import java.math.BigInteger
import com.example.elixir.R
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.elixir.databinding.FragmentChallengeBinding
import com.example.elixir.databinding.DialogChallengeCompletedBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.elixir.challenge.data.ChallengeDetailEntity
import com.example.elixir.challenge.data.StageItem
import com.example.elixir.challenge.network.ChallengeRepository
import com.example.elixir.challenge.network.ChallengeProgressData
import java.util.Calendar
import com.example.elixir.RetrofitClient
import com.example.elixir.network.AppDatabase
import kotlinx.coroutines.launch

class ChallengeFragment : Fragment() {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChallengeViewModel
    private lateinit var stageAdapter: ChallengeStageListAdapter
    private var lastSelectedChallengeId: Int? = null
    private var isInitialLoad = true

    companion object {
        private const val TAG = "ChallengeFragment"
    }

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

        // 어댑터 초기화 시 빈 리스트만 전달
        stageAdapter = ChallengeStageListAdapter(requireContext())
        binding.listStageChallenge.adapter = stageAdapter

        initializeViewModel()
        setupBottomSheet()
        observeViewModel()
    }

    private fun initializeViewModel() {
        try {
            val appDB = AppDatabase.getInstance(requireContext())
            val api = RetrofitClient.instanceChallengeApi
            val repository = ChallengeRepository(api, appDB.challengeDao())

            viewModel = ChallengeViewModel(repository)

            if (isInitialLoad) {
                loadInitialData()
                isInitialLoad = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "ViewModel 초기화 실패", e)
            handleError("데이터 초기화 중 오류 발생: ${e.message}")
        }
    }

    private fun loadInitialData() {
        try {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            Log.d(TAG, "현재 연도($currentYear)의 챌린지 목록 로드 시작")

            // 연도별 챌린지 목록만 먼저 로드
            viewModel.loadChallengesByYear(currentYear)
        } catch (e: Exception) {
            Log.e(TAG, "초기 데이터 로드 실패", e)
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
        lifecycleScope.launch {
            // 스피너 목록 관찰 유지
            viewModel.challenges.collect { challenges ->
                if (challenges.isNotEmpty()) {
                    val sortedChallenges = challenges.sortedByDescending { it.month }
                    updateSpinner(sortedChallenges)
                } else {
                    showEmptyState()
                }
            }
        }

        lifecycleScope.launch {
            // progressState 따로 보지 않고 selectedChallenge만 관찰
            viewModel.selectedChallenge.collect { challenge ->
                if (challenge != null) {
                    updateChallengeDetailUI(challenge) // 기본 정보 업데이트
                    updateChallengeUI(challenge)       // 진행도 기반 UI 업데이트
                }
            }
        }

        // 에러 상태 관찰
        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let { handleError(it) }
            }
        }

        // 로딩 상태 관찰
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                Log.d(TAG, "로딩 상태: $isLoading")
            }
        }
    }

    private fun showEmptyState() {
        binding.apply {

        }
    }


        private fun handleError(error: String) {
        when {
            error.contains("HTTP 500") || error.contains("서버 오류") -> {
                Toast.makeText(requireContext(), "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_LONG).show()
            }
            error.contains("인터넷 연결") -> {
                Toast.makeText(requireContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_LONG).show()
            }
            error.contains("NullPointerException") -> {
                Toast.makeText(requireContext(), "데이터 처리 중 오류가 발생했습니다. 앱을 재시작해주세요.", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.clearError()
    }



    private fun updateSpinner(challenges: List<ChallengeDetailEntity>) {
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
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long
                ) {
                    val selectedChallenge = challenges.getOrNull(position) ?: return
                    if (lastSelectedChallengeId != selectedChallenge.id) {
                        lastSelectedChallengeId = selectedChallenge.id
                        Log.d(TAG, "스피너 선택: ${selectedChallenge.name} (id: ${selectedChallenge.id})")

                        // 상세 + 진행도 한번에 로드하는 새로운 메서드 호출
                        viewModel.loadChallengeWithProgress(selectedChallenge.id)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    Log.d(TAG, "스피너에서 아무것도 선택되지 않음")
                }
            }
        }
    }

    // 챌린지 기본 정보 UI 업데이트 (진행도 제외)
    private fun updateChallengeDetailUI(challenge: ChallengeDetailEntity) {
        Log.d(TAG, "챌린지 기본 정보 UI 업데이트: ${challenge.name}")

        binding.apply {
            tvChallengeSub2.text = getString(R.string.challenge_sub1)
            tvTitleChallenge.text = challenge.name
            tvPeriodChallenge.text = challenge.period ?: ""
            tvChallengeSub1.text = getString(R.string.challenge_sub1_format, challenge.name)
            tvGoalChallenge.text = challenge.purpose ?: ""
            tvDescriptionChallenge.text = challenge.description ?: ""
            tvChallengeSub3.text = getString(R.string.challenge_sub2_format, challenge.name, challenge.achievementName ?: "")
        }
    }

    // 진행도 정보로 UI 업데이트
    private fun updateChallengeUI(challenge: ChallengeDetailEntity) {
        Log.d("ChallengeFragment", "UI 업데이트 시작: ${challenge.name} (id: ${challenge.id})")

        val currentStage = calculateCurrentStage(challenge)
        val stageGoals = createStageGoalsList(challenge)

        // 어댑터에 데이터와 현재 스테이지를 전달
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

        // 모든 스테이지 완료 시 completion 확인
        if (currentStage == 5) {
            Log.d(TAG, "모든 스테이지 클리어! 완료 정보 확인 시작...")
            checkFinalCompletion()
        }
    }

    // 클리어된 단계 수에 따라 현재 스테이지 계산
    private fun calculateCurrentStage(challenge: ChallengeDetailEntity): Int {
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

    private fun createStageGoalsList(challenge: ChallengeDetailEntity): MutableList<StageItem> {
        val stageGoals = mutableListOf<StageItem>()
        Log.d(TAG, "createStageGoalsList 시작 - 챌린지 ID: ${challenge.id}")

        // Stage 1 - 항상 추가
        stageGoals.add(
            StageItem(
                id = BigInteger.valueOf(1),
                challengeId = BigInteger.valueOf(challenge.id.toLong()),
                stepNumber = 1,
                stepName = challenge.step1Goal1Desc ?: "목표 1",
                progressDate = "",
                isComplete = challenge.step1Goal1Achieved
            )
        )
        stageGoals.add(
            StageItem(
                id = BigInteger.valueOf(2),
                challengeId = BigInteger.valueOf(challenge.id.toLong()),
                stepNumber = 1,
                stepName = challenge.step1Goal2Desc ?: "목표 2",
                progressDate = "",
                isComplete = challenge.step1Goal2Achieved
            )
        )
        Log.d(TAG, "Stage 1 추가 완료 - 목표1: ${challenge.step1Goal1Achieved}, 목표2: ${challenge.step1Goal2Achieved}")

        // Stage 2 - Active 상태 확인
        if (challenge.step2Goal1Active) {
            stageGoals.add(
                StageItem(
                    id = BigInteger.valueOf(3),
                    challengeId = BigInteger.valueOf(challenge.id.toLong()),
                    stepNumber = 2,
                    stepName = challenge.step2Goal1Desc ?: "목표 1",
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
                    stepName = challenge.step2Goal2Desc ?: "목표 2",
                    progressDate = "",
                    isComplete = challenge.step2Goal2Achieved
                )
            )
        }
        Log.d(TAG, "Stage 2 추가 완료 - active1: ${challenge.step2Goal1Active}, active2: ${challenge.step2Goal2Active}")
        Log.d(TAG, "Stage 2 달성 - 목표1: ${challenge.step2Goal1Achieved}, 목표2: ${challenge.step2Goal2Achieved}")

        // Stage 3 - Active 상태 확인
        if (challenge.step3Goal1Active) {
            stageGoals.add(
                StageItem(
                    id = BigInteger.valueOf(5),
                    challengeId = BigInteger.valueOf(challenge.id.toLong()),
                    stepNumber = 3,
                    stepName = challenge.step3Goal1Desc ?: "목표 1",
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
                    stepName = challenge.step3Goal2Desc ?: "목표 2",
                    progressDate = "",
                    isComplete = challenge.step3Goal2Achieved
                )
            )
        }
        Log.d(TAG, "Stage 3 추가 완료 - active1: ${challenge.step3Goal1Active}, active2: ${challenge.step3Goal2Active}")

        // Stage 4 - Active 상태 확인
        if (challenge.step4Goal1Active) {
            stageGoals.add(
                StageItem(
                    id = BigInteger.valueOf(7),
                    challengeId = BigInteger.valueOf(challenge.id.toLong()),
                    stepNumber = 4,
                    stepName = challenge.step4Goal1Desc ?: "목표 1",
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
                    stepName = challenge.step4Goal2Desc ?: "목표 2",
                    progressDate = "",
                    isComplete = challenge.step4Goal2Achieved
                )
            )
        }
        Log.d(TAG, "Stage 4 추가 완료 - active1: ${challenge.step4Goal1Active}, active2: ${challenge.step4Goal2Active}")

        Log.d(TAG, "최종 stageGoals 크기: ${stageGoals.size}")
        stageGoals.forEachIndexed { index, item ->
            Log.d(TAG, "stageGoals[$index]: stepNumber=${item.stepNumber}, name=${item.stepName}, complete=${item.isComplete}")
        }

        return stageGoals
    }

    // 최종 완료 여부 확인
    private fun checkFinalCompletion() {
        viewModel.loadChallengeCompletionForPopup { completed, achievementName, achievementImageUrl ->
            if (completed) {
                Log.d(TAG, "API 응답: 챌린지 최종 완료! 다이얼로그 표시: $achievementName")
                val challenge = viewModel.selectedChallenge.value
                if (challenge != null) {
                    showCompletionDialog(challenge, achievementName, achievementImageUrl)
                }
            } else {
                Log.d(TAG, "API 응답: 챌린지 최종 완료 아님. 다이얼로그 미표시.")
            }
        }
    }

    private fun showCompletionDialog(challenge: ChallengeDetailEntity, achievementName: String?, achievementImageUrl: String?) {
        val dialogBinding = DialogChallengeCompletedBinding.inflate(layoutInflater)

        Log.d(TAG, "챌린지 완료 다이얼로그 표시")

        dialogBinding.tvDialogTitle.text = achievementName ?: getString(R.string.challenge_completion_title)
        dialogBinding.tvDialogMessage.text = getString(R.string.challenge_completion_message, challenge.name)

        if (!achievementImageUrl.isNullOrEmpty()) {
            try {
                Glide.with(dialogBinding.root)
                    .load(achievementImageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.bg_badge_empty)
                    .error(R.drawable.bg_badge_empty)
                    .fitCenter()
                    .into(dialogBinding.imgDialog)
            } catch (e: Exception) {
                Log.e(TAG, "이미지 로딩 실패", e)
                dialogBinding.imgDialog.setImageResource(R.drawable.bg_badge_empty)
            }
        } else {
            dialogBinding.imgDialog.setImageResource(R.drawable.bg_badge_empty)
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