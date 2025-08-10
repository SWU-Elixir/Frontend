package com.example.elixir.challenge.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.elixir.R
import com.example.elixir.challenge.data.StageItem
import com.example.elixir.databinding.ItemChallengeListBinding

class ChallengeStageListAdapter(
    private val context: Context,
    // 초기화 시 빈 리스트를 받도록 변경
    private var stageList: List<StageItem> = mutableListOf()
) : BaseAdapter() {

    // 어댑터가 내부적으로 관리할 필터링된 리스트
    private var filteredList: List<StageItem> = emptyList()

    override fun getCount(): Int {
        // 필터링된 리스트의 크기를 반환
        return filteredList.size
    }

    override fun getItem(position: Int): StageItem {
        // 필터링된 리스트에서 아이템을 가져옴
        return filteredList[position]
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: ItemChallengeListBinding
        val view: View

        if (convertView == null) {
            binding = ItemChallengeListBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            binding = convertView.tag as ItemChallengeListBinding
            view = convertView
        }

        val item = getItem(position)

        // 스테이지 번호와 미션 내용 설정
        binding.tvChallengeStage.text = context.getString(R.string.challenge_step, item.stepNumber)
        binding.tvChallengeMission.text = item.stepName ?: ""

        // 스테이지 타입에 따른 아이콘 설정
        val iconRes = when (item.stepName) {
            "하루 한 끼 재철 식재료를 포함한 식사 기록" -> R.drawable.ic_challenge_meal_record
            "점심 챙겨 먹기" -> R.drawable.ic_challenge_meal_time
            "아침 챙겨 먹기" -> R.drawable.ic_challenge_meal_time
            "재철 식재료를 활용한 레시피 작성" -> R.drawable.ic_challenge_recipe_upload
            else -> R.drawable.ic_challenge_other // 기본 아이콘
        }
        binding.imgChallengeIcon.setImageResource(iconRes)

        // 완료된 스테이지는 50% 투명도로 표시
        view.alpha = if (item.isComplete) 0.5f else 1.0f

        return view
    }

    // `updateData` 메서드를 수정하여 필터링 로직을 한 번만 수행하도록 변경
    fun updateData(newStageList: List<StageItem>, currentStage: Int) {
        this.stageList = newStageList
        // 전달받은 currentStage에 해당하는 아이템만 필터링하여 새로운 리스트를 만듭니다.
        // 역순으로 정렬하는 로직을 추가하여 최신 스테이지가 상단에 보이도록 합니다.
        filteredList = newStageList.filter { it.stepNumber <= currentStage }.reversed()
        notifyDataSetChanged()
    }
}