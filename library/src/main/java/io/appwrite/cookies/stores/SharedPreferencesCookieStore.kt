package io.appwrite.cookies.stores

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.hostIsIp
import io.ktor.http.isSecure
import io.ktor.util.date.getTimeMillis
import io.ktor.util.toLowerCasePreservingASCIIRules
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.min


fun Cookie.matches(requestUrl: Url): Boolean {
    val domain = domain?.toLowerCasePreservingASCIIRules()?.trimStart('.')
        ?: error("Domain field should have the default value")

    val path = with(path) {
        val current = path ?: error("Path field should have the default value")
        if (current.endsWith('/')) current else "$path/"
    }

    val host = requestUrl.host.toLowerCasePreservingASCIIRules()
    val requestPath = let {
        val pathInRequest = requestUrl.encodedPath
        if (pathInRequest.endsWith('/')) pathInRequest else "$pathInRequest/"
    }

    if (host != domain && (hostIsIp(host) || !host.endsWith(".$domain"))) {
        return false
    }

    if (path != "/" &&
        requestPath != path &&
        !requestPath.startsWith(path)
    ) {
        return false
    }

    return !(secure && !requestUrl.protocol.isSecure())
}

fun Cookie.fillDefaults(requestUrl: Url): Cookie {
    var result = this

    if (result.path?.startsWith("/") != true) {
        result = result.copy(path = requestUrl.encodedPath)
    }

    if (result.domain.isNullOrBlank()) {
        result = result.copy(domain = requestUrl.host)
    }

    return result
}

class AcceptAllCookiesStorage(
    context: Context,
    private val name: String
) : CookiesStorage {
    private val preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    private val gson = Gson()

    private val container: MutableList<Cookie> = mutableListOf()
    private val oldestCookie: AtomicLong = AtomicLong(0L)
    private val mutex = Mutex()

    init {
        preferences.all.forEach { (key, value) ->
            try {
                val cookieType = object : TypeToken<Cookie>() {}.type
                val internalCookies =
                    gson.fromJson<Cookie>(value.toString(), cookieType)
                container.add(internalCookies)
            } catch (exception: Throwable) {
                exception.printStackTrace()
                Log.e(
                    javaClass.simpleName,
                    "Error while loading key = $key, value = $value from cookie store named $name",
                    exception
                )
            }
        }
    }

    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
        val now = getTimeMillis()
        if (now >= oldestCookie.get()) cleanup(now)

        return@withLock container.filter { it.matches(requestUrl) }
    }

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie): Unit = mutex.withLock {
        with(cookie) {
            if (name.isBlank()) return@withLock
        }

        container.removeAll {
            val shouldRemove = it.name == cookie.name && it.matches(requestUrl)
            if (shouldRemove) {
                preferences.edit()
                    .remove(cookie.name)
                    .apply()
            }
            shouldRemove
        }
        val copyCookie = cookie.fillDefaults(requestUrl)
        container.add(copyCookie)
        cookie.expires?.timestamp?.let { expires ->
            if (oldestCookie.get() > expires) {
                oldestCookie.updateAndGet { expires }
            }
        }

        val cookieType = object : TypeToken<Cookie>() {}.type
        val json = gson.toJson(copyCookie, cookieType)
        preferences
            .edit()
            .putString(copyCookie.name, json)
            .apply()
    }

    override fun close() {
    }

    private fun cleanup(timestamp: Long) {
        container.removeAll { cookie ->
            val expires = cookie.expires?.timestamp ?: return@removeAll false
            if (expires < timestamp) {
                //remove item
                preferences.edit()
                    .remove(cookie.name)
                    .apply()
            }
            expires < timestamp
        }

        val newOldest = container.fold(Long.MAX_VALUE) { acc, cookie ->
            cookie.expires?.timestamp?.let { min(acc, it) } ?: acc
        }

        oldestCookie.updateAndGet {
            newOldest
        }
    }
}