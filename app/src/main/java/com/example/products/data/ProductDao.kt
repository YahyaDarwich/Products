package com.example.products.data

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("Select * from products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("Select * from products ORDER BY name ASC")
    fun getAllProductsToExport(): Cursor

    @Query("SELECT * from products WHERE name LIKE '%' || :searchKeyword || '%' ORDER BY name ASC")
    fun searchProducts(searchKeyword: String): Flow<List<Product>>

    @Query("Select * from products WHERE id = :productId")
    fun getProduct(productId: Int): Flow<Product>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)
}