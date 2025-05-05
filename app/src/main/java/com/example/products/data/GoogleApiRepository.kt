package com.example.products.data

import com.example.products.models.UserInfoResponse
import com.example.products.network.GoogleApiService

interface GoogleApiRepository {
    suspend fun getUserInfo(token: String): UserInfoResponse
}

class NetworkGoogleApiAppRepository(private val googleApiService: GoogleApiService) :
    GoogleApiRepository {
    override suspend fun getUserInfo(token: String): UserInfoResponse {
        return googleApiService.getUserInfo(token)
    }
}