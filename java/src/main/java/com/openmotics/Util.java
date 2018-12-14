package com.openmotics;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by svanscho on 13/12/2018.
 */
@Slf4j
public class Util {

    private static final String KEYSTORE_FILENAME = "letsencrypt.key";
    private static final String KEYSTORE_PWD = "openmotics";

    public static CloseableHttpClient getHttpClient(String userAgent, Boolean verifyHttps) {
        log.debug("getting new http client with verifyHttps={}", verifyHttps);
        SSLConnectionSocketFactory sslsf = getSSLConnectionSocketFactory(verifyHttps);

        RegistryBuilder<ConnectionSocketFactory> schemeRegistry = RegistryBuilder.create();
        schemeRegistry.register("http", PlainConnectionSocketFactory.getSocketFactory());
        schemeRegistry.register("https", sslsf);

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(schemeRegistry.build());
        cm.setMaxTotal(10);                    // Set max total connection to 10
        cm.setDefaultMaxPerRoute(5);           // Set default max connection per route to 5

        return HttpClientBuilder.create()
                .setConnectionManager(cm)
                .setUserAgent(userAgent)
                .build();
    }

    private static SSLConnectionSocketFactory getSSLConnectionSocketFactory(Boolean verifyHttps) {
        try {
            // Trust own CA and all self-signed certs
            if (verifyHttps) { //TODO: perform java version check
                // older versions of java don't have the CA certificate (Let's encrypt) for the openmotics cloud API built-in
                // a custom keystore was created for that purpose so let's import it
                // https://community.letsencrypt.org/t/will-the-cross-root-cover-trust-by-the-default-list-in-the-jdk-jre/134/13
                KeyStore ks=KeyStore.getInstance("JKS");
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                ks.load(classLoader.getResourceAsStream(KEYSTORE_FILENAME),KEYSTORE_PWD.toCharArray());
                log.debug("loading custom keystore for let's encrypt CA authority");
                SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(ks,
                        new TrustSelfSignedStrategy())
                        .build();
                // Allow TLSv1 protocol only
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                        sslContext,
                        new String[]{"TLSv1"},
                        null,
                        SSLConnectionSocketFactory.getDefaultHostnameVerifier());
                return sslsf;
            } else {
                // Trust all certs
                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
                };
                // Install the all-trusting trust manager
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                        sslContext,
                        NoopHostnameVerifier.INSTANCE);
                return sslsf;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return SSLConnectionSocketFactory.getSystemSocketFactory();

    }
}
