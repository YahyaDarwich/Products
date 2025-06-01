package com.example.products.data

import android.content.Context
import com.example.products.BuildConfig
import com.example.products.R
import com.example.products.csv_utils.CSVManager
import com.example.products.helpers.LocalStorage
import com.example.products.helpers.Tools
import com.example.products.ui.screens.SettingsKeys
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import java.io.File
import java.io.FileWriter
import java.util.Date


class NetworkAppAuthRepository() : AuthRepository {
    override val requestedScopes: List<Scope>
        get() = listOf(Scope(DriveScopes.DRIVE_FILE))
    override val authorizationRequest: AuthorizationRequest
        get() = AuthorizationRequest.builder().setRequestedScopes(requestedScopes)
            .requestOfflineAccess(BuildConfig.WEB_CLIENT_ID, true)
            .build()
    override val backupFileName: String
        get() = "Products App Data Backup.csv"

    override fun authorize(
        context: Context
    ) = callbackFlow {
        Identity.getAuthorizationClient(context)
            .authorize(authorizationRequest)
            .addOnSuccessListener { result ->
                trySend(AuthResult.Success(result))
            }
            .addOnFailureListener { e ->
                trySend(AuthResult.Error(e))
            }

        awaitClose { }
    }

    override fun backup(context: Context) = channelFlow {
        try {
            send(BackupResult.Loading)

            val localStorage = LocalStorage(context)
            var accessToken = localStorage.getString(SettingsKeys.ACCESS_TOKEN.keyName)
            val isTokenExpired =
                System.currentTimeMillis() >= localStorage.getLong(SettingsKeys.EXPIRATION_TIME.keyName)

            if (isTokenExpired) {
                val credential = GoogleCredential.Builder()
                    .setTransport(AndroidHttp.newCompatibleTransport())
                    .setJsonFactory(GsonFactory.getDefaultInstance())
                    .setClientSecrets(BuildConfig.WEB_CLIENT_ID, BuildConfig.WEB_CLIENT_SECRET)
                    .build()
                    .setRefreshToken(localStorage.getString(SettingsKeys.REFRESH_TOKEN.keyName))

                credential.refreshToken()

                accessToken = credential.accessToken
                localStorage.putString(
                    SettingsKeys.ACCESS_TOKEN.keyName,
                    accessToken
                )
                localStorage.putString(
                    SettingsKeys.REFRESH_TOKEN.keyName,
                    credential.refreshToken
                )
                localStorage.putLong(
                    SettingsKeys.EXPIRATION_TIME.keyName,
                    credential.expirationTimeMilliseconds
                )
            }

            val credential = GoogleCredential().setAccessToken(accessToken)

            val drive = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName(context.getString(R.string.app_name))
                .build()

            val result = drive.files().list()
                .setQ("name = '$backupFileName' and trashed = false")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute()

            val files = result.files

            val fileMetaData = com.google.api.services.drive.model.File()
            fileMetaData.setName(backupFileName)
            fileMetaData.setMimeType("text/csv")

            val file = File(context.filesDir, "$backupFileName.csv")
            CSVManager.writeCSVFile(FileWriter(file))
            val mediaContent = FileContent("text/csv", file);

            if (files.isEmpty()) {
                drive.files().create(fileMetaData, mediaContent)
                    .setFields("id")
                    .execute()
            } else {
                drive.files().update(files[0].id, fileMetaData, mediaContent)
                    .execute()
            }

            localStorage.putString(
                SettingsKeys.LAST_BACKUP_DATE.keyName,
                Tools.formatDate(Date(), "dd MMM yyyy, hh:mm aa")
            )

            localStorage.putString(
                SettingsKeys.LAST_BACKUP_FILE_SIZE.keyName, Tools.formatFileSize(
                    file.length()
                )
            )

            send(BackupResult.Success)
        } catch (e: GoogleJsonResponseException) {
            e.printStackTrace()
            send(BackupResult.Error(e))
        } catch (e: Exception) {
            e.printStackTrace()
            send(BackupResult.Error(e))
        }
    }
}