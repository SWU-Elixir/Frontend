package com.example.elixir.recipe.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.elixir.recipe.data.RecipeListItemData
import com.example.elixir.recipe.network.api.RecipeApi

class SearchPagingSource(
    private val api: RecipeApi,
    private val keyword: String,
    private val categoryType: String?,
    private val categorySlowAging: String?
) : PagingSource<Int, RecipeListItemData>() {

    // 사이즈에 맞게 데이터 불러오기
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RecipeListItemData> {
        try {
            val page = params.key ?: 1
            val response = api.searchRecipe(
                keyword = keyword,
                page = page,
                size = params.loadSize,
                categoryType = categoryType,
                categorySlowAging = categorySlowAging
            )
            if (!response.isSuccessful) {
                throw Exception("검색 API 호출 실패: ${response.code()}")
            }
            val recipes = response.body()?.data?.content ?: emptyList()
            return LoadResult.Page(
                data = recipes,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (recipes.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, RecipeListItemData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}