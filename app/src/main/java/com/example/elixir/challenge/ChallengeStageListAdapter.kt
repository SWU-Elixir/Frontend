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
    private val currentStage: Int // 현재 진행 중인 스테이지
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

        if (convertView == null) {
            binding = ItemChallengeListBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            binding = convertView.tag as ItemChallengeListBinding
            view = convertView
        }

        val item = getItem(position)

        binding.challengeStage.text = "${item.stepNumber}단계"
        binding.challengeMission.text = item.stepName

        val iconRes = when (item.stepType) {
            "Meal_Record" -> R.drawable.ic_challenge_meal_record
            "Meal_Time" -> R.drawable.ic_challenge_meal_time
            "Recipe_Upload" -> R.drawable.ic_challenge_recipe_upload
            "Other" -> R.drawable.ic_challenge_other
            else -> R.drawable.ic_challenge_other // 기본 아이콘
        }
        binding.challengeIcon.setImageResource(iconRes)

        // 이전 단계는 50% 투명도 처리
        view.alpha = if (item.stepNumber < currentStage) 0.5f else 1.0f

        // 클리어된 아이템이면 투명하게 처리
        view.alpha = if (item.isComplete) 0.5f else 1.0f

        return view
    }
}

