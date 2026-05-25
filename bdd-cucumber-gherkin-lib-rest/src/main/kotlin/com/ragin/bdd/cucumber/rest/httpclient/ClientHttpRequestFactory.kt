package com.ragin.bdd.cucumber.rest.httpclient

import com.ragin.bdd.cucumber.config.BddProperties
import com.ragin.bdd.cucumber.core.ScenarioStateContext
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier
import org.apache.hc.client5.http.ssl.TlsSocketStrategy
import org.apache.hc.client5.http.ssl.TrustAllStrategy
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.ssl.SSLContexts
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import java.net.Proxy

open class ClientHttpRequestFactory(
    private val bddProperties: BddProperties
) {
    fun createRequestFactory(): ClientHttpRequestFactory {
        return HttpComponentsClientHttpRequestFactory(
            createHttpClient()
        )
    }

    protected fun createHttpClient(proxyHost: String? = null, proxyPort: Int? = null): CloseableHttpClient {
        val httpClientBuilder = HttpClientBuilder.create()
        httpClientBuilder.disableRedirectHandling()

        if (bddProperties.ssl != null && bddProperties.ssl!!.disableCheck) {
            val sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                .build()
            val tlsStrategy = ClientTlsStrategyBuilder.create()
                .setSslContext(sslContext)
                .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .buildClassic()
            val connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setTlsSocketStrategy(tlsStrategy as TlsSocketStrategy)
                .build()
            httpClientBuilder.setConnectionManager(connectionManager)
        }

        var proxyHostUsed: String? = null
        var proxyPortUsed: Int? = null
        if (hasProxyConfigured()) {
            proxyHostUsed = bddProperties.proxy!!.host
            proxyPortUsed = bddProperties.proxy!!.port
        }

        if (proxyHost != null && proxyPort != null) {
            proxyHostUsed = proxyHost
            proxyPortUsed = proxyPort
        }

        val proxy = proxyOrNull(proxyHost = proxyHostUsed, proxyPort = proxyPortUsed)
        if (proxy != null) {
            httpClientBuilder.setProxy(
                HttpHost(
                    Proxy.Type.HTTP.name,
                    proxy.first,
                    proxy.second,
                )
            )
        }
        return httpClientBuilder.build()
    }


    private fun proxyOrNull(proxyHost: String?, proxyPort: Int?): Pair<String, Int>? {
        return if (ScenarioStateContext.dynamicProxyHost != null && ScenarioStateContext.dynamicProxyPort != null) {
            Pair(first = ScenarioStateContext.dynamicProxyHost!!, second = ScenarioStateContext.dynamicProxyPort!!)
        } else if (proxyHost != null && proxyPort != null) {
            Pair(first = proxyHost, second = proxyPort)
        } else {
            null
        }
    }

    private fun hasProxyConfigured(): Boolean {
        return bddProperties.proxy != null
                && bddProperties.proxy!!.host.isNotEmpty()
                && bddProperties.proxy!!.port != null && bddProperties.proxy!!.port!! > 0
    }
}
