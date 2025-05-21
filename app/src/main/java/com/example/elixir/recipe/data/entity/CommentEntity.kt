package com.example.elixir.recipe.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.elixir.recipe.network.request.CommentRequest

// Room DB에 저장할 댓글 데이터 클래스
@Entity(tableName = "comment_table")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val recipeId: Int,
    val content: String
)

// 서버 업로드 시 CommentEntity를 CommentRequest로 변환하는 확장 함수
fun CommentEntity.toRequest(): CommentRequest {
    return CommentRequest(
        recipeId = this.recipeId,
        content = this.content
    )
}