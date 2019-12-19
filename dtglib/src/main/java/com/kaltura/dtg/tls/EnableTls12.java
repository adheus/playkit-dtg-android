/*
 * Copyright (c) 2017 Vimeo  (https://vimeo.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.kaltura.dtg.tls;

import android.os.Build;

import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Used to enable TLS v1.2 when creating SSLSockets.
 * <p/>
 * For some reason, android supports TLS v1.2 from API 16, but enables it by
 * default only from API 20.
 * @link https://developer.android.com/reference/javax/net/ssl/SSLSocket.html
 * @see SSLSocketFactory
 */
public class EnableTls12 {
    private static SSLContext sslContext;
    private static TrustManager[] trustManagers;

    public static HttpURLConnection onPreApi22(HttpURLConnection connection) {
        if (connection instanceof HttpsURLConnection && Build.VERSION.SDK_INT < 22) {
            try {
                ((HttpsURLConnection)connection).setSSLSocketFactory(getSocketFactory());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return connection;
    }

    private static SSLContext getSslContext() throws NoSuchAlgorithmException, KeyManagementException {
        if (sslContext == null) {
            sslContext = SSLContext.getInstance("TLSv1.2");
            try {
                sslContext.init(null, getTrustManagers(), null);
            } catch (Exception ex) {
                ex.printStackTrace();
                sslContext.init(null, null, null);
            }
        }
        return sslContext;
    }

    private static TrustManager[] getTrustManagers() throws NoSuchAlgorithmException, KeyStoreException {
        if (trustManagers == null) {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            // If the default trust managers isn't a X509TrustManager, we keep trustManagers null [RT]
            if (trustManagers.length == 1 && trustManagers[0] instanceof X509TrustManager) {
                EnableTls12.trustManagers = trustManagers;
            }
        }
        return trustManagers;
    }

    private static SSLSocketFactory getSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        return new Tls12SocketFactory(getSslContext().getSocketFactory());
    }

}
