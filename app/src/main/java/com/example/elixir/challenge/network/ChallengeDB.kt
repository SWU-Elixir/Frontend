package com.example.elixir.challenge.network

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.elixir.challenge.data.ChallengeDao
import com.example.elixir.challenge.data.ChallengeEntity

@Database(entities = [ChallengeEntity::class], version = 2)
@TypeConverters(IngredientsConverter::class)
abstract class ChallengeDB : RoomDatabase() {
    abstract fun challengeDao(): ChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: ChallengeDB? = null

        fun getInstance(context: Context): ChallengeDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChallengeDB::class.java,
                    "challenge_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

