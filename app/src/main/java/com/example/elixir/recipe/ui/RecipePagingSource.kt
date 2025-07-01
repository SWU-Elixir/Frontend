package com.example.elixir.recipe.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.elixir.recipe.data.RecipeListItemData
import com.example.elixir.recipe.network.api.RecipeApi

class RecipePagingSource(private val api: RecipeApi, private val categoryType: String?,
                         private val categorySlowAging: String?): PagingSource<Int, RecipeListItemData>() {
    // 이전, 다음 페이지 넘기기
    override fun getRefreshKey(state: PagingState<Int, RecipeListItemData>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    // 사이즈에 맞게 데이터 불러오기
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RecipeListItemData> {
        try {
            val page = params.key ?: 1
            val response = api.getRecipe(
                page = page,
                size = params.loadSize,
                categoryType = categoryType,
                categorySlowAging = categorySlowAging
            )
            // 불러오는 걸 실패했다면 코드 출력
            if (!response.isSuccessful) {
                throw Exception("API 호출 실패: ${response.code()}")
            }
            // 데이터를 설정해놓은 사이즈에 맞게 페이지 호출
            val responseBody = response.body()
            val recipes = responseBody?.data?.content ?: emptyList()
            return LoadResult.Page(
                data = recipes,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (recipes.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}