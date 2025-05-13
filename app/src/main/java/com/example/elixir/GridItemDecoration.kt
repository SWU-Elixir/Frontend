package com.example.elixir

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val horizontalPadding: Int // 양쪽 패딩 값
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // item position
        val column = position % spanCount // item column

        // 가로 간격 계산 (전체 패딩을 제외한 간격)
        val totalSpacing = spacing * (spanCount - 1) + horizontalPadding * 2
        val itemSpacing = totalSpacing / spanCount

        outRect.left = column * itemSpacing / spanCount
        outRect.right = itemSpacing - (column + 1) * itemSpacing / spanCount

        // 세로 방향 공백 설정
        if (position >= spanCount) {
            outRect.top = spacing // 아이템 간 상단 간격
        } else {
            outRect.top = 0 // 첫 번째 행은 상단 공백 없음
        }
        outRect.bottom = 0 // 하단 공백 없음
    }
}