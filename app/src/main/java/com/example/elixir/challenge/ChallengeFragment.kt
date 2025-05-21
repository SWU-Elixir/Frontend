package com.example.elixir.challenge

import ChallengeViewModel
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
import java.util.Calendar
import com.example.elixir.signup.RetrofitClient

class ChallengeFragment : Fragment() {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChallengeViewModel
    private lateinit var stageAdapter: ChallengeStageListAdapter

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

        val db = ChallengeDB.getInstance(requireContext())
        val api = RetrofitClient.instanceChallengeApi
        val repository = ChallengeRepository(api, db.challengeDao())
        val service = ChallengeService(repository)
        viewModel = ChallengeViewModel(service)

        // 바텀시트 설정
        BottomSheetBehavior.from(binding.bottomSheet).apply {
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}
                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }

        // 데이터 관찰
        viewModel.challenges.observe(viewLifecycleOwner) { challenges ->
            if (challenges.isNotEmpty()) {
                Log.d("ChallengeFragment", "챌린지 데이터 업데이트: ${challenges.size}개")
                
                // 챌린지 목록이 로드된 경우 (연도별 조회)
                if (challenges.size > 1) {
                    Log.d("ChallengeFragment", "연도별 챌린지 목록 표시")
                    val challengeTitles = challenges.map { it.name }
                    val spinnerAdapter = ArrayAdapter(
                        requireContext(),
                        R.layout.item_challenge_spinner,
                        challengeTitles
                    )
                    spinnerAdapter.setDropDownViewResource(R.layout.item_challenge_spinner_dropdown)
                    binding.challengeSpinner.adapter = spinnerAdapter

                    // Spinner 선택 이벤트 처리
                    binding.challengeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                            val selectedChallenge = challenges[position]
                            Log.d("ChallengeFragment", "스피너 선택: ${selectedChallenge.name} (id: ${selectedChallenge.id})")
                            if (selectedChallenge.id > 0) {
                                viewModel.loadChallengesById(selectedChallenge.id)
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {}
                    }
                }
                // 챌린지 상세 정보가 로드된 경우 (ID로 조회)
                else {
                    Log.d("ChallengeFragment", "챌린지 상세 정보 표시")
                    updateChallenge(challenges[0])
                }
            }
        }

        // 에러 처리
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("ChallengeFragment", "데이터 로드 실패: $it")
                Toast.makeText(requireContext(), "데이터 로드 실패: $it", Toast.LENGTH_SHORT).show()
            }
        }

        // 초기 데이터 로드
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        Log.d("ChallengeFragment", "현재 연도($currentYear)의 챌린지 목록 로드 시작")
        viewModel.loadChallengesByYear(currentYear)
    }

    // 챌린지에 맞게 리스트와 UI 갱신
    private fun updateChallenge(challenge: ChallengeEntity) {
        Log.d("ChallengeFragment", "UI 업데이트 시작: ${challenge.name}")
        
        // 현재 스테이지 계산
        val currentStage = calculateCurrentStage(challenge)
        Log.d("ChallengeFragment", "현재 스테이지: $currentStage")

        // 스테이지 목표 목록 생성
        val stageGoals = createStageGoalsList(challenge)
        Log.d("ChallengeFragment", "스테이지 목표 생성 완료: ${stageGoals.size}개")

        // 단계 리스트 뷰 어댑터 갱신
        stageAdapter = ChallengeStageListAdapter(requireContext(), stageGoals, currentStage)
        binding.challengeStageList.apply {
            adapter = stageAdapter
        }

        // 스테이지에 따라 대표 이미지 변경
        val imageRes = when (currentStage) {
            1 -> R.drawable.png_challenge_1
            2 -> R.drawable.png_challenge_2
            3 -> R.drawable.png_challenge_3
            4 -> R.drawable.png_challenge_4
            else -> R.drawable.png_challenge_5
        }
        binding.challengeImage.setImageResource(imageRes)

        // 챌린지 상세 정보 표시
        binding.apply {
            challengeTitleText.text = challenge.name
            challengePeriodText.text = challenge.period ?: ""
            challengeSub1.text = getString(R.string.challenge_sub1_format, challenge.name)
            challengeGoalText.text = challenge.purpose ?: ""
            challengeDescriptionText.text = challenge.description ?: ""
            challengeSub2.text = getString(R.string.challenge_sub2_format, challenge.name, challenge.achievementName ?: "")
        }

        // 모든 스테이지 완료 시 다이얼로그 표시
        if (challenge.challengeCompleted) {
            Log.d("ChallengeFragment", "챌린지 완료 다이얼로그 표시")
            showCompletionDialog(challenge.achievementName ?: "")
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
        stageGoals.add(StageItem(
            id = BigInteger.valueOf(1),
            challengeId = BigInteger.valueOf(challenge.id.toLong()),
            stepNumber = 1,
            stepName = challenge.step1Goal1Desc,
            stepType = challenge.step1Goal1Type ?: "",
            progressDate = "",
            isComplete = challenge.step1Goal1Achieved
        ))
        stageGoals.add(StageItem(
            id = BigInteger.valueOf(2),
            challengeId = BigInteger.valueOf(challenge.id.toLong()),
            stepNumber = 1,
            stepName = challenge.step1Goal2Desc ?: "",
            stepType = challenge.step1Goal2Type ?: "",
            progressDate = "",
            isComplete = challenge.step1Goal2Achieved
        ))
        Log.d("ChallengeFragment", "Stage 1 goals: $stageGoals")

        // Stage 2
        if (challenge.step2Goal1Active) {
            stageGoals.add(StageItem(
                id = BigInteger.valueOf(3),
                challengeId = BigInteger.valueOf(challenge.id.toLong()),
                stepNumber = 2,
                stepName = challenge.step2Goal1Desc ?: "",
                stepType = challenge.step2Goal1Type ?: "",
                progressDate = "",
                isComplete = challenge.step2Goal1Achieved
            ))
        }
        if (challenge.step2Goal2Active) {
            stageGoals.add(StageItem(
                id = BigInteger.valueOf(4),
                challengeId = BigInteger.valueOf(challenge.id.toLong()),
                stepNumber = 2,
                stepName = challenge.step2Goal2Desc ?: "",
                stepType = challenge.step2Goal2Type ?: "",
                progressDate = "",
                isComplete = challenge.step2Goal2Achieved
            ))
        }

        // Stage 3
        if (challenge.step3Goal1Active) {
            stageGoals.add(StageItem(
                id = BigInteger.valueOf(5),
                challengeId = BigInteger.valueOf(challenge.id.toLong()),
                stepNumber = 3,
                stepName = challenge.step3Goal1Desc ?: "",
                stepType = challenge.step3Goal1Type ?: "",
                progressDate = "",
                isComplete = challenge.step3Goal1Achieved
            ))
        }
        if (challenge.step3Goal2Active) {
            stageGoals.add(StageItem(
                id = BigInteger.valueOf(6),
                challengeId = BigInteger.valueOf(challenge.id.toLong()),
                stepNumber = 3,
                stepName = challenge.step3Goal2Desc ?: "",
                stepType = challenge.step3Goal2Type ?: "",
                progressDate = "",
                isComplete = challenge.step3Goal2Achieved
            ))
        }

        // Stage 4
        if (challenge.step4Goal1Active) {
            stageGoals.add(StageItem(
                id = BigInteger.valueOf(7),
                challengeId = BigInteger.valueOf(challenge.id.toLong()),
                stepNumber = 4,
                stepName = challenge.step4Goal1Desc ?: "",
                stepType = challenge.step4Goal1Type ?: "",
                progressDate = "",
                isComplete = challenge.step4Goal1Achieved
            ))
        }
        if (challenge.step4Goal2Active) {
            stageGoals.add(StageItem(
                id = BigInteger.valueOf(8),
                challengeId = BigInteger.valueOf(challenge.id.toLong()),
                stepNumber = 4,
                stepName = challenge.step4Goal2Desc ?: "",
                stepType = challenge.step4Goal2Type ?: "",
                progressDate = "",
                isComplete = challenge.step4Goal2Achieved
            ))
        }

        return stageGoals
    }

    private fun showCompletionDialog(title: String) {
        val dialogBinding = DialogChallengeCompletedBinding.inflate(layoutInflater)

        dialogBinding.dialogTitle.text = getString(R.string.challenge_completion_title)
        dialogBinding.dialogMessage.text = getString(R.string.challenge_completion_message, title)
        dialogBinding.dialogImage.setImageResource(R.drawable.png_badge)

        val alertDialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.dialogButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
