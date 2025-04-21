package com.example.elixir.challenge

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.elixir.R

class ChallengeStageListAdapter(
    private val context: Context,
    private var stageList: MutableList<StageItem>,
    private val currentStage: Int // 현재 진행 중인 스테이지
) : BaseAdapter() {

    override fun getCount(): Int {
        return stageList.count { it.stage <= currentStage }
    }

    override fun getItem(position: Int): StageItem {
        val filtered = stageList.filter { it.stage <= currentStage }
        return filtered[position]
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_challenge_list, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val item = getItem(position)

        holder.challengeStage.text = item.typeName
        holder.challengeMission.text = item.description
        holder.challengeIcon.setImageResource(R.drawable.ic_dietlog_lunch)

        // 이전 단계는 50% 투명도 처리
        view.alpha = if (item.stage < currentStage) 0.5f else 1.0f

        // 클리어된 아이템이면 투명하게 처리
        view.alpha = if (item.isCleared) 0.5f else 1.0f

        return view
    }

    private class ViewHolder(view: View) {
        val challengeIcon: ImageView = view.findViewById(R.id.challengeIcon)
        val challengeStage: TextView = view.findViewById(R.id.challenge_stage)
        val challengeMission: TextView = view.findViewById(R.id.challenge_mission)
    }
}

