package com.example.elixir.recipe

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.example.elixir.databinding.ItemRecipeListBinding
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
        val binding = ItemRecipeListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecipeViewHolder(binding)
    }

    override fun getItemCount(): Int = recipeList.size

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = recipeList[position]

        // 이미지 설정 (없을 경우 기본 이미지 사용)
        val pictureRes = item.imageUrl ?: R.drawable.ic_recipe_white
        holder.binding.recipePicture.setImageURI(Uri.parse("android.resource://${holder.itemView.context.packageName}/$pictureRes"))


        // 텍스트 정보 설정
        holder.binding.recipeNameText.text = item.title
        holder.binding.categorySlowAging.text = item.categorySlowAging
        holder.binding.categoryType.text = item.categoryType
        holder.binding.recipeLevel.text = item.difficulty
        holder.binding.heartCount.text = formatCount(item.likeCount)

        // 조리 시간 표시
        holder.binding.recipeTimeHour.text = if (item.timeHours == 0) "" else "${item.timeHours}시간"
        holder.binding.recipeTimeMin.text = "${item.timeMinutes}분"

        // 재료 리스트 Flexbox 설정
        holder.binding.ingredientList.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = RecipeIngredientAdapter(item.ingredients)
        }

        // 초기 버튼 상태 설정
        holder.binding.bookmarkButton.setBackgroundResource(
            if(item.isBookmarked) R.drawable.ic_recipe_bookmark_selected
            else R.drawable.ic_recipe_bookmark_normal
        )
        holder.binding.heartButton.setBackgroundResource(
            if(item.isLiked) R.drawable.ic_recipe_heart_selected
            else R.drawable.ic_recipe_heart_normal
        )

        // 북마크 버튼 클릭 이벤트
        holder.binding.bookmarkButton.setOnClickListener {
            item.isBookmarked = !item.isBookmarked
            holder.binding.bookmarkButton.setBackgroundResource(
                if(item.isBookmarked) R.drawable.ic_recipe_bookmark_selected
                else R.drawable.ic_recipe_bookmark_normal
            )
        }

        // 좋아요 버튼 클릭 이벤트
        holder.binding.heartButton.setOnClickListener {
            item.isLiked = !item.isLiked
            holder.binding.heartButton.setBackgroundResource(
                if(item.isLiked) R.drawable.ic_recipe_heart_selected
                else R.drawable.ic_recipe_heart_normal
            )
            if (!item.isLiked) {
                item.likeCount--
            } else {
                item.likeCount++
            }
            item.isLiked = !item.isLiked
            holder.binding.heartCount.text = formatCount(item.likeCount)

            onHeartClick(item)
        }

        // 버튼 클릭 리스너 연결
        holder.binding.bookmarkButton.setOnClickListener { onBookmarkClick(item) }

        // 전체 아이템 클릭 로그 출력
        holder.binding.root.setOnClickListener {
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
    private fun formatCount(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000     -> String.format("%.1fk", count / 1_000.0)
            else               -> count.toString()
        }.removeSuffix(".0") // 소수점 0 제거
    }

    /**
     * ViewHolder 클래스 - 아이템 뷰에 포함된 UI 요소들을 바인딩
     */
    class RecipeViewHolder(val binding: ItemRecipeListBinding) : RecyclerView.ViewHolder(binding.root)
}
