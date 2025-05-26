package com.example.elixir.challenge

import android.os.Bundle
import android.view.*
import android.widget.*
import java.math.BigInteger
import com.example.elixir.R
import androidx.fragment.app.Fragment
import com.example.elixir.databinding.FragmentChallengeBinding
import com.example.elixir.databinding.DialogChallengeCompletedBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class ChallengeFragment : Fragment() {

    private var _binding: FragmentChallengeBinding? = null
    private val binding get() = _binding!!

    private lateinit var listAdapter: ChallengeStageListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChallengeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바텀시트 설정
        BottomSheetBehavior.from(binding.bottomSheet).apply {
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}
                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }

        // 샘플 챌린지 데이터 구성
        val allChallenges = listOf(
            ChallengeItem(
                id = 1,
                name = "2월 봄맞이 챌린지",
                purpose = "겨울의 끝자락, 체력이 떨어지기 쉬운 2월엔 비타민과 항산화 영양소 보충이 필수입니다.\n" +
                        "\n" +
                        "봄동은 비타민 C가 풍부해 환절기 면역력 강화에 도움을 주고,\n" +
                        "딸기는 피부 건강에 좋은 항산화 성분과 비타민 C가 가득해 피부 컨디션 개선에 탁월하죠.\n" +
                        "또한, 시금치는 베타카로틴과 철분이 많아 세포 노화 방지와 빈혈 예방에 효과적이며,\n" +
                        "아몬드는 피부 탄력과 심혈관 건강을 동시에 챙길 수 있는 영양 간식이에요.\n" +
                        "\n" +
                        "2월엔 이처럼 영양 가득한 제철 재료와 함께\n" +
                        "속부터 건강해지는 식습관을 만들어보세요!",
                description = "1단계\n" +
                        "• 하루 한 끼 재철 식재료(봄동, 딸기, 시금치, 아몬드)를 포함한 식사 기록\n" +
                        "   - 식단 기록 시 챌린지 태그 선택 시 성공\n" +
                        "   - 봄동, 딸기, 시금치, 아몬드 중 하나만 포함해도 성공 인정\n" +
                        "• 점심 챙겨 먹기\n" +
                        "   - 점심 시간(11시~14시) 내 식단 기록 시 성공\n" +
                        "\n2단계\n" +
                        "• 하루 한 끼 재철 식재료(봄동, 딸기, 시금치, 아몬드)를 포함한 식사 기록 (1단계와 동일)\n" +
                        "• 아침 챙겨 먹기\n" +
                        "   - 아침 시간(6시~9시) 내 식단 기록 시 성공\n" +
                        "\n3단계\n" +
                        "• 하루 한 끼 재철 식재료(봄동, 딸기, 시금치, 아몬드)를 포함한 식사 기록 (1단계와 동일)\n" +
                        "• 하루 3끼 식단 기록\n" +
                        "   - 아침, 점심, 저녁 전부 기록 시 성공\n" +
                        "\n4단계\n" +
                        "• 재철 식재료(봄동, 딸기, 시금치, 아몬드)를 활용한 레시피 작성\n" +
                        "   - 레시피 작성 시 챌린지 태그 선택 시 성공\n" +
                        "   - 봄동, 딸기, 시금치, 아몬드 중 하나만 포함해도 성공 인정\n" +
                        "• 1달 동안 누적 60끼 식단 기록\n" +
                        "   - 3월 1달간 식단(아침, 점심, 저녁, 간식) 누적 60개 이상 시 성공\n",
                badgeTitle = "비타민 수호자",
                badgeUrl = R.drawable.png_badge,
                startDate = "2025-02-01",
                endDate = "2028-02-28",
                month = 2,
                year = 2025,
                stages = mutableListOf(
                    StageItem(BigInteger.valueOf(8), BigInteger.valueOf(1), 4, "재철 식재료를 활용한 레시피 작성", "Recipe_Upload", "2024-05-28", false),
                    StageItem(BigInteger.valueOf(7), BigInteger.valueOf(1), 4, "1달 동안 누적 60끼 식단 기록", "Other", "2024-05-27", false),
                    StageItem(BigInteger.valueOf(6), BigInteger.valueOf(1), 3, "하루 한 끼 재철 식재료를 포함한 식사 기록", "Meal_Record", "2024-05-20", true),
                    StageItem(BigInteger.valueOf(5), BigInteger.valueOf(1), 3, "하루 3끼 식단 기록", "Other", "2024-05-18", false),
                    StageItem(BigInteger.valueOf(4), BigInteger.valueOf(1), 2, "하루 한 끼 재철 식재료를 포함한 식사 기록", "Meal_Record", "2024-05-10", true),
                    StageItem(BigInteger.valueOf(3), BigInteger.valueOf(1), 2, "아침 챙겨 먹기", "Meal_Time", "2024-05-09", true),
                    StageItem(BigInteger.valueOf(2), BigInteger.valueOf(1), 1, "하루 한 끼 재철 식재료를 포함한 식사 기록", "Meal_Record", "2024-05-01", true),
                    StageItem(BigInteger.valueOf(1), BigInteger.valueOf(1), 1, "점심 챙겨 먹기", "Meal_Time", "2024-05-01", true)
                )
            ),
            ChallengeItem(
                id = 2,
                name = "3월 환절기 건강 챌린지",
                purpose = "꽃샘추위와 미세먼지로 유난히 몸이 예민해지는 3월,\n" +
                        "이럴 때일수록 면역력과 간 건강 관리가 중요합니다.\n" +
                        "\n" +
                        "달래는 알리신 성분이 풍부해 혈액순환을 돕고 피로 회복에 효과적이며,\n" +
                        "냉이는 아르기닌과 비타민이 풍부해 간 해독을 돕는 대표 봄나물이에요.\n" +
                        "또한 쑥은 항염 작용과 함께 장 건강과 소화를 돕는 식재료로, 미세먼지로 지친 몸에 안성맞춤이죠.\n" +
                        "\n" +
                        "이번 3월에는 매일 한 끼, 봄나물을 식단에 담아\n" +
                        "내 몸의 방어력을 끌어올리는 항산화 챌린지에 도전해보세요!",
                description = "1단계\n" +
                        "• 하루 한 끼 재철 식재료(달래, 냉이, 쑥)를 포함한 식사 기록\n" +
                        "   - 식단 기록 시 챌린지 태그 선택 시 성공\n" +
                        "   - 달래, 냉이, 쑥 중 하나만 포함해도 성공 인정\n" +
                        "• 점심 챙겨 먹기\n" +
                        "   - 점심 시간(11시~14시) 내 식단 기록 시 성공\n" +
                        "\n2단계\n" +
                        "• 하루 한 끼 재철 식재료(달래, 냉이, 쑥)를 포함한 식사 기록 (1단계와 동일)\n" +
                        "• 아침 챙겨 먹기\n" +
                        "   - 아침 시간(6시~9시) 내 식단 기록 시 성공\n" +
                        "\n3단계\n" +
                        "• 하루 한 끼 재철 식재료(달래, 냉이, 쑥)를 포함한 식사 기록 (1단계와 동일)\n" +
                        "• 하루 3끼 식단 기록\n" +
                        "   - 아침, 점심, 저녁 전부 기록 시 성공\n" +
                        "\n4단계\n" +
                        "• 재철 식재료(달래, 냉이, 쑥)를 활용한 레시피 작성\n" +
                        "   - 레시피 작성 시 챌린지 태그 선택 시 성공\n" +
                        "   - 달래, 냉이, 쑥 중 하나만 포함해도 성공 인정\n" +
                        "• 1달 동안 누적 60끼 식단 기록\n" +
                        "   - 3월 1달간 식단(아침, 점심, 저녁, 간식) 누적 60개 이상 시 성공\n",
                badgeTitle = "환절기 방어왕",
                badgeUrl = R.drawable.png_badge,
                startDate = "2025-03-01",
                endDate = "2025-03-31",
                month = 3,
                year = 2025,
                stages = mutableListOf(
                    StageItem(BigInteger.valueOf(8), BigInteger.valueOf(2), 4, "재철 식재료를 활용한 레시피 작성", "Recipe_Upload", "2024-05-28", true),
                    StageItem(BigInteger.valueOf(7), BigInteger.valueOf(2), 4, "1달 동안 누적 60끼 식단 기록", "Other", "2024-05-27", true),
                    StageItem(BigInteger.valueOf(6), BigInteger.valueOf(2), 3, "하루 한 끼 재철 식재료를 포함한 식사 기록", "Meal_Record", "2024-05-20", true),
                    StageItem(BigInteger.valueOf(5), BigInteger.valueOf(2), 3, "하루 3끼 식단 기록", "Other", "2024-05-18", true),
                    StageItem(BigInteger.valueOf(4), BigInteger.valueOf(2), 2, "하루 한 끼 재철 식재료를 포함한 식사 기록", "Meal_Record", "2024-05-10", true),
                    StageItem(BigInteger.valueOf(3), BigInteger.valueOf(2), 2, "아침 챙겨 먹기", "Meal_Time", "2024-05-09", true),
                    StageItem(BigInteger.valueOf(2), BigInteger.valueOf(2), 1, "하루 한 끼 재철 식재료를 포함한 식사 기록", "Meal_Record", "2024-05-01", true),
                    StageItem(BigInteger.valueOf(1), BigInteger.valueOf(2), 1, "점심 챙겨 먹기", "Meal_Time", "2024-05-01", true)
                )
            )
        )

        // Spinner에 챌린지 제목 목록 표시
        val challengeTitles = allChallenges.map { it.name }
        val spinnerAdapter = ArrayAdapter(requireContext(),
            R.layout.item_challenge_spinner, challengeTitles)
        spinnerAdapter.setDropDownViewResource(R.layout.item_challenge_spinner_dropdown)
        binding.challengeSpinner.adapter = spinnerAdapter

        // Spinner 선택 이벤트 처리
        binding.challengeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedChallenge = allChallenges[position]
                updateChallenge(selectedChallenge) // 챌린지 선택 시 화면 업데이트
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    // 챌린지에 맞게 리스트와 UI 갱신
    private fun updateChallenge(challenge: ChallengeItem) {
        val currentStage = calculateCurrentStage(challenge.stages) // 현재 스테이지 계산
        val visibleStages = challenge.stages.filter { it.stepNumber <= currentStage } // 해당 스테이지까지만 표시

        // 단계 리스트 뷰 어댑터 갱신
        listAdapter = ChallengeStageListAdapter(requireContext(), visibleStages.toMutableList(), currentStage)
        binding.challengeStageList.adapter = listAdapter

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
        binding.challengeTitleText.text = challenge.name
        binding.challengePeriodText.text = getString(R.string.challenge_period_format, challenge.startDate, challenge.endDate)
        binding.challengeSub1.text = getString(R.string.challenge_sub1_format, challenge.name)
        binding.challengeGoalText.text = challenge.purpose
        binding.challengeDescriptionText.text = challenge.description
        binding.challengeSub2.text = getString(R.string.challenge_sub2_format, challenge.name, challenge.badgeTitle)

        // 모든 스테이지 완료 시 다이얼로그 표시
        val maxStage = challenge.stages.maxOfOrNull { it.stepNumber } ?: 1
        if (currentStage > maxStage) {
            showCompletionDialog(challenge.badgeTitle)
        }
    }

    // 클리어된 단계 수에 따라 현재 스테이지 계산
    private fun calculateCurrentStage(stages: List<StageItem>): Int {
        val maxStage = stages.maxOfOrNull { it.stepNumber } ?: 1

        for (stage in 1..maxStage) {
            val stageItems = stages.filter { it.stepNumber == stage }
            if (stageItems.any { !it.isComplete }) return stage // 아직 완료되지 않은 스테이지 발견 시 해당 스테이지 반환
        }

        return maxStage + 1 // 모든 단계 완료 시 다음 스테이지로
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
