package com.example.elixir.network

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.elixir.calendar.data.DietLogEntity
import com.example.elixir.calendar.network.db.DietLogDao
import com.example.elixir.recipe.data.dao.CommentDao
import com.example.elixir.recipe.data.dao.RecipeDao
import com.example.elixir.recipe.data.entity.CommentEntity
import com.example.elixir.recipe.data.entity.RecipeEntity
import com.example.elixir.utils.DBConverters

// 데이터베이스 안 객체와 데이터 정의
@Database(entities = [DietLogEntity::class, RecipeEntity::class, CommentEntity::class], version = 11)
@TypeConverters(DBConverters::class)
abstract class AppDatabase : RoomDatabase() {
    // DAO 객체를 가져오는 메서드
    abstract fun dietLogDao(): DietLogDao
    abstract fun recipeDao(): RecipeDao
    abstract fun commentDao(): CommentDao

    // 데이터베이스 인스턴스 생성
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elixir_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}