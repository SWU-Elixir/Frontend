package com.example.elixir.recipe

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

/**
 * 레시피 리스트 화면에서 사용되는 RecyclerView 어댑터
 * recipeList: 레시피 목록 데이터
 * onBookmarkClick: 북마크 버튼 클릭 시 동작
 * onHeartClick: 좋아요(하트) 버튼 클릭 시 동작
 */
class RecipeListAdapter(
    private var recipeList: List<RecipeData>,
    private val onBookmarkClick: (RecipeData) -> Unit,
    private val onHeartClick: (RecipeData) -> Unit,
    private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_list, parent, false)
        return RecipeViewHolder(view)
    }

    override fun getItemCount(): Int = recipeList.size

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = recipeList[position]

        // 이미지 설정 (없을 경우 기본 이미지 사용)
        val pictureRes = item.imageUrl ?: R.drawable.ic_recipe_white
        holder.recipeImage.setImageURI(Uri.parse("android.resource://${holder.itemView.context.packageName}/$pictureRes"))

        // 텍스트 정보 설정
        holder.recipeTitle.text = item.title
        holder.categorySlowAging.text = item.categorySlowAging
        holder.categoryType.text = item.categoryType
        holder.recipeLevel.text = item.difficulty

        // 조리 시간 표시
        holder.recipeTimeHour.text = if (item.timeHours == 0) "" else "${item.timeHours}시간"
        holder.recipeTimeMin.text = "${item.timeMinutes}분"

        // 재료 리스트 Flexbox 설정
        holder.ingredientRecyclerView.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = RecipeIngredientAdapter(item.ingredients)
        }

        // 북마크/하트 아이콘 상태 설정
        holder.bookmarkButton.setImageResource(
            if (item.isBookmarked) R.drawable.ic_recipe_bookmark_selected else R.drawable.ic_recipe_bookmark_normal
        )
        holder.heartButton.setImageResource(
            if (item.isLiked) R.drawable.ic_recipe_heart_selected else R.drawable.ic_recipe_heart_normal
        )

        // 좋아요 수 포맷 변환 (예: 1.2k / 2M 등)
        val likeCountText = when {
            item.likeCount >= 1_000_000 -> formatCount(item.likeCount / 1_000_000.0, "M")
            item.likeCount >= 1_000     -> formatCount(item.likeCount / 1_000.0, "k")
            else                        -> item.likeCount.toString()
        }
        holder.heartCount.text = likeCountText

        // 버튼 클릭 리스너 연결
        holder.bookmarkButton.setOnClickListener { onBookmarkClick(item) }
        holder.heartButton.setOnClickListener { onHeartClick(item) }

        // 전체 아이템 클릭 로그 출력
        holder.itemView.setOnClickListener {
            Log.d("RecipeAdapter", "아이템 클릭됨: ${item.title}")

            // 레시피 상세 프래그먼트 생성 및 데이터 전달
            val detailFragment = RecipeDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("title", item.title)
                    putString("categorySlowAging", item.categorySlowAging)
                    putString("categoryType", item.categoryType)
                    putString("tip", item.tips)
                    putStringArrayList("ingredients", ArrayList(item.ingredients))
                    putStringArrayList("seasoning", ArrayList(item.seasoning))
                    //putInt("imageUrl", item.imageUrl ?: u),
                    putString("difficulty", item.difficulty)
                    putInt("hour", item.timeHours)
                    putInt("minute", item.timeMinutes)
                }
            }

            fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    /**
     * 외부에서 데이터 갱신 시 호출하는 함수
     */
    fun updateData(newList: List<RecipeData>) {
        recipeList = newList
        notifyDataSetChanged()
    }

    /**
     * 좋아요 수 표시를 포맷에 맞게 변환하는 함수
     * @param value: 숫자 값
     * @param suffix: 단위 (k, M 등)
     */
    private fun formatCount(value: Double, suffix: String): String {
        val formatted = if (value < 10) {
            String.format("%.1f", value) // e.g. 1.2k
        } else {
            value.toInt().toString()     // e.g. 15k
        }
        return formatted.removeSuffix(".0") + suffix
    }

    /**
     * ViewHolder 클래스 - 아이템 뷰에 포함된 UI 요소들을 바인딩
     */
    class RecipeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recipeImage: ImageView = view.findViewById(R.id.recipePicture)
        val recipeTitle: TextView = view.findViewById(R.id.recipeNameText)
        val categorySlowAging: TextView = view.findViewById(R.id.category_slow_aging)
        val categoryType: TextView = view.findViewById(R.id.category_type)
        val ingredientRecyclerView: RecyclerView = view.findViewById(R.id.ingredientList)
        val bookmarkButton: ImageButton = view.findViewById(R.id.bookmarkButton)
        val heartButton: ImageButton = view.findViewById(R.id.heartButton)
        val heartCount: TextView = view.findViewById(R.id.heartCount)
        val recipeLevel: TextView = view.findViewById(R.id.recipeLevel)
        val recipeTimeHour: TextView = view.findViewById(R.id.recipeTimeHour)
        val recipeTimeMin: TextView = view.findViewById(R.id.recipeTimeMin)
    }
}
