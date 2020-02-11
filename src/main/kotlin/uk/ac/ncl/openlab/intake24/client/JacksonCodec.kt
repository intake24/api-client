package uk.ac.ncl.openlab.intake24.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

import kotlin.reflect.KClass

class JacksonCodec {
    val mapper = ObjectMapper()

    init {
        mapper
                .registerModule(JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .registerModule(KotlinModule())

    }

    fun <T> encode(obj: T): String {
        return mapper.writeValueAsString(obj)
    }

    inline fun <reified T : Any> decode(string: String): T {
        return mapper.readValue(string)
    }

    fun <T : Any> decode(string: String, klass: KClass<T>): T {
        return mapper.readValue(string, klass.java)
    }
}
