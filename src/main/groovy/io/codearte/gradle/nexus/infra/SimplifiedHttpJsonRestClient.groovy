package io.codearte.gradle.nexus.infra

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import java.util.concurrent.TimeUnit
import javax.annotation.concurrent.ThreadSafe
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.jetbrains.annotations.NotNull

/**
 * Specialized REST client to communicate with Nexus.
 */
@CompileStatic
@Slf4j
@ThreadSafe
class SimplifiedHttpJsonRestClient {

    private final OkHttpClient restClient

    SimplifiedHttpJsonRestClient(OkHttpClient restClient, String username, String password) {
        OkHttpClient.Builder clientBuilder = restClient.newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
                Request.Builder request = chain.request().newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")

                if (username != null && password != null) {
                    request.header("Authorization", Credentials.basic(username, password))
                }

                return chain.proceed(request.build())
            }
        })
        this.restClient = clientBuilder.build()
    }

    Map get(String uri) {
        Request request = new Request.Builder()
            .url(uri)
            .build()
        return sendRequestHandlingErrors(request)
    }

    Map post(String uri, Map content) {
        Request request = new Request.Builder()
            .post(RequestBody.create(JsonOutput.toJson(content), null))
            .url(uri)
            .build()
        return sendRequestHandlingErrors(request)
    }

    private Map sendRequestHandlingErrors(Request request) {
        try {
            return restClient.newCall(request).execute().withCloseable {
                if (!it.successful) {
                    String message = "${it.code()}: ${it.message()}, body: ${it.body().string() ?: '<empty>'}"
                    //TODO: Suppress error message on 404 if waiting for drop?
                    log.warn("${request.method()} request failed. ${message}")
                    throw new NexusHttpResponseException(it.code(), message)
                }
                if (it.body().contentLength() == 0) {
                    return [:]
                } else {
                    (Map) new JsonSlurper().parse(it.body().byteStream())
                }
            }
        } catch(IOException e) {
            throw new NexusStagingException("Could not connect to Nexus.", e)
        }
    }
}
