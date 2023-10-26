package io.appwrite.extensions

import io.appwrite.json.toJsonElement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

val json = Json {
    ignoreUnknownKeys = true
}

fun Any.toJson(): String {
    return json.encodeToString(toJsonElement())
}

fun <T> String.fromJson(clazz: Class<T>): T {
    return try {
        val data = json.decodeFromString(serializer(clazz), this) as T
        println("toJson => ${data}")
        data
    } catch (e: Exception) {
        e.printStackTrace()
        val data = json.parseToJsonElement(this) as T
        println("exception toJson => ${data}")
        data
    }
}

inline fun <reified T> String.fromJson(): T =
    json.decodeFromString(this)

fun <T> Any.jsonCast(to: Class<T>): T =
    toJson().fromJson(to)

inline fun <reified T> Any.jsonCast(): T =
    toJson().fromJson(T::class.java)

fun <T> Any.tryJsonCast(to: Class<T>): T? = try {
    toJson().fromJson(to)
} catch (ex: Exception) {
    ex.printStackTrace()
    null
}

inline fun <reified T> Any.tryJsonCast(): T? = try {
    toJson().fromJson(T::class.java)
} catch (ex: Exception) {
    ex.printStackTrace()
    null
}
