package com.achain.conf;

import org.apache.http.Consts;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.CodingErrorAction;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author qiangkezhen
 */
@Configuration
public class HttpConfig {

    private CloseableHttpClient client;

    @PostConstruct
    private void init() {

        int timeout5MinsInMillis = 5 * 60 * 1000;
        RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                                                   .setSocketTimeout(timeout5MinsInMillis)
                                                   .setConnectionRequestTimeout(timeout5MinsInMillis)
                                                   .setConnectTimeout(timeout5MinsInMillis)
                                                   .build();
        ConnectionConfig connectionConfig = ConnectionConfig.copy(ConnectionConfig.DEFAULT)
                                                            .setMalformedInputAction(CodingErrorAction.IGNORE)
                                                            .setUnmappableInputAction(CodingErrorAction.IGNORE)
                                                            .setCharset(Consts.UTF_8)
                                                            .build();
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager =
            new PoolingHttpClientConnectionManager();

        poolingHttpClientConnectionManager.setMaxTotal(1000);
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(500);
        client = HttpClients.custom()
                            .setConnectionManager(poolingHttpClientConnectionManager)
                            .setConnectionManagerShared(false)
                            .evictIdleConnections(60, TimeUnit.SECONDS)
                            .evictExpiredConnections()
                            .setDefaultRequestConfig(requestConfig)
                            .setDefaultConnectionConfig(connectionConfig)
                            .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
                            .useSystemProperties()
                            .build();
    }

    @PreDestroy
    private void destroy() throws IOException {
        client.close();
    }

    @Bean
    public CloseableHttpClient getClient() {
        return client;
    }
}
