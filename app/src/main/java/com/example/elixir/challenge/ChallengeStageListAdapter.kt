package com.example.elixir.challenge

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.elixir.R
import com.example.elixir.databinding.ItemChallengeListBinding

class ChallengeStageListAdapter(
    private val context: Context,
    private var stageList: MutableList<StageItem>,
    private val currentStage: Int
) : BaseAdapter() {

    override fun getCount(): Int {
        return stageList.count { it.stepNumber <= currentStage }
    }
    override fun getItem(position: Int): StageItem {
        val filtered = stageList.filter { it.stepNumber <= currentStage }
        return filtered[position]
    }
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: ItemChallengeListBinding
        val view: View

        // 뷰 재사용 로직
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
        binding.challengeStage.text = context.getString(R.string.challenge_step, item.stepNumber)
        binding.challengeMission.text = item.stepName ?: ""

        // 스테이지 타입에 따른 아이콘 설정
        val iconRes = when (item.stepType) {
            "식단기록" -> R.drawable.ic_challenge_meal_record
            "Meal_Time" -> R.drawable.ic_challenge_meal_time
            "Recipe_Upload" -> R.drawable.ic_challenge_recipe_upload
            "Other" -> R.drawable.ic_challenge_other
            else -> R.drawable.ic_challenge_other // 기본 아이콘
        }
        binding.challengeIcon.setImageResource(iconRes)

        // 완료된 스테이지는 50% 투명도로 표시
        view.alpha = if (item.isComplete) 0.5f else 1.0f

        return view
    }
}

