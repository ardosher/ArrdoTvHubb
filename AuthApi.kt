package uz.ardo.tvhub

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Firebase Authentication (Email/Parol) — REST orqali.
 * Faqat Web API Key kerak, google-services.json ham, plagin ham shart emas.
 */
object AuthConfig {
    // Firebase Console > Project settings > General > "Web API Key"
    const val API_KEY = "PASTE_FIREBASE_WEB_API_KEY"
}

class AuthException(message: String) : Exception(message)

class AuthResult(val email: String, val uid: String, val idToken: String, val refreshToken: String)

object AuthApi {

    fun signUp(email: String, password: String) = call("accounts:signUp", email, password)
    fun signIn(email: String, password: String) = call("accounts:signInWithPassword", email, password)

    private fun call(endpoint: String, email: String, password: String): AuthResult {
        if (AuthConfig.API_KEY.startsWith("PASTE")) {
            throw AuthException("Firebase kaliti kiritilmagan (AuthApi.kt ichidagi API_KEY).")
        }
        val url = URL("https://identitytoolkit.googleapis.com/v1/$endpoint?key=${AuthConfig.API_KEY}")
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 15000
            conn.readTimeout = 15000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            val body = JSONObject()
                .put("email", email)
                .put("password", password)
                .put("returnSecureToken", true)
                .toString()
            conn.outputStream.use { it.write(body.toByteArray()) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
            val json = try { JSONObject(text) } catch (e: Exception) { JSONObject() }

            if (code !in 200..299) {
                throw AuthException(translate(json.optJSONObject("error")?.optString("message") ?: ""))
            }
            return AuthResult(
                email = json.optString("email", email),
                uid = json.optString("localId"),
                idToken = json.optString("idToken"),
                refreshToken = json.optString("refreshToken")
            )
        } finally {
            conn.disconnect()
        }
    }

    private fun translate(m: String): String = when {
        m.startsWith("EMAIL_EXISTS") -> "Bu email bilan hisob allaqachon bor. Kirish tugmasini bosing."
        m.startsWith("INVALID_LOGIN_CREDENTIALS") ||
        m.startsWith("INVALID_PASSWORD") ||
        m.startsWith("EMAIL_NOT_FOUND") -> "Email yoki parol noto'g'ri."
        m.startsWith("WEAK_PASSWORD") -> "Parol kamida 6 ta belgidan iborat bo'lsin."
        m.startsWith("INVALID_EMAIL") -> "Email noto'g'ri yozilgan."
        m.startsWith("USER_DISABLED") -> "Bu hisob o'chirilgan."
        m.startsWith("TOO_MANY_ATTEMPTS") -> "Juda ko'p urinish. Birozdan so'ng qayta urinib ko'ring."
        m.startsWith("OPERATION_NOT_ALLOWED") -> "Firebase'da Email/Parol usuli yoqilmagan."
        m.contains("API key") -> "Firebase kaliti noto'g'ri."
        else -> "Xatolik yuz berdi. Qayta urinib ko'ring."
    }
}

object Session {
    private fun prefs(c: Context) = c.getSharedPreferences("auth", Context.MODE_PRIVATE)

    fun email(c: Context): String? = prefs(c).getString("email", null)

    fun save(c: Context, r: AuthResult) {
        prefs(c).edit()
            .putString("email", r.email)
            .putString("uid", r.uid)
            .putString("refresh", r.refreshToken)
            .apply()
    }

    fun clear(c: Context) = prefs(c).edit().clear().apply()
}
