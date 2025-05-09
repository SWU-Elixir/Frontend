package com.example.elixir.recipe

import android.os.Parcel
import android.os.Parcelable
import java.math.BigInteger

data class RecipeData(
    val id: BigInteger,
    val memberId: BigInteger,           // 레시피 작성자 ID
    val title: String,                  // 레시피 이름
    val imageUrl: String,               // 레시피 이미지 리소스 ID (nullable)
    val categorySlowAging: String,      // 항산화 강화, 혈당 조절, 염증 감소
    val categoryType: String,           // 한식, 양식, 중식, 일식, 디저트, 음료/차, 양념/소스/잼

    val difficulty: String,             // 쉬움, 보통, 어려움
    val timeHours: Int,                 // 조리 시간 (시간 단위)
    val timeMinutes: Int,               // 조리 시간 (분 단위)
    val ingredients: List<String>,      // 사용된 재료 목록
    val seasoning: List<String>,        // 사용된 양념 목록
    val recipeOrder: List<String>,      // 레시피 순서 (조리법)
    val tips: String,                   // 레시피 팁

    val createdAt: String,
    val updateAt: String,

    var isBookmarked: Boolean = false,   // 북마크 여부
    var isLiked: Boolean = false,        // 좋아요 클릭 여부
    var likeCount: Int
)