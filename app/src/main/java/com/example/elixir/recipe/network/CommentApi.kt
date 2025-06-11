package com.example.elixir.recipe.network

import com.example.elixir.recipe.network.request.CommentRequest
import com.example.elixir.network.GetStringResponse
import com.example.elixir.recipe.network.response.GetCommentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CommentApi {
    @POST("/api/recipe/{recipeId}/comment")
    suspend fun uploadComment(
        @Path("recipeId") recipeId: Int,
        @Body requestBody: CommentRequest
    ): Response<GetCommentResponse>

    @PUT("/api/recipe/{recipeId}/comment/{commentId}")
    suspend fun updateComment(
        @Path("recipeId") recipeId: Int,
        @Path("commentId") commentId: Int,
        @Body requestBody: CommentRequest
    ): Response<GetCommentResponse>

    @DELETE("/api/recipe/{recipeId}/comment/{commentId}")
    suspend fun deleteComment(
        @Path("recipeId") recipeId: Int,
        @Path("commentId") commentId: Int
    ): Response<GetStringResponse>
}