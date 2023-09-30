package io.appwrite

import android.content.Context
import android.content.pm.PackageManager
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.appwrite.cookies.stores.AcceptAllCookiesStorage
import io.appwrite.exceptions.AppwriteException
import io.appwrite.json.PreciseNumberAdapter
import io.appwrite.models.InputFile
import io.appwrite.models.UploadProgress
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormPart
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.RandomAccessFile
import kotlin.coroutines.CoroutineContext

class Client @JvmOverloads constructor(
    context: Context,
    var endPoint: String = "https://HOSTNAME/v1",
    var endPointRealtime: String? = null,
    private var selfSigned: Boolean = false
) : CoroutineScope {

    companion object {
        const val CHUNK_SIZE = 5 * 1024 * 1024; // 5MB
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private val job = Job()

    private val gson = GsonBuilder().registerTypeAdapter(
        object : TypeToken<Map<String, Any>>() {}.type,
        PreciseNumberAdapter()
    ).create()

    lateinit var http: HttpClient

    private val headers: MutableMap<String, String>

    val config: MutableMap<String, String>

    val cookieJar = AcceptAllCookiesStorage(context, "myCookie")

    private val appVersion by lazy {
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return@lazy pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            return@lazy ""
        }
    }

    init {
        headers = mutableMapOf(
            "content-type" to "application/json",
            "origin" to "appwrite-android://${context.packageName}",
            "user-agent" to "${context.packageName}/${appVersion}, ${System.getProperty("http.agent")}",
            "x-sdk-name" to "Android",
            "x-sdk-platform" to "client",
            "x-sdk-language" to "android",
            "x-sdk-version" to "4.0.0",
            "x-appwrite-response-format" to "1.4.0"
        )
        config = mutableMapOf()

        setSelfSigned(selfSigned)
    }

    /**
     * Set Project
     *
     * Your project ID
     *
     * @param {string} project
     *
     * @return this
     */
    fun setProject(value: String): Client {
        config["project"] = value
        addHeader("x-appwrite-project", value)
        return this
    }

    /**
     * Set JWT
     *
     * Your secret JSON Web Token
     *
     * @param {string} jwt
     *
     * @return this
     */
    fun setJWT(value: String): Client {
        config["jWT"] = value
        addHeader("x-appwrite-jwt", value)
        return this
    }

    /**
     * Set Locale
     *
     * @param {string} locale
     *
     * @return this
     */
    fun setLocale(value: String): Client {
        config["locale"] = value
        addHeader("x-appwrite-locale", value)
        return this
    }

    /**
     * Set self Signed
     *
     * @param status
     *
     * @return this
     */
    fun setSelfSigned(status: Boolean): Client {
        selfSigned = status

        if (!selfSigned) {
            http = HttpClient {
                install(HttpCookies) {
                    storage = cookieJar
                }
                install(WebSockets) {

                }
                install(ContentNegotiation) {
                    gson()
                }
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            println(message)
                        }
                    }
                    level = LogLevel.ALL
                }
                expectSuccess = true
            }
            return this
        }

        return this
    }

    /**
     * Set endpoint and realtime endpoint.
     *
     * @param endpoint
     *
     * @return this
     */
    fun setEndpoint(endPoint: String): Client {
        this.endPoint = endPoint

        if (this.endPointRealtime == null && endPoint.startsWith("http")) {
            this.endPointRealtime = endPoint.replaceFirst("http", "ws")
        }

        return this
    }

    /**
     * Set realtime endpoint
     *
     * @param endpoint
     *
     * @return this
     */
    fun setEndpointRealtime(endPoint: String): Client {
        this.endPointRealtime = endPoint
        return this
    }

    /**
     * Add Header
     *
     * @param key
     * @param value
     *
     * @return this
     */
    fun addHeader(key: String, value: String): Client {
        headers[key] = value
        return this
    }

    /**
     * Send the HTTP request
     *
     * @param method
     * @param path
     * @param headers
     * @param params
     *
     * @return [T]
     */
    @Throws(AppwriteException::class)
    suspend fun <T> call(
        method: String,
        path: String,
        headers: Map<String, String> = mapOf(),
        params: Map<String, Any?> = mapOf(),
        responseType: Class<T>,
        converter: ((Any) -> T)? = null
    ): T {
        val filteredParams = params.filterValues { it != null }

        val httpBuilder = HttpRequestBuilder()
        httpBuilder.url(endPoint + path)
        httpBuilder.method = HttpMethod(method)

        httpBuilder.headers {
            this@Client.headers.plus(headers).forEach { (key, value) ->
                append(key, value)
            }
        }

        if ("GET" == method) {
            filteredParams.forEach {
                when (it.value) {
                    null -> {
                        return@forEach
                    }

                    is List<*> -> {
                        val list = it.value as List<*>
                        for (index in list.indices) {
                            httpBuilder.parameter(
                                "${it.key}[]",
                                list[index].toString()
                            )
                        }
                    }

                    else -> {
                        httpBuilder.parameter(it.key, it.value.toString())
                    }
                }
            }

            return awaitResponse(httpBuilder, responseType, converter)
        }

        if (MultipartBody.FORM.toString() == headers["content-type"]) {
            httpBuilder.setBody(
                MultiPartFormDataContent(
                    formData {
                        filteredParams.forEach {
                            when {
                                it.key == "file" -> {
                                    //add file
                                    append(it.value as FormPart<*>)
                                }

                                it.value is List<*> -> {
                                    val list = it.value as List<*>
                                    for (index in list.indices) {
                                        append("${it.key}[]", list[index].toString())
                                    }
                                }

                                else -> {
                                    append(it.key, it.value.toString())
                                }
                            }
                        }
                    }
                )
            )
        } else {
            val body = gson.toJson(filteredParams)
            httpBuilder.apply {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }

        return awaitResponse(httpBuilder, responseType, converter)
    }

    /**
     * Upload a file in chunks
     *
     * @param path
     * @param headers
     * @param params
     *
     * @return [T]
     */
    @Throws(AppwriteException::class)
    suspend fun <T> chunkedUpload(
        path: String,
        headers: MutableMap<String, String>,
        params: MutableMap<String, Any?>,
        responseType: Class<T>,
        converter: ((Any) -> T),
        paramName: String,
        idParamName: String? = null,
        onProgress: ((UploadProgress) -> Unit)? = null,
    ): T {
        var file: RandomAccessFile? = null
        val input = params[paramName] as InputFile
        val size: Long = when (input.sourceType) {
            "path", "file" -> {
                file = RandomAccessFile(input.path, "r")
                file.length()
            }

            "bytes" -> {
                (input.data as ByteArray).size.toLong()
            }

            else -> throw UnsupportedOperationException()
        }

        if (size < CHUNK_SIZE) {
            val data = when (input.sourceType) {
                "file", "path" -> File(input.path).asRequestBody()
                "bytes" -> (input.data as ByteArray).toRequestBody(input.mimeType.toMediaType())
                else -> throw UnsupportedOperationException()
            }
            params[paramName] = MultipartBody.Part.createFormData(
                paramName,
                input.filename,
                data
            )
            return call(
                method = "POST",
                path,
                headers,
                params,
                responseType,
                converter
            )
        }

        val buffer = ByteArray(CHUNK_SIZE)
        var offset = 0L
        var result: Map<*, *>? = null

        if (idParamName?.isNotEmpty() == true && params[idParamName] != "unique()") {
            // Make a request to check if a file already exists
            val current = call(
                method = "GET",
                path = "$path/${params[idParamName]}",
                headers = headers,
                params = emptyMap(),
                responseType = Map::class.java,
            )
            val chunksUploaded = current["chunksUploaded"] as Long
            offset = chunksUploaded * CHUNK_SIZE
        }

        while (offset < size) {
            when (input.sourceType) {
                "file", "path" -> {
                    file!!.seek(offset)
                    file!!.read(buffer)
                }

                "bytes" -> {
                    val end = if (offset + CHUNK_SIZE < size) {
                        offset + CHUNK_SIZE - 1
                    } else {
                        size - 1
                    }
                    (input.data as ByteArray).copyInto(
                        buffer,
                        startIndex = offset.toInt(),
                        endIndex = end.toInt()
                    )
                }

                else -> throw UnsupportedOperationException()
            }

            params[paramName] = MultipartBody.Part.createFormData(
                paramName,
                input.filename,
                buffer.toRequestBody()
            )

            headers["Content-Range"] =
                "bytes $offset-${((offset + CHUNK_SIZE) - 1).coerceAtMost(size - 1)}/$size"

            result = call(
                method = "POST",
                path,
                headers,
                params,
                responseType = Map::class.java
            )

            offset += CHUNK_SIZE
            headers["x-appwrite-id"] = result!!["\$id"].toString()
            onProgress?.invoke(
                UploadProgress(
                    id = result!!["\$id"].toString(),
                    progress = offset.coerceAtMost(size).toDouble() / size * 100,
                    sizeUploaded = offset.coerceAtMost(size),
                    chunksTotal = result!!["chunksTotal"].toString().toInt(),
                    chunksUploaded = result!!["chunksUploaded"].toString().toInt(),
                )
            )
        }

        return converter(result as Map<String, Any>)
    }

    /**
     * Await Response
     *
     * @param request
     * @param responseType
     * @param converter
     *
     * @return [T]
     */
    @Throws(AppwriteException::class)
    private suspend fun <T> awaitResponse(
        request: HttpRequestBuilder,
        responseType: Class<T>,
        converter: ((Any) -> T)? = null
    ): T {
        return try {
            val response: HttpResponse = http.request(builder = request)

            when (responseType) {
                Boolean::class.java -> {
                    return true as T
                }

                ByteArray::class.java -> {
                    return response.readBytes() as T
                }
            }

            val body = response.bodyAsText()
            if (body.isEmpty()) {
                return true as T
            }

            val map = gson.fromJson<Any>(
                body,
                object : TypeToken<Any>() {}.type
            )
            return converter?.invoke(map) ?: map as T
        } catch (e: Exception) {
            e.printStackTrace()
            return null as T
        }
    }
}