package com.example.elixir.member.network

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.elixir.member.data.MemberDao
import com.example.elixir.member.data.MemberEntity
import com.example.elixir.member.data.AchievementEntity
import com.example.elixir.member.data.FollowEntity
import com.example.elixir.member.data.ProfileEntity
import com.example.elixir.member.data.RecipeEntity

@Database(
    entities = [
        MemberEntity::class,
        AchievementEntity::class,
        RecipeEntity::class,
        FollowEntity::class,
        ProfileEntity::class
    ],
    version = 4
)
abstract class MemberDB : RoomDatabase() {
    abstract fun memberDao(): MemberDao

    companion object {
        @Volatile
        private var INSTANCE: MemberDB? = null

        fun getInstance(context: Context): MemberDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MemberDB::class.java,
                    "member"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}