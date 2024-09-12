//package com.example.commutedocmaker.integrity
//
//import com.google.android.play.core.integrity.StandardIntegrityManager
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.util.concurrent.TimeUnit
//
//class IntegrityApiManager(private val integrityManager: StandardIntegrityManager) {
//
//    private val cache = mutableMapOf<String, CachedToken>()
//
//    sealed class IntegrityResult {
//        data class Success(val token: String) : IntegrityResult()
//        data class Failure(val exception: Exception) : IntegrityResult()
//    }
//
//    data class CachedToken(val token: String, val timestamp: Long)
//
//    suspend fun requestIntegrityToken(requestHash: String, forceRefresh: Boolean = false): IntegrityResult {
//        if (!forceRefresh) {
//            val cachedToken = getCachedToken(requestHash)
//            if (cachedToken != null) {
//                return IntegrityResult.Success(cachedToken)
//            }
//        }
//
//        return withContext(Dispatchers.IO) {
//            try {
//                val integrityTokenProvider = integrityManager.prepareIntegrityToken()
//                val request = StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
//                    .setRequestHash(requestHash)
//                    .build()
//
//                val response = integrityTokenProvider.requast(request).await()
//                val token = response.token()
//                cacheToken(requestHash, token)
//                IntegrityResult.Success(token)
//            } catch (e: Exception) {
//                IntegrityResult.Failure(e)
//            }
//        }
//    }
//
//    private fun getCachedToken(requestHash: String): String? {
//        val cachedToken = cache[requestHash]
//        return if (cachedToken != null && !isTokenExpired(cachedToken)) {
//            cachedToken.token
//        } else {
//            null
//        }
//    }
//
//    private fun cacheToken(requestHash: String, token: String) {
//        cache[requestHash] = CachedToken(token, System.currentTimeMillis())
//    }
//
//    private fun isTokenExpired(cachedToken: CachedToken): Boolean {
//        val currentTime = System.currentTimeMillis()
//        val tokenAge = currentTime - cachedToken.timestamp
//        return tokenAge > TimeUnit.HOURS.toMillis(1) // Assume tokens expire after 1 hour
//    }
//}
