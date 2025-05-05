package com.example.products.network

import com.example.products.models.UserInfoResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface GoogleApiService {
    @GET("userinfo")
    suspend fun getUserInfo(@Header("Authorization") authHeader: String): UserInfoResponse
}