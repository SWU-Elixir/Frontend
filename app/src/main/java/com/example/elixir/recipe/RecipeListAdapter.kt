package com.example.elixir.recipe

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.elixir.R
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent

class RecipeListAdapter(
    private val recipeList: List<RecipeItem>,
    private val onBookmarkClick: (RecipeItem) -> Unit,
    private val onHeartClick: (RecipeItem) -> Unit
) : RecyclerView.Adapter<RecipeListAdapter.RecipeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_list, parent, false)
        return RecipeViewHolder(view)
    }

    override fun getItemCount(): Int = recipeList.size

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val item = recipeList[position]

        holder.recipeImage.setImageResource(R.drawable.png_recipe_sample)
        holder.recipeTitle.text = item.recipeTitle

        holder.ingredientRecyclerView.apply {
            layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.FLEX_START
            }
            adapter = RecipeIngredientAdapter(item.recipeIngredients)
        }

        holder.categorySlowAging.text = item.categorySlowAging
        holder.categoryType.text = item.categoryType

        holder.bookmarkButton.setImageResource(
            if (item.isBookmarked) R.drawable.ic_recipe_bookmark_selected else R.drawable.ic_recipe_bookmark_normal
        )

        holder.heartButton.setImageResource(
            if (item.isLiked) R.drawable.ic_recipe_heart_selected else R.drawable.ic_recipe_heart_normal
        )
        holder.heartCount.text = item.likeCount.toString()
        holder.recipeLevel.text = item.difficulty
        holder.recipeTimeHour.text = if (item.timeHours == 0) "" else "${item.timeHours}시간"
        holder.recipeTimeMin.text = "${item.timeMinutes}분"

        holder.bookmarkButton.setOnClickListener { onBookmarkClick(item) }
        holder.heartButton.setOnClickListener { onHeartClick(item) }
    }

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

