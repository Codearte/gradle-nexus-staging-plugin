package io.codearte.gradle.nexus.infra

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.MediaType
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
class SimplifiedHttpJsonRestClient {

    private static final MediaType JSON = MediaType.parse("application/json")

    private enum RequestType {
        GET, POST
    }

    private final OkHttpClient restClient

    SimplifiedHttpJsonRestClient(OkHttpClient restClient, String username, String password) {
        OkHttpClient.Builder clientBuilder = restClient.newBuilder()
        if (username != null) {
            clientBuilder.addInterceptor(new Interceptor() {
                @Override
                Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
                    Request.Builder request = chain.request().newBuilder()
                        .addHeader("Authorization", Credentials.basic(username, password))

                    return chain.proceed(request.build())
                }
            })
        }
        this.restClient = clientBuilder.build()
    }

    Map get(String uri) {
        Request request = new Request.Builder()
            .url(uri)
            .build()
        return sendRequestHandlingErrors(request, RequestType.GET)
    }

    Map post(String uri, Map content) {
        Request request = new Request.Builder()
            .method("POST", RequestBody.create(JsonOutput.toJson(content), JSON))
            .url(uri)
            .build()
        return sendRequestHandlingErrors(request, RequestType.POST)
    }

    private Map sendRequestHandlingErrors(Request request, RequestType requestTypeName) {
        try {
            return restClient.newCall(request).execute().withCloseable {
                if (!it.successful) {
                    String message = "${it.code()}: ${it.message()}, body: ${it.body().string() ?: '<empty>'}"
                    //TODO: Suppress error message on 404 if waiting for drop?
                    log.warn("$requestTypeName request failed. ${message}")
                    throw new NexusHttpResponseException(it.code(), message)
                }
                (Map) new JsonSlurper().parse(it.body().byteStream())
            }
        } catch(IOException e) {
            throw new NexusStagingException("Could not connect to Nexus.", e)
        }
    }
}
