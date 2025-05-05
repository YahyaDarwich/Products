package com.example.products.data

import android.content.Context
import com.example.products.BuildConfig
import com.example.products.network.GoogleApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val productsRepository: ProductsRepository
    val googleApiRepository: GoogleApiRepository
    val authRepository: AuthRepository
}

class AppDataContainer(context: Context) : AppContainer {
    private val googleApiBaseUrl = BuildConfig.GOOGLE_API_BASE_URL
    private val okHttpClient = OkHttpClient.Builder().build()
    private val retrofit = Retrofit.Builder().client(okHttpClient)
        .baseUrl(googleApiBaseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofitService: GoogleApiService by lazy {
        retrofit.create(GoogleApiService::class.java)
    }

    override val productsRepository: ProductsRepository by lazy {
        OfflineProductsRepository(ProductsDatabase.getDatabase(context))
    }

    override val googleApiRepository: GoogleApiRepository by lazy {
        NetworkGoogleApiAppRepository(retrofitService)
    }

    override val authRepository: AuthRepository
        get() = NetworkAppAuthRepository()
}