package com.example.elixir.recipe.ui.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.elixir.recipe.data.SearchItemData
import com.example.elixir.recipe.network.api.RecipeApi

class SearchPagingSource(
    private val api: RecipeApi,
    private val keyword: String,
    private val categoryType: String?,
    private val categorySlowAging: String?
) : PagingSource<Int, SearchItemData>() {

    // 사이즈에 맞게 데이터 불러오기
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchItemData> {
        try {
            val page = params.key ?: 1
            val response = api.searchRecipe(
                keyword = keyword,
                page = page,
                size = params.loadSize,
                categoryType = categoryType,
                categorySlowAging = categorySlowAging
            )
            val responseBody = response.body()
            val recipes = responseBody?.data?.content ?: emptyList()

            // 여기서 타입을 명확히!
            val itemList: MutableList<SearchItemData> = recipes
                .map { SearchItemData.SearchItem(it) }
                .toMutableList()

            if (page == 1) {
                itemList.add(0, SearchItemData.SearchTextHeader)
                itemList.add(0, SearchItemData.SearchSpinnerHeader)
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

    override fun getRefreshKey(state: PagingState<Int, SearchItemData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}