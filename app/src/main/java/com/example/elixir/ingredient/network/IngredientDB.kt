package com.example.elixir.ingredient.network
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.elixir.ingredient.data.IngredientDao
import com.example.elixir.ingredient.data.IngredientData

@Database(entities = [IngredientData::class], version = 3)
abstract class IngredientDB : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var INSTANCE: IngredientDB? = null

        fun getInstance(context: Context): IngredientDB {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IngredientDB::class.java,
                    "ingredient"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}