package com.example.elixir.calendar.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

@Entity(tableName = "diet_table")
data class DietLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "score") val score: Int,
    @ColumnInfo(name = "ingredientTagId") val ingredientTagId: List<Int>,
    @ColumnInfo(name = "time") val time: LocalDateTime,
    @ColumnInfo(name = "image") val imageUrl: String
)

// 데이터 전송 객체(이미지 url 제외) 생성
fun DietLogEntity.toDto(): DietLogDto {
    return DietLogDto(
        name = this.name,
        type = this.type,
        score = this.score,
        ingredientTagId = this.ingredientTagId,
        time = this.time.toString()
    )
}

fun DietLogEntity.toMealDto(memberId: Int): MealDto {
    return MealDto(
        id = this.id,
        memberId = memberId,
        name = this.name,
        imageUrl = this.imageUrl,
        type = this.type,
        score = this.score,
        ingredientTagId = this.ingredientTagId,
        time = this.time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    )
}