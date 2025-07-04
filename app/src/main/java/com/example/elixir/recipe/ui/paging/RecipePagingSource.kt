package com.example.elixir.recipe.ui.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.elixir.recipe.data.RecipeItemData
import com.example.elixir.recipe.data.RecipeListItemData
import com.example.elixir.recipe.network.api.RecipeApi

class RecipePagingSource(
    private val api: RecipeApi,
    private val categoryType: String?,
    private val categorySlowAging: String?
) : PagingSource<Int, RecipeListItemData>() {

    override fun getRefreshKey(state: PagingState<Int, RecipeListItemData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RecipeListItemData> {
        try {
            val page = params.key ?: 1
            val response = api.getRecipe(
                page = page,
                size = params.loadSize,
                categoryType = categoryType,
                categorySlowAging = categorySlowAging
            )
            if (!response.isSuccessful) {
                throw Exception("API 호출 실패: ${response.code()}")
            }
            val responseBody = response.body()
            val recipes = responseBody?.data?.content ?: emptyList()

            // 여기서 타입을 명확히!
            val itemList: MutableList<RecipeListItemData> = recipes
                .map { RecipeListItemData.RecipeItem(it) }
                .toMutableList()

            if (page == 1) {
                itemList.add(0, RecipeListItemData.SearchSpinnerHeader)
                itemList.add(0, RecipeListItemData.RecommendHeader)
            }

            return LoadResult.Page(
                data = itemList,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (recipes.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

}
