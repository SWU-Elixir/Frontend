package com.example.elixir.member.network

import android.util.Log
import androidx.room.Transaction
import com.example.elixir.member.data.MemberDao
import com.example.elixir.member.data.MemberEntity
import com.example.elixir.member.data.AchievementEntity
import com.example.elixir.member.data.FollowEntity
import com.example.elixir.member.data.ProfileEntity
import com.example.elixir.member.data.RecipeEntity
import com.example.elixir.signup.SignupRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import com.google.gson.Gson
import java.io.File

class MemberRepository (
    private val api: MemberApi,
    private val dao: MemberDao
) {


    suspend fun follow(targetMemberId: Int): Boolean {
        return try {
            val response = api.follow(targetMemberId)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun unfollow(targetMemberId: Int): Boolean {
        return try {
            val response = api.unfollow(targetMemberId)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    @Transaction
    suspend fun fetchAndSaveMember(): MemberEntity? {
        return try {
            val response = api.getMember()
            if (response.status == 200) {
                dao.insertMember(response.data)
                response.data
            } else {
                Log.e("MemberRepository", "API 호출 실패: ${response.message}")
                getMemberFromDb()
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "데이터 저장 실패", e)
            getMemberFromDb()
        }
    }

    suspend fun getMemberFromDb(): MemberEntity? {
        return try {
            dao.getMember()
        } catch (e: Exception) {
            Log.e("MemberRepository", "DB 조회 실패", e)
            null
        }
    }

    @Transaction
    suspend fun fetchAndSaveProfile(): ProfileEntity? {
        return try {
            val response = api.getProfile()
            if (response.status == 200) {
                dao.insertProfile(response.data)
                response.data
            } else {
                Log.e("MemberRepository", "API 호출 실패: ${response.message}")
                getProfileFromDb()
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "데이터 저장 실패", e)
            getProfileFromDb()
        }
    }

    suspend fun getProfileFromDb(): ProfileEntity? {
        return try {
            dao.getProfile()
        } catch (e: Exception) {
            Log.e("MemberRepository", "DB 조회 실패", e)
            null
        }
    }

    @Transaction
    suspend fun fetchAndSaveAchievements(): List<AchievementEntity> {
        return try {
            val response = api.getAchievements()
            if (response.status == 200) {
                val entities = response.data.map {
                    AchievementEntity(
                        year = it.year,
                        month = it.month,
                        achievementName = it.achievementName,
                        achievementImageUrl = it.achievementImageUrl,
                        challengeCompleted = it.challengeCompleted
                    )
                }
                dao.insertAchievements(entities)
                entities
            } else {
                Log.e("MemberRepository", "업적 API 호출 실패: ${response.message}")
                getAchievementsFromDb()
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "업적 데이터 저장 실패", e)
            getAchievementsFromDb()
        }
    }

    suspend fun getAchievementsFromDb(): List<AchievementEntity> {
        return try {
            dao.getAchievements()
        } catch (e: Exception) {
            Log.e("MemberRepository", "업적 DB 조회 실패", e)
            emptyList()
        }
    }

    @Transaction
    suspend fun fetchAndSaveTop3Achievements(): List<AchievementEntity> {
        return try {
            val response = api.getTop3Achievements()
            if (response.status == 200) {
                val entities = response.data.map {
                    AchievementEntity(
                        year = it.year,
                        month = it.month,
                        achievementName = it.achievementName,
                        achievementImageUrl = it.achievementImageUrl,
                        challengeCompleted = it.challengeCompleted
                    )
                }
                // 기존 업적 DB에 insert (중복 방지 위해 id/unique key 필요)
                dao.insertAchievements(entities)
                entities
            } else {
                getTop3AchievementsFromDb()
            }
        } catch (e: Exception) {
            getTop3AchievementsFromDb()
        }
    }

    suspend fun getTop3AchievementsFromDb(): List<AchievementEntity> {
        return try {
            dao.getAchievements().take(3)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Transaction
    suspend fun fetchAndSaveMyRecipes(): List<RecipeEntity> {
        return try {
            val response = api.getMyRecipes()
            if (response.status == 200) {
                val entities = response.data.map {
                    RecipeEntity(
                        recipeId = it.recipeId,
                        imageUrl = it.imageUrl
                    )
                }
                dao.insertRecipes(entities)
                entities
            } else {
                Log.e("MemberRepository", "내 레시피 API 호출 실패: ${response.message}")
                getMyRecipesFromDb()
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "내 레시피 데이터 저장 실패", e)
            getMyRecipesFromDb()
        }
    }

    suspend fun getMyRecipesFromDb(): List<RecipeEntity> {
        return try {
            dao.getRecipes()
        } catch (e: Exception) {
            Log.e("MemberRepository", "내 레시피 DB 조회 실패", e)
            emptyList()
        }
    }

    @Transaction
    suspend fun fetchAndSaveScrapRecipes(): List<RecipeEntity> {
        return try {
            val response = api.getScrapRecipes()
            if (response.status == 200) {
                val entities = response.data.map {
                    RecipeEntity(
                        recipeId = it.recipeId,
                        imageUrl = it.imageUrl
                    )
                }
                dao.insertRecipes(entities)
                entities
            } else {
                Log.e("MemberRepository", "스크랩 레시피 API 호출 실패: ${response.message}")
                getScrapRecipesFromDb()
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "스크랩 레시피 데이터 저장 실패", e)
            getScrapRecipesFromDb()
        }
    }

    suspend fun getScrapRecipesFromDb(): List<RecipeEntity> {
        return try {
            dao.getRecipes()
        } catch (e: Exception) {
            Log.e("MemberRepository", "내 레시피 DB 조회 실패", e)
            emptyList()
        }
    }

    @Transaction
    suspend fun fetchAndSaveMyFollowing(): List<FollowEntity> {
        return try {
            val response = api.getFollowing()
            if (response.status == 200) {
                val entities = response.data.map {
                    FollowEntity(
                        followId = it.followId,
                        id = it.id ?: it.followId,
                        nickname = it.nickname,
                        profileUrl = it.profileUrl,
                        title = it.title
                    )
                }
                dao.insertFollows(entities)
                entities
            } else {
                Log.e("MemberRepository", "스크랩 레시피 API 호출 실패: ${response.message}")
                getMyFollowingFromDb()
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "스크랩 레시피 데이터 저장 실패", e)
            getMyFollowingFromDb()
        }
    }

    suspend fun getMyFollowingFromDb(): List<FollowEntity> {
        return try {
            dao.getFollow()
        } catch (e: Exception) {
            Log.e("MemberRepository", "내 레시피 DB 조회 실패", e)
            emptyList()
        }
    }

    @Transaction
    suspend fun fetchAndSaveMyFollower(): List<FollowEntity> {
        return try {
            val response = api.getFollower()
            if (response.status == 200) {
                val entities = response.data.map {
                    FollowEntity(
                        followId = it.followId,
                        id = it.id ?: it.followId,
                        nickname = it.nickname,
                        profileUrl = it.profileUrl,
                        title = it.title
                    )
                }
                dao.insertFollows(entities)
                entities
            } else {
                Log.e("MemberRepository", "스크랩 레시피 API 호출 실패: ${response.message}")
                getMyFollowerFromDb()
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "스크랩 레시피 데이터 저장 실패", e)
            getMyFollowerFromDb()
        }
    }

    suspend fun getMyFollowerFromDb(): List<FollowEntity> {
        return try {
            dao.getFollow()
        } catch (e: Exception) {
            Log.e("MemberRepository", "내 레시피 DB 조회 실패", e)
            emptyList()
        }
    }

    @Transaction
    suspend fun fetchAndSaveIdFollowing(targetMemberId: Int): List<FollowEntity> {
        return try {
            val response = api.getFollowing(targetMemberId)
            if (response.status == 200) {
                val entities = response.data.map {
                    FollowEntity(
                        followId = it.followId,
                        id = it.id ?: it.followId,
                        nickname = it.nickname,
                        profileUrl = it.profileUrl,
                        title = it.title
                    )
                }
                dao.insertFollows(entities)
                entities
            } else {
                Log.e("MemberRepository", "특정 사용자 팔로잉 API 호출 실패: ${response.message}")
                getIdFollowingFromDb(targetMemberId)
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "특정 사용자 팔로잉 데이터 저장 실패", e)
            getIdFollowingFromDb(targetMemberId)
        }
    }

    suspend fun getIdFollowingFromDb(targetMemberId: Int): List<FollowEntity> {
        return try {
            dao.getFollowByMemberId(targetMemberId)
        } catch (e: Exception) {
            Log.e("MemberRepository", "특정 사용자 팔로잉 DB 조회 실패", e)
            emptyList()
        }
    }

    @Transaction
    suspend fun fetchAndSaveIdFollower(targetMemberId: Int): List<FollowEntity> {
        return try {
            val response = api.getFollower(targetMemberId)
            if (response.status == 200) {
                val entities = response.data.map {
                    FollowEntity(
                        followId = it.followId,
                        id = it.id ?: it.followId,
                        nickname = it.nickname,
                        profileUrl = it.profileUrl,
                        title = it.title
                    )
                }
                dao.insertFollows(entities)
                entities
            } else {
                Log.e("MemberRepository", "특정 사용자 팔로워 API 호출 실패: ${response.message}")
                getIdFollowerFromDb(targetMemberId)
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "특정 사용자 팔로워 데이터 저장 실패", e)
            getIdFollowerFromDb(targetMemberId)
        }
    }

    suspend fun getIdFollowerFromDb(targetMemberId: Int): List<FollowEntity> {
        return try {
            dao.getFollowByMemberId(targetMemberId)
        } catch (e: Exception) {
            Log.e("MemberRepository", "특정 사용자 팔로워 DB 조회 실패", e)
            emptyList()
        }
    }

    suspend fun signup(signupRequest: SignupRequest, profileImageFile: File?): Any? {
        return try {
            val gson = Gson()
            val dtoJson = gson.toJson(signupRequest)
            val dtoBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), dtoJson)
            val imagePart = profileImageFile?.let {
                val reqFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("profileImage", it.name, reqFile)
            }
            api.signup(dtoBody, imagePart)
        } catch (e: Exception) {
            Log.e("MemberRepository", "회원가입 실패", e)
            null
        }
    }

    // 이메일 인증 요청
    suspend fun requestEmailVerification(email: String): SignupResponse? {
        return try {
            val json = """{"email":"$email"}"""
            val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)
            api.emailVerification(body)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun verifyEmailCode(email: String, code: String): SignupResponse? {
        return try {
            val json = """{"email":"$email","code":"$code"}"""
            val body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)
            api.emailVerify(body)
        } catch (e: Exception) {
            null
        }
    }
}