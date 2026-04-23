package ru.hse.fandomatch.data.api

import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Url

interface S3UploadApiService {
    @PUT
    suspend fun upload(
        @Url url: String,
        @Body body: okhttp3.RequestBody
    ): retrofit2.Response<Unit>
}
