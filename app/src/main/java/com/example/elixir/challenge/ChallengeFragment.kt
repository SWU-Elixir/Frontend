package com.example.elixir.challenge

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.elixir.R
import com.google.android.material.bottomsheet.BottomSheetBehavior

// 챌린지 정보를 담는 데이터 클래스
data class ChallengeItem(
    val challengeId: Int,
    val challengeTitle: String,           // 챌린지 제목
    val challengePeriod: String,          // 챌린지 기간
    val challengeGoal: String,            // 챌린지 목적
    val challengeDescription: String,     // 챌린지 설명
    val challengeBadge: String,
    val stages: MutableList<StageItem>    // 챌린지에 속한 단계 리스트
)

// 각 단계 정보를 담는 데이터 클래스
data class StageItem(
    val stage: Int,                       // 단계 번호
    val typeNumber: Int,                  // 유형 번호
    val typeName: String,                 // 유형 이름
    val description: String,              // 유형 설명
    val isCleared: Boolean = false        // 클리어 여부 (기본값: false)
)

class ChallengeFragment : Fragment() {

    // UI 컴포넌트 선언
    private lateinit var eventListView: ListView
    private lateinit var listAdapter: ChallengeStageListAdapter
    private lateinit var challengeImage: ImageView
    private lateinit var challengeSpinner: Spinner

    // 챌린지 정보를 표시할 텍스트뷰
    private lateinit var challengeTitleText: TextView
    private lateinit var challengePeriodText: TextView
    private lateinit var challengeGoalText: TextView
    private lateinit var challengeDescriptionText: TextView

    // 전체 챌린지 목록
    private lateinit var allChallenges: List<ChallengeItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_challenge, container, false)

        // UI 요소 연결
        eventListView = view.findViewById(R.id.challengeStageList)
        challengeImage = view.findViewById(R.id.challengeImage)
        challengeSpinner = view.findViewById(R.id.challengeSpinner)

        challengeTitleText = view.findViewById(R.id.challengeTitleText)
        challengePeriodText = view.findViewById(R.id.challengePeriodText)
        challengeGoalText = view.findViewById(R.id.challengeGoalText)
        challengeDescriptionText = view.findViewById(R.id.challengeDescriptionText)

        // 바텀시트 설정
        val bottomSheet = view.findViewById<CardView>(R.id.bottomSheet)
        BottomSheetBehavior.from(bottomSheet).apply {
            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {}
                override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            })
        }

        // 샘플 챌린지 데이터 구성
        allChallenges = listOf(
            ChallengeItem(
                challengeId = 1,
                challengeTitle = "2월 챌린지",
                challengePeriod = "2024.05.01 ~ 2024.05.31",
                challengeGoal = "하루 2L 이상 물 마시기",
                challengeDescription = "수분 섭취를 통해 신진대사 촉진 및 건강 증진을 목표로 합니다.",
                challengeBadge = "봄나물 마스터",
                stages = mutableListOf(
                    StageItem(2, 1, "2단계 유형 1", "설명", true),
                    StageItem(2, 2, "2단계 유형 2", "설명", true),
                    StageItem(1, 1, "1단계 유형 1", "설명", true),
                    StageItem(1, 2, "1단계 유형 2", "설명", true)
                )
            ),
            ChallengeItem(
                challengeId = 2,
                challengeTitle = "3월 챌린지",
                challengePeriod = "2024.05.01 ~ 2024.05.31",
                challengeGoal = "하루 2L 이상 물 마시기",
                challengeDescription = "수분 섭취를 통해 신진대사 촉진 및 건강 증진을 목표로 합니다.",
                challengeBadge = "봄나물 마스터",
                stages = mutableListOf(
                    StageItem(3, 1, "3단계 유형 1", "설명", false),
                    StageItem(3, 2, "3단계 유형 2", "설명", false),
                    StageItem(2, 1, "2단계 유형 1", "설명", true),
                    StageItem(2, 2, "2단계 유형 2", "설명", true),
                    StageItem(1, 1, "1단계 유형 1", "설명", true),
                    StageItem(1, 2, "1단계 유형 2", "설명", true)
                )
            )
        )

        // Spinner에 챌린지 제목 목록 표시
        val challengeTitles = allChallenges.map { it.challengeTitle }
        val spinnerAdapter = ArrayAdapter(requireContext(),
            R.layout.item_challenge_spinner, challengeTitles)
        spinnerAdapter.setDropDownViewResource(R.layout.item_challenge_spinner_dropdown)
        challengeSpinner.adapter = spinnerAdapter

        // Spinner 선택 이벤트 처리
        challengeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedChallenge = allChallenges[position]
                updateChallenge(selectedChallenge) // 챌린지 선택 시 화면 업데이트
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        return view
    }

    // 챌린지에 맞게 리스트와 UI 갱신
    private fun updateChallenge(challenge: ChallengeItem) {
        val currentStage = calculateCurrentStage(challenge.stages) // 현재 스테이지 계산
        val visibleStages = challenge.stages.filter { it.stage <= currentStage } // 해당 스테이지까지만 표시

        // 단계 리스트 뷰 어댑터 갱신
        listAdapter = ChallengeStageListAdapter(requireContext(), visibleStages.toMutableList(), currentStage)
        eventListView.adapter = listAdapter

        // 스테이지에 따라 대표 이미지 변경
        val imageRes = when (currentStage) {
            1 -> R.drawable.png_challenge_1
            2 -> R.drawable.png_challenge_2
            3 -> R.drawable.png_challenge_3
            4 -> R.drawable.png_challenge_4
            else -> R.drawable.png_challenge_5
        }
        challengeImage.setImageResource(imageRes)

        // 챌린지 상세 정보 표시
        challengeTitleText.text = challenge.challengeTitle
        challengePeriodText.text = challenge.challengePeriod
        challengeGoalText.text = challenge.challengeGoal
        challengeDescriptionText.text = challenge.challengeDescription

        // 모든 스테이지 완료 시 다이얼로그 표시 (calculateCurrentStage 활용)
        val maxStage = challenge.stages.maxOfOrNull { it.stage } ?: 1
        if (currentStage > maxStage) {
            showCompletionDialog(challenge.challengeBadge)
        }
    }

    // 클리어된 단계 수에 따라 현재 스테이지 계산
    private fun calculateCurrentStage(stages: List<StageItem>): Int {
        val maxStage = stages.maxOfOrNull { it.stage } ?: 1

        for (stage in 1..maxStage) {
            val stageItems = stages.filter { it.stage == stage }
            if (stageItems.any { !it.isCleared }) return stage // 아직 완료되지 않은 스테이지 발견 시 해당 스테이지 반환
        }

        return maxStage + 1 // 모든 단계 완료 시 다음 스테이지로
    }

    private fun showCompletionDialog(title: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_challenge_completed, null)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val dialogMessage = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val dialogImage = dialogView.findViewById<ImageView>(R.id.dialogImage)
        val dialogButton = dialogView.findViewById<Button>(R.id.dialogButton)

        dialogTitle.text = "챌린지 완료!"
        dialogMessage.text = "'$title' 칭호 및 뱃지를 획득했습니다."
        dialogImage.setImageResource(R.drawable.img_badge) // 원하는 이미지로 교체 가능

        val alertDialog = android.app.AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialogButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

}
