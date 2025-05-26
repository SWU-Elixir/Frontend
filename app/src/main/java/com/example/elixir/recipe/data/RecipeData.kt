package com.example.elixir.recipe.data

import org.threeten.bp.LocalDateTime


data class RecipeData(
    var email: String,                          // 작성자 이메일
    var title: String,                          // 레시피 제목
    var description: String,
    var categorySlowAging: String,
    var categoryType: String,
    var difficulty: String,
    var timeHours: Int,
    var timeMinutes: Int,
    var ingredientTagIds: List<Int>,            // 태그 목록
    var ingredients: Map<String, String>,       // 재료와 양념
    var seasoning: Map<String, String>,
    var stepDescriptions: List<String>,         // 조리 단계 설명
    var stepImageUrls: List<String>,            // 조리 단계 이미지 URL (nullable)
    var tips: String,
    var allergies: List<String>,                // 알레르기 정보
    var imageUrl: String,
    var authorFollowByCurrentUser: Boolean,     // 현재 사용자가 작성자를 팔로우하고 있는지 여부
    var likedByCurrentUser: Boolean,            // 현재 사용자가 좋아요를 눌렀는지 여부
    var scrappedByCurrentUser: Boolean,         // 현재 사용자가 스크랩을 눌렀는지 여부
    var authorNickname: String,                 // 작성자 닉네임
    var authorTitle: String,                    // 작성자 직책
    var likes: Int = 0,                         // 좋아요 수
    var scraps: Int = 0,                        // 스크랩 수
    var createdAt: LocalDateTime,
    var updatedAt: LocalDateTime
)