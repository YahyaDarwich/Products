package com.example.products.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class ProductsDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var Instance: ProductsDatabase? = null

        fun getDatabase(context: Context): ProductsDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, ProductsDatabase::class.java, "product_database")
                    .fallbackToDestructiveMigration()
                    .build().also { Instance = it }
            }
        }
    }
}