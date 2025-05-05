package com.example.products.data

import android.content.Context
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import java.io.File

interface AuthRepository {
    val requestedScopes: List<Scope>
    val authorizationRequest: AuthorizationRequest
    val backupFileName: String

    fun authorize(
        context: Context,
    ) = callbackFlow<AuthResult> { }

    fun backup(context: Context) = channelFlow<BackupResult> {}
}

sealed interface AuthResult {
    data class Error(val e: Exception) : AuthResult
    data class Success(val authorizationResult: AuthorizationResult) : AuthResult
}

sealed interface BackupResult {
    data object Success : BackupResult
    data object Loading : BackupResult
    data class Error(val e: Exception) : BackupResult
}