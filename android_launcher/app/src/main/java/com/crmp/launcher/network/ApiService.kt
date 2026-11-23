package com.crmp.launcher.network

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class AuthResult(val success: Boolean, val token: String = "", val nickname: String = "", val error: String = "")
data class VersionInfo(val latestVersion: String, val patchUrl: String, val mandatory: Boolean)

object ApiService {
    // 10.0.2.2 — localhost вашего ПК при запуске в Android Emulator
    private const val BASE_URL = "http://10.0.2.2:3000"

    fun register(nick: String, email: String, password: String, referral: String): AuthResult {
        val body = JSONObject().apply {
            put("nickname", nick)
            put("email", email)
            put("password", password)
            put("referral", referral)
        }
        return postAuth("/api/auth/register", body)
    }

    fun login(nick: String, password: String): AuthResult {
        val body = JSONObject().apply {
            put("nickname", nick)
            put("password", password)
        }
        return postAuth("/api/auth/login", body)
    }

    fun fetchVersion(): VersionInfo? {
        return try {
            val conn = (URL("$BASE_URL/api/version").openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
            }
            if (conn.responseCode == 200) {
                val json = JSONObject(conn.inputStream.bufferedReader().readText())
                VersionInfo(
                    latestVersion = json.optString("latestVersion", "1.0.0"),
                    patchUrl = json.optString("patchUrl", ""),
                    mandatory = json.optBoolean("mandatory", false)
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun postAuth(path: String, body: JSONObject): AuthResult {
        return try {
            val conn = (URL("$BASE_URL$path").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
                connectTimeout = 5000
                readTimeout = 5000
            }
            conn.outputStream.use { it.write(body.toString().toByteArray()) }

            val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.readText().orEmpty()
            val json = JSONObject(text)

            if (conn.responseCode in 200..299) {
                AuthResult(
                    success = true,
                    token = json.optString("token"),
                    nickname = json.optString("nickname")
                )
            } else {
                AuthResult(success = false, error = json.optString("error", "Ошибка сервера"))
            }
        } catch (e: Exception) {
            AuthResult(success = false, error = "Нет связи с API: ${e.message}")
        }
    }
}
