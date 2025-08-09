package com.example.elixir.member.network

import android.util.Log
import androidx.room.Transaction
import com.example.elixir.member.data.AchievementEntity
import com.example.elixir.member.data.ChallengeEntity
import com.example.elixir.member.data.FollowEntity
import com.example.elixir.member.data.MemberDao
import com.example.elixir.member.data.MemberEntity
import com.example.elixir.member.data.ProfileEntity
import com.example.elixir.member.data.RecipeEntity
import com.example.elixir.signup.SignupRequest
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MemberRepository(
    private val api: MemberApi,
    private val dao: MemberDao
) {
    // 회원 정보
    suspend fun getMemberFromDb(): MemberEntity? {
        return try {
            dao.getMember()
        } catch (e: Exception) {
            Log.e("MemberRepository", "DB 조회 실패", e)
            null
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

    // 프로필 정보
    suspend fun getProfileFromDb(): ProfileEntity? {
        return try {
            Log.d("MemberRepository", "프로필: ${dao.getProfile()}")
            dao.getProfile()
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

    // 특정 회원의 프로필 정보 가져오기
    suspend fun fetchAndSaveProfile(memberId: Int): ProfileEntity? {
        return try {
            val response = api.getProfile(memberId)
            if (response.status == 200) {
                response.data
            } else {
                Log.e("MemberRepository", "회원 프로필 API 호출 실패: ${response.message}")
                null
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "회원 프로필 로드 실패", e)
            null
        }
    }

    // 팔로우/언팔로우
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

    // 팔로잉/팔로워 정보
    suspend fun getMyFollowingFromDb(): List<FollowEntity> {
        return try {
            dao.getFollow()
        } catch (e: Exception) {
            Log.e("MemberRepository", "내 팔로잉 DB 조회 실패", e)
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
                Log.e("MemberRepository", "내 팔로잉 API 호출 실패: ${response.message}")
                getMyFollowingFromDb()
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "내 팔로잉 데이터 저장 실패", e)
            getMyFollowingFromDb()
        }
    }

    suspend fun getMyFollowerFromDb(): List<FollowEntity> {
        return try {
            dao.getFollow()
        } catch (e: Exception) {
            Log.e("MemberRepository", "내 팔로워 DB 조회 실패", e)
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
                Log.e("MemberRepository", "내 팔로워 API 호출 실패: ${response.message}")
                getMyFollowerFromDb()
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "내 팔로워 데이터 저장 실패", e)
            getMyFollowerFromDb()
        }
    }

    suspend fun getMyFollowingIds(): Set<Int> {
        return try {
            val response = api.getFollowing()
            if (response.status == 200) {
                response.data.map { it.id ?: it.followId }.toSet()
            } else {
                Log.e("MemberRepository", "내 팔로잉 ID 목록 API 호출 실패: ${response.message}")
                emptySet()
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "내 팔로잉 ID 목록 로드 실패", e)
            emptySet()
        }
    }

    suspend fun checkIsFollowing(targetMemberId: Int): Boolean {
        return try {
            val response = api.getFollowing() // 내 팔로잉 목록 가져오기
            if (response.status == 200) {
                response.data.any { it.id == targetMemberId || it.followId == targetMemberId }
            } else {
                Log.e("MemberRepository", "팔로잉 목록 확인 API 호출 실패: ${response.message}")
                false
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "팔로우 상태 확인 실패", e)
            false
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

    suspend fun getIdFollowerFromDb(targetMemberId: Int): List<FollowEntity> {
        return try {
            dao.getFollowByMemberId(targetMemberId)
        } catch (e: Exception) {
            Log.e("MemberRepository", "특정 사용자 팔로워 DB 조회 실패", e)
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

    // 업적 정보
    suspend fun getChallengesFromDb(): List<ChallengeEntity> {
        return try {
            dao.getChallenges()
        } catch (e: Exception) {
            Log.e("MemberRepository", "업적 DB 조회 실패", e)
            emptyList()
        }
    }

    @Transaction
    suspend fun fetchAndSaveChallenges(): List<ChallengeEntity> {
        return try {
            val response = api.getChallenges()
            if (response.status == 200) {
                val entities = response.data.map {
                    ChallengeEntity(
                        year = it.year,
                        month = it.month,
                        achievementName = it.achievementName,
                        achievementImageUrl = it.achievementImageUrl,
                        challengeCompleted = it.challengeCompleted
                    )
                }
                dao.insertChallenges(entities)
                entities
            } else {
                Log.e("MemberRepository", "업적 API 호출 실패: ${response.message}")
                getChallengesFromDb()
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "업적 데이터 저장 실패", e)
            getChallengesFromDb()
        }
    }

    suspend fun getTop3ChallengesFromDb(): List<ChallengeEntity> {
        return try {
            dao.getChallenges().take(3)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Transaction
    suspend fun fetchAndSaveTop3Challenges(): List<ChallengeEntity> {
        return try {
            val response = api.getAllAchievements()
            if (response.status == 200) {
                val entities = response.data.map {
                    ChallengeEntity(
                        year = it.year,
                        month = it.month,
                        achievementName = it.achievementName,
                        achievementImageUrl = it.achievementImageUrl,
                        challengeCompleted = it.challengeCompleted
                    )
                }
                dao.insertChallenges(entities)
                entities
            } else {
                getTop3ChallengesFromDb()
            }
        } catch (e: Exception) {
            getTop3ChallengesFromDb()
        }
    }

    // 모든 업적을 DB에서 가져오는 함수
    suspend fun getAllAchievementsFromDb(): List<AchievementEntity> {
        return try {
            // dao.getAllAchievements() 호출 (AchievementDao의 메서드)
            dao.getAchievements()
        } catch (e: Exception) {
            Log.e("MemberRepository", "업적 DB 조회 실패", e)
            emptyList()
        }
    }

    // API에서 모든 업적을 가져와 DB에 저장하는 함수
    @Transaction
    suspend fun fetchAndSaveAllAchievements(): List<AchievementEntity> {
        return try {
            val response = api.getAchievements() // 업적 API 호출
            if (response.status == 200 && response.data != null) {
                val entities = response.data.map {
                    // API 응답 데이터에 맞춰 AchievementEntity로 매핑
                    AchievementEntity(
                        code = it.code, // 업적 코드를 사용
                        achievementName = it.achievementName,
                        description = it.description,
                        achievementImageUrl = it.achievementImageUrl,
                        completed = it.completed,
                        level = it.level,
                        type = it.type
                    )
                }
                // achievementDao.insertAllAchievements(entities) 호출 (AchievementDao의 메서드)
                dao.insertAchievements(entities)
                entities
            } else {
                Log.e("MemberRepository", "업적 API 호출 실패: ${response.message}")
                getAllAchievementsFromDb() // API 실패 시 DB에서 기존 데이터 반환
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "업적 데이터 저장/조회 실패", e)
            getAllAchievementsFromDb() // 예외 발생 시 DB에서 기존 데이터 반환
        }
    }

    // DB에서 상위 3개의 업적을 가져오는 함수 (예시: 완료된 업적 중 레벨이 높은 3개)
    suspend fun getTop3AchievementsFromDb(): List<AchievementEntity> {
        return try {
            // dao.getTop3CompletedAchievements() 호출 (AchievementDao의 메서드)
            dao.getAchievements().take(3)
        } catch (e: Exception) {
            Log.e("MemberRepository", "상위 3개 업적 DB 조회 실패", e)
            emptyList()
        }
    }

    // API에서 상위 3개 업적을 가져와 DB에 저장하는 함수 (API가 상위 3개만 제공하는 경우)
    @Transaction
    suspend fun fetchAndSaveTop3Achievements(): List<AchievementEntity> {
        return try {
            val response = api.getTop3Achievements() // 상위 3개 업적 API 호출 (가상의 API)
            if (response.status == 200 && response.data != null) {
                val entities = response.data.map {
                    AchievementEntity(
                        code = it.code,
                        achievementName = it.achievementName,
                        description = it.description,
                        achievementImageUrl = it.achievementImageUrl,
                        completed = it.completed,
                        level = it.level,
                        type = it.type
                    )
                }
                dao.insertAchievements(entities) // 상위 3개 업적을 DB에 저장
                entities
            } else {
                Log.e("MemberRepository", "상위 3개 업적 API 호출 실패: ${response.message}")
                getTop3AchievementsFromDb() // API 실패 시 DB에서 기존 데이터 반환
            }
        } catch (e: Exception) {
            Log.e("MemberRepository", "상위 3개 업적 저장/조회 실패", e)
            getTop3AchievementsFromDb() // 예외 발생 시 DB에서 기존 데이터 반환
        }
    }

    // 레시피 정보
    suspend fun getMyRecipesFromDb(): List<RecipeEntity> {
        return try {
            dao.getRecipes()
        } catch (e: Exception) {
            Log.e("MemberRepository", "내 레시피 DB 조회 실패", e)
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

    suspend fun getScrapRecipesFromDb(): List<RecipeEntity> {
        return try {
            dao.getRecipes()
        } catch (e: Exception) {
            Log.e("MemberRepository", "스크랩 레시피 DB 조회 실패", e)
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

    // 회원가입 및 인증
    suspend fun signup(signupRequest: SignupRequest, profileImageFile: File?): Any? {
        return try {
            val gson = Gson()
            val dtoJson = gson.toJson(signupRequest)
            val dtoBody = dtoJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
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

    // 회원가입 및 인증
    suspend fun socialSignup(loginType: String, signupRequest: SocialSignupDto, profileImageFile: File?): Any? {
        return try {
            val gson = Gson()
            val dtoJson = gson.toJson(signupRequest)
            val dtoBody = dtoJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val imagePart = profileImageFile?.let {
                val reqFile = it.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("profileImage", it.name, reqFile)
            }
            api.socialSignup(loginType, dtoBody, imagePart)
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

    suspend fun updatePassword(email: String, newPassword: String): Boolean {
        return try {
            val json = """{"email":"$email","newPassword":"$newPassword"}"""
            val requestBody = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), json)
            val response = api.putUpdatePassword(requestBody)
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("MemberRepository", "비밀번호 업데이트 실패", e)
            false
        }
    }

    // 소셜 로그인
    suspend fun socialLogin(loginType: String, accessToken: String): Result<SocialLoginData> {
        return try {
            val response = api.socialLogin(loginType, AccessTokenRequest(accessToken))
            Log.d("MemberRepository", response.status.toString())
            when(response.status) {
                200 -> Result.success(response.data)
                400 -> Result.failure(Throwable("잘못된 요청입니다."))
                401 -> Result.failure(Throwable("인증이 필요합니다."))
                403 -> Result.failure(Throwable("권한이 없습니다."))
                404 -> Result.failure(Throwable("사용자를 찾을 수 없습니다."))
                409 -> Result.failure(Throwable("이미 가입된 계정입니다."))
                500 -> Result.failure(Throwable("서버 오류입니다."))
                else -> Result.failure(Throwable("알 수 없는 오류가 발생했습니다."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}