package com.example.elixir.network

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.elixir.calendar.data.DietLogEntity
import com.example.elixir.calendar.network.db.DietLogDao
import com.example.elixir.challenge.data.ChallengeDao
import com.example.elixir.challenge.data.ChallengeDetailEntity
import com.example.elixir.challenge.network.IngredientsConverter
import com.example.elixir.ingredient.data.IngredientDao
import com.example.elixir.ingredient.data.IngredientEntity
import com.example.elixir.member.data.AchievementEntity
import com.example.elixir.member.data.ChallengeEntity
import com.example.elixir.member.data.FollowEntity
import com.example.elixir.member.data.MemberDao
import com.example.elixir.member.data.MemberEntity
import com.example.elixir.member.data.ProfileEntity
import com.example.elixir.recipe.data.dao.CommentDao
import com.example.elixir.recipe.data.dao.RecipeDao
import com.example.elixir.recipe.data.entity.CommentEntity
import com.example.elixir.recipe.data.entity.RecipeEntity
import com.example.elixir.utils.DBConverters

// 데이터베이스 안 객체와 데이터 정의
@Database(
    entities = [
        DietLogEntity::class,           // 식단 엔티티
        RecipeEntity::class,            // 레시피 엔티티
        CommentEntity::class,           // 댓글 엔티티
        MemberEntity::class,            // 사용자 엔티티
        ChallengeEntity::class,         // 챌린지 엔티티
        ChallengeDetailEntity::class,   // 챌린지 상세 엔티티
        AchievementEntity::class,       // 업적 엔티티
        FollowEntity::class,            // 팔로우 엔티티
        ProfileEntity::class,           // 프로필 엔티티
        IngredientEntity::class         // 식재료 엔티티
    ],
    version = 13,
)
@TypeConverters(DBConverters::class, IngredientsConverter::class)
abstract class AppDatabase : RoomDatabase() {
    // ---------------------- DAO 정의 ---------------------- //
    abstract fun dietLogDao(): DietLogDao
    abstract fun recipeDao(): RecipeDao
    abstract fun commentDao(): CommentDao
    abstract fun memberDao(): MemberDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun ingredientDao(): IngredientDao

    // 인스턴스 정의
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elixir_database" // DB 파일명 하나로
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

