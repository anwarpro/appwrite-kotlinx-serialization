package io.appwrite.extensions

import io.appwrite.json.toJsonElement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

val json = Json

fun Any.toJson(): String {
    println("toJson => ${this.toJsonElement()}")
    return json.encodeToString(this.toJsonElement())
}

fun <T> String.fromJson(clazz: Class<T>): T {
    println("toJson => $this")
    return try {
        json.decodeFromString(serializer(clazz), this) as T
    } catch (e: Exception) {
        e.printStackTrace()
        json.parseToJsonElement(this) as T
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
