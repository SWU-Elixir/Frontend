package com.example.elixir.recipe.ui.paging

import android.graphics.Canvas
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.recipe.ui.adapter.RecipeListAdapter

class StickyHeaderItemDecoration(
    private val adapter: RecipeListAdapter
) : RecyclerView.ItemDecoration() {

    private var stickyHeaderView: View? = null
    private var stickyHeaderPosition = RecyclerView.NO_POSITION
    private var stickyHeaderTop = 0f

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(canvas, parent, state)

        // 첫 번째로 보이는 아이템의 위치 확인
        val topChild = parent.getChildAt(0) ?: return
        val topChildPosition = parent.getChildAdapterPosition(topChild)

        if (topChildPosition == RecyclerView.NO_POSITION) return

        Log.d("StickyHeader", "Top child position: $topChildPosition")

        // SearchSpinnerHeader(position 1)가 스크롤되어 올라갔을 때만 sticky 표시
        val headerPos = getStickyHeaderPosition(topChildPosition)
        if (headerPos == RecyclerView.NO_POSITION) {
            stickyHeaderView = null
            return
        }

        // Sticky 헤더 뷰 생성 또는 갱신
        val currentHeader = getHeaderViewForItem(headerPos, parent) ?: return
        fixLayoutSize(parent, currentHeader)

        // 다음 헤더와의 충돌 처리
        val contactPoint = currentHeader.bottom
        val childInContact = getChildInContact(parent, contactPoint, headerPos)

        if (childInContact != null && isHeader(parent.getChildAdapterPosition(childInContact))) {
            moveHeader(canvas, currentHeader, childInContact)
        } else {
            drawHeader(canvas, currentHeader)
        }
    }

    private fun getStickyHeaderPosition(itemPosition: Int): Int {
        // position 1 (SearchSpinnerHeader)이 스크롤되어 보이지 않을 때 sticky 적용
        return if (itemPosition >= 2) 1 else RecyclerView.NO_POSITION
    }

    private fun isHeader(position: Int): Boolean {
        return position == 0 || position == 1 // RecommendHeader와 SearchSpinnerHeader
    }

    private fun getHeaderViewForItem(headerPosition: Int, parent: RecyclerView): View? {
        if (headerPosition != 1) return null // SearchSpinnerHeader만 sticky 적용

        try {
            val headerType = adapter.getItemViewType(headerPosition)

            // 기존 sticky 헤더가 있고 위치가 같다면 재사용
            if (stickyHeaderView != null && stickyHeaderPosition == headerPosition) {
                return stickyHeaderView
            }

            // 새로운 sticky 헤더 생성
            val headerHolder = adapter.onCreateViewHolder(parent, headerType)
            adapter.onBindViewHolder(headerHolder, headerPosition)

            stickyHeaderView = headerHolder.itemView
            stickyHeaderPosition = headerPosition

            return stickyHeaderView
        } catch (e: Exception) {
            Log.e("StickyHeader", "Error creating header view", e)
            return null
        }
    }

    private fun fixLayoutSize(parent: ViewGroup, view: View) {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        val childWidth = ViewGroup.getChildMeasureSpec(
            widthSpec,
            parent.paddingLeft + parent.paddingRight,
            view.layoutParams?.width ?: ViewGroup.LayoutParams.MATCH_PARENT
        )
        val childHeight = ViewGroup.getChildMeasureSpec(
            heightSpec,
            parent.paddingTop + parent.paddingBottom,
            view.layoutParams?.height ?: ViewGroup.LayoutParams.WRAP_CONTENT
        )

        view.measure(childWidth, childHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    private fun drawHeader(canvas: Canvas, header: View) {
        canvas.save()
        stickyHeaderTop = 0f
        canvas.translate(0f, stickyHeaderTop)
        header.draw(canvas)
        canvas.restore()
    }

    private fun moveHeader(canvas: Canvas, currentHeader: View, nextHeader: View) {
        canvas.save()
        val translationY = (nextHeader.top - currentHeader.height).toFloat()
        stickyHeaderTop = translationY
        canvas.translate(0f, translationY)
        currentHeader.draw(canvas)
        canvas.restore()
    }

    private fun getChildInContact(parent: RecyclerView, contactPoint: Int, currentHeaderPos: Int): View? {
        var childInContact: View? = null

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            val childPosition = parent.getChildAdapterPosition(child)

            if (childPosition == currentHeaderPos) continue

            var heightTolerance = 0
            if (isHeader(childPosition)) {
                heightTolerance = child.height - contactPoint
            }

            val childBottomPosition = if (child.top > 0) {
                child.bottom + heightTolerance
            } else {
                child.bottom
            }

            if (childBottomPosition > contactPoint && child.top <= contactPoint) {
                childInContact = child
                break
            }
        }

        return childInContact
    }

    // StickyHeader 영역의 터치 이벤트 확인
    fun isTouchOnStickyHeader(x: Float, y: Float): Boolean {
        val header = stickyHeaderView ?: return false
        val isInBounds = x >= 0 && x <= header.width &&
                y >= stickyHeaderTop && y <= stickyHeaderTop + header.height

        Log.d("StickyTouch", "Touch bounds check: x=$x, y=$y, headerTop=$stickyHeaderTop, headerHeight=${header.height}, inBounds=$isInBounds")
        return isInBounds
    }

    // StickyHeader의 터치 이벤트를 실제 뷰로 전달
    fun handleStickyHeaderTouch(recyclerView: RecyclerView, x: Float, y: Float): Boolean {
        val header = stickyHeaderView ?: return false

        Log.d("StickyTouch", "Handling sticky header touch")

        // 터치 좌표를 StickyHeader 뷰의 로컬 좌표로 변환
        val localX = x
        val localY = y - stickyHeaderTop

        // 실제 SearchSpinnerHeader 뷰 찾기
        val realHeaderChild = findRealHeaderView(recyclerView)
        if (realHeaderChild != null) {
            Log.d("StickyTouch", "Found real header view, dispatching touch")
            // 실제 뷰에 터치 이벤트 전달
            return dispatchTouchToRealView(realHeaderChild, localX, localY)
        } else {
            Log.d("StickyTouch", "Real header view not found")
        }

        return true // 이벤트를 소비했다고 표시
    }

    private fun findRealHeaderView(recyclerView: RecyclerView): View? {
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val position = recyclerView.getChildAdapterPosition(child)
            if (position == 1) { // SearchSpinnerHeader position
                return child
            }
        }
        return null
    }

    private fun findClickableChildAt(parent: View, x: Float, y: Float): Boolean {
        Log.d("StickyTouch", "Finding clickable child at ($x, $y) in ${parent::class.simpleName}")

        if (parent is ViewGroup) {
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val childX = x - child.left
                val childY = y - child.top

                Log.d("StickyTouch", "Checking child $i: ${child::class.simpleName}, bounds=(${child.left}, ${child.top}, ${child.right}, ${child.bottom})")

                if (childX >= 0 && childX <= child.width &&
                    childY >= 0 && childY <= child.height) {

                    Log.d("StickyTouch", "Touch within child bounds, isClickable=${child.isClickable}")

                    if (child.isClickable) {
                        // 클릭 이벤트 실행
                        Log.d("StickyTouch", "Performing click on ${child::class.simpleName}")
                        child.performClick()
                        return true
                    } else if (child is ViewGroup) {
                        // 재귀적으로 자식 뷰 검사
                        if (findClickableChildAt(child, childX, childY)) {
                            return true
                        }
                    }
                }
            }
        } else if (parent.isClickable) {
            Log.d("StickyTouch", "Parent view is clickable, performing click")
            parent.performClick()
            return true
        }

        Log.d("StickyTouch", "No clickable child found")
        return false
    }

    private fun findAndClickSpinner(view: View, x: Float, y: Float): Boolean {
        // 스피너 ID로 직접 찾기 (layout XML에서 사용한 ID)
        val spinnerType = view.findViewById<Spinner>(R.id.spinner_type)
        val spinnerDifficulty = view.findViewById<Spinner>(R.id.spinner_difficulty)
        val resetButton = view.findViewById<View>(R.id.resetButton)

        Log.d("StickyTouch", "Found spinners: type=$spinnerType, difficulty=$spinnerDifficulty, reset=$resetButton")

        // 각 뷰의 위치 확인하고 터치 좌표와 비교
        spinnerType?.let { spinner ->
            if (isTouchInView(spinner, x, y)) {
                Log.d("StickyTouch", "Clicking type spinner")
                spinner.performClick()
                return true
            }
        }

        spinnerDifficulty?.let { spinner ->
            if (isTouchInView(spinner, x, y)) {
                Log.d("StickyTouch", "Clicking difficulty spinner")
                spinner.performClick()
                return true
            }
        }

        resetButton?.let { button ->
            if (isTouchInView(button, x, y)) {
                Log.d("StickyTouch", "Clicking reset button")
                button.performClick()
                return true
            }
        }

        return false
    }

    private fun isTouchInView(view: View, x: Float, y: Float): Boolean {
        val left = view.left.toFloat()
        val top = view.top.toFloat()
        val right = view.right.toFloat()
        val bottom = view.bottom.toFloat()

        Log.d("StickyTouch", "View bounds: ($left, $top, $right, $bottom), touch: ($x, $y)")

        return x in left..right && y >= top && y <= bottom
    }

    // dispatchTouchToRealView 메서드 수정
    private fun dispatchTouchToRealView(view: View, x: Float, y: Float): Boolean {
        Log.d("StickyTouch", "Dispatching touch to real view at ($x, $y)")

        // 터치 좌표가 뷰 영역 내에 있는지 확인
        if (x < 0 || x > view.width || y < 0 || y > view.height) {
            Log.d("StickyTouch", "Touch outside view bounds")
            return false
        }

        // 직접 스피너 찾아서 클릭
        if (findAndClickSpinner(view, x, y)) {
            return true
        }

        // 기존 방식으로도 시도
        val result = findClickableChildAt(view, x, y)
        Log.d("StickyTouch", "Touch dispatch result: $result")
        return result
    }
}