package com.bitdance.giveortake;

import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * The backend server's API requires SSL, but the certificate is self-signed.
 * This code ensures that Android doesn't choke on the self-signed cert.
 */
public class SSLConnectionHelper {
    private static final String TAG = "SSLConnectionHelper";

    private static boolean trustManagerInstalled = false;

    private static HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return s.equals(Constants.API_HOSTNAME);
        }
    };

    public static HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    public static void trustAllHosts() {
        if (!trustManagerInstalled) {
            doTrustAllHosts();
            trustManagerInstalled = true;
        }
    }

    private static void doTrustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{getTrustManager()};

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            Log.e(TAG, "Error while installing trust manager", e);
        }
    }

    private static X509TrustManager getTrustManager() {
        return new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        };
    }

    private static X509HostnameVerifier getX509HostnameVerifier() {
        return new X509HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return s.equals(Constants.API_HOSTNAME);
            }

            @Override
            public void verify(String s, SSLSocket sslSocket) throws IOException {
                if (!s.equals(Constants.API_HOSTNAME)) {
                    throw new IOException("Hostname did not match: " + s);
                }
            }

            @Override
            public void verify(String s, X509Certificate x509Certificate) throws SSLException {
                if (!s.equals(Constants.API_HOSTNAME)) {
                    throw new SSLException("Hostname did not match: " + s);
                }
            }

            @Override
            public void verify(String s, String[] strings, String[] strings2) throws SSLException {
                if (!s.equals(Constants.API_HOSTNAME)) {
                    throw new SSLException("Hostname did not match: " + s);
                }
            }
        };
    }


    public static HttpClient sslClient(HttpClient client) {
        try {
            X509TrustManager tm = getTrustManager();
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
            ssf.setHostnameVerifier(getX509HostnameVerifier());
            ClientConnectionManager ccm = client.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
            return new DefaultHttpClient(ccm, client.getParams());
        } catch (Exception ex) {
            return null;
        }
    }

    public static class MySSLSocketFactory extends SSLSocketFactory {
        SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(SSLContext context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
            super(null);
            sslContext = context;
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }
}
