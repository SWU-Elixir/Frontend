package com.example.elixir.recipe.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.elixir.recipe.network.request.CommentEditRequest
import com.example.elixir.recipe.network.request.CommentRequest
import com.example.elixir.recipe.network.response.GetCommentResponse

// Room DB에 저장할 댓글 데이터 클래스
@Entity(tableName = "comment_table")
data class CommentEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val recipeId: Int,
    val content: String,
    val nickName: String? = null,
    val title: String? = null,
    val authorProfileUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

// 서버 업로드 시 CommentEntity를 CommentRequest로 변환하는 확장 함수
fun CommentEntity.toRequest(): CommentRequest {
    return CommentRequest(
        recipeId = this.recipeId,
        content = this.content
    )
}

fun CommentEntity.toEditRequest(): CommentEditRequest {
    return CommentEditRequest(
        commentId = this.id,
        recipeId = this.recipeId,
        content = this.content
    )
}

fun GetCommentResponse.toEntity(): CommentEntity? {
    val commentItem = this.data ?: return null
    return CommentEntity(
        id = commentItem.commentId,
        recipeId = commentItem.recipeId,
        content = commentItem.content,
        nickName = commentItem.nickName,
        title = commentItem.title,
        authorProfileUrl = commentItem.authorProfileUrl,
        createdAt = commentItem.createdAt,
        updatedAt = commentItem.updatedAt
    )
}