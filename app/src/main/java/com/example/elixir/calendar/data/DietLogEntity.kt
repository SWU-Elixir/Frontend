package com.example.elixir.calendar.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.elixir.calendar.DietLogDto
import org.threeten.bp.LocalDateTime

@Entity(tableName = "diet_table")
data class DietLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: String,
    val score: Int,
    val ingredientTagId: List<Int>,
    val time: LocalDateTime,
    val imagePath: String // 이미지 파일 경로 추가
)

fun DietLogEntity.toDto(): DietLogDto {
    return DietLogDto(
        name = this.name,
        type = this.type, // 필요에 따라 기본값 설정
        score = this.score,
        ingredientTagId = this.ingredientTagId,
        time = this.time.toString()
    )
}