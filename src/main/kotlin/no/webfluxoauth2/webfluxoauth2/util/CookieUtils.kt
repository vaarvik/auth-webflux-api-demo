package no.webfluxoauth2.webfluxoauth2.util

import org.springframework.http.HttpCookie
import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.util.SerializationUtils
import java.time.Duration
import java.util.*
import javax.servlet.http.Cookie

object CookieUtils {
    @JvmStatic
    fun getCookie(request: ServerHttpRequest, name: String): HttpCookie? {
        val cookies = request.cookies
        return cookies.getFirst(name)
    }

    @JvmStatic
    fun addCookie(response: ServerHttpResponse, name: String, value: String, maxAge: Duration? = Duration.ofHours(2)) {
        val cookie = ResponseCookie.from(name, value)
        cookie.path("/")
        cookie.httpOnly(true)
        cookie.secure(true)
        cookie.maxAge(maxAge!!)
        response.addCookie(cookie.build())
    }

    @JvmStatic
    fun deleteCookie(request: ServerHttpRequest, response: ServerHttpResponse, name: String) {
        val cookies = request.cookies
        cookies[name]?.forEach {
            val cookie = ResponseCookie.from(it.name, "")
            cookie.path("/")
            cookie.maxAge(0)
            response.addCookie(cookie.build())
        }
    }

    @JvmStatic
    fun serialize(`object`: Any?): String {
        return Base64.getUrlEncoder()
            .encodeToString(SerializationUtils.serialize(`object`))
    }

    @JvmStatic
    fun <T> deserialize(cookie: Cookie, cls: Class<T>): T {
        return cls.cast(
            SerializationUtils.deserialize(
                Base64.getUrlDecoder().decode(cookie.value)
            )
        )
    }
}