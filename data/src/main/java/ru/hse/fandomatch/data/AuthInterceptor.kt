package ru.hse.fandomatch.data

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import ru.hse.fandomatch.domain.repos.SharedPrefRepository

class AuthInterceptor(
    private val sharedPrefRepository: SharedPrefRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sharedPrefRepository.getToken()
        val request = chain.request().newBuilder()
        if (!token.isNullOrEmpty()) {
            Log.d("AuthInterceptor", "Adding token to request: ${chain.request().url()}")
            request.addHeader("Authorization", "Bearer $token")
        } else {
            Log.d("AuthInterceptor", "No token found, proceeding without Authorization header")
        }
        return chain.proceed(request.build())
    }
}
