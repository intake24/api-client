package uk.ac.ncl.openlab.intake24.client

import org.http4k.client.OkHttp
import org.http4k.core.then
import kotlin.test.Test

class ClientTest {
    @Test
    fun clientTest() {

        val codec = JacksonCodec()
        val auth = Intake24AuthFilter("http://192.168.0.20:9001", OkHttp(), codec, EmailCredentials("test@test.test", "test123"));

        val handler = ThrowOnHttpErrorFilter().then(auth.then(OkHttp()))

        val surveys = Surveys("http://192.168.0.20:9001", handler, codec)

        val response = surveys.getSubmissions("demo", null, null, null, 0, 100);

        println(response)

    }
}
