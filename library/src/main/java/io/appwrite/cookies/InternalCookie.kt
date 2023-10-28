package io.appwrite.cookies

import io.ktor.http.Cookie
import io.ktor.util.date.GMTDate
import kotlinx.serialization.Serializable

@Serializable
data class InternalCookie(
    val name: String,
    val value: String,
    val maxAge: Int = 0,
    val expires: Long? = null,
    val domain: String? = null,
    val path: String? = null,
    val secure: Boolean = false,
    val httpOnly: Boolean = false,
    val extensions: Map<String, String?> = emptyMap()
) {
    companion object {
        fun InternalCookie.toCookie(): Cookie {
            return Cookie(
                name = this.name,
                value = this.value,
                maxAge = this.maxAge,
                expires = this.expires?.let { GMTDate(it) },
                domain = this.domain,
                path = this.path,
                secure = this.secure,
                httpOnly = this.httpOnly,
                extensions = this.extensions
            )
        }

        fun Cookie.toCustomCookie(): InternalCookie {
            return InternalCookie(
                name = this.name,
                value = this.value,
                maxAge = this.maxAge,
                expires = this.expires?.timestamp,
                domain = this.domain,
                path = this.path,
                secure = this.secure,
                httpOnly = this.httpOnly,
                extensions = this.extensions
            )
        }
    }
}