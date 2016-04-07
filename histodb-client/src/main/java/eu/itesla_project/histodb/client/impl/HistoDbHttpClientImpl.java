/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl;

import eu.itesla_project.commons.io.ForwardingInputStream;
import eu.itesla_project.modules.histo.cache.HistoDbCache;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;


/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HistoDbHttpClientImpl implements HistoDbHttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoDbHttpClientImpl.class);

    private final HistoDbCache cache;

    private CloseableHttpClient httpClient;

    HistoDbHttpClientImpl(HistoDbCache cache) {
        this.cache = cache;
    }

    @Override
    public HistoDbCache getCache() {
        return cache;
    }

    private synchronized CloseableHttpClient getHttpclient(HistoDbConfig config) {
        if (httpClient == null) {
            try {
                ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }};
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                LayeredConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
                Registry<ConnectionSocketFactory> r = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", plainsf)
                        .register("https", sslsf)
                        .build();
                PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(r);
                cm.setDefaultMaxPerRoute(10);
                cm.setMaxTotal(20);
                HttpClientBuilder httpClientBuilder = HttpClients.custom()
                        .setConnectionManager(cm);
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(new AuthScope(new HttpHost(config.getConnectionParameters().getHost(), config.getConnectionParameters().getPort())),
                                                   new UsernamePasswordCredentials(config.getConnectionParameters().getUserName(), config.getConnectionParameters().getPassword()));
                if (config.getProxyParameters() != null) {
                    HttpHost proxy = new HttpHost(config.getProxyParameters().getHost(), config.getProxyParameters().getPort());
                    credentialsProvider.setCredentials(new AuthScope(proxy),
                                                       new UsernamePasswordCredentials(config.getProxyParameters().getUserName(), config.getProxyParameters().getPassword()));
                    httpClientBuilder.setProxy(proxy);
                }
                httpClient = httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                        .build();
            } catch (KeyManagementException|NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        return httpClient;
    }

    private InputStream httpRequest(HttpUriRequest request, HistoDbUrl url) throws IOException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Querying histo DB " + url.format());
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Querying histo DB " + url.prettyFormat());
        }

        long start = System.currentTimeMillis();

        HttpContext context = HttpClientContext.create();
        CloseableHttpResponse response = getHttpclient(url.getConfig()).execute(request, context);
        StatusLine statusLine = response.getStatusLine();

        switch (statusLine.getStatusCode()) {
            case 200:
                long duration = System.currentTimeMillis() - start;
                LOGGER.debug("Query done in {} ms", duration);
                return new ForwardingInputStream<InputStream>(response.getEntity().getContent()) {
                    @Override
                    public void close() throws IOException {
                        super.close();
                        EntityUtils.consume(response.getEntity());
                        response.close();
                    }
                };

            case 202:
                EntityUtils.consume(response.getEntity());
                response.close();
                return null;

            default:
                throw new RuntimeException("Query failed with status " + statusLine + " : " + url);
        }
    }

    private InputStream cachedHttpRequest(HttpUriRequest request, HistoDbUrl url) throws IOException {
        InputStream is = null;

        if (cache != null) {
            is = cache.getData(url.format());
        }

        if (is != null) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Using cached data for query " + url.format());
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Using cached data for query " + url.prettyFormat());
            }
        } else {
            is = httpRequest(request, url);

            if (cache != null && is != null) {
                OutputStream os = cache.putData(url.format());
                is = new TeeInputStream(is, os, true);
            }
        }

        return is;
    }

    @Override
    public InputStream getHttpRequest(HistoDbUrl url) throws IOException {
        HttpRequestBase httpReq;
        String urlStr = url.format();
        if (urlStr.length() > 255) { // arbitrary very low value
            // replace by a post because of url size limit
            HttpPost httpPost = new HttpPost(new HistoDbUrl(url.getConfig(), url.getPath(), Collections.emptyMap()).format());
            List<BasicNameValuePair> parameters = url.getQuery().entrySet().stream().map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())).collect(Collectors.toList());
            httpPost.setEntity(new UrlEncodedFormEntity(parameters, StandardCharsets.UTF_8));
            httpReq = httpPost;
        } else {
            httpReq = new HttpGet(urlStr);
        }
        return cachedHttpRequest(httpReq, url);
    }

    @Override
    public InputStream postHttpRequest(HistoDbUrl url, byte[] content) throws IOException {
        HttpPost httppost = new HttpPost(url.format());
        httppost.setEntity(new ByteArrayEntity(content));
        return cachedHttpRequest(httppost, url);
    }

    @Override
    public InputStream deleteHttpRequest(HistoDbUrl url) throws IOException {
        HttpDelete httpdel = new HttpDelete(url.format());
        return cachedHttpRequest(httpdel, url);
    }

    @Override
    public void close() throws Exception {
        if (httpClient != null) {
            httpClient.close();
        }
        if (cache != null) {
            cache.close();
        }
    }

}
