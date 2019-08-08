package weevent.robust.sdk.client;


import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import com.webank.weevent.sdk.jsonrpc.IBrokerRpc;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;


/**
 * JsonRpc client for WeEvent Broker.
 * This is client sdk in java.
 * Provide pure json rpc2.0 protocol for other language.
 * //@formatter:off
 * <pre>
 * try {
 *     String url = "https://localhost/weevent/jsonrpc";
 *     if (args.length == 1) {
 *         url = args[0];
 *     }
 *     IBrokerRpc iBrokerRpc = JsonRpcClient.build(url);
 * } catch (BrokerException | MalformedURLException | NoSuchAlgorithmException | KeyManagementException e) {
 *     e.printStackTrace();
 * }
 * <pre>
 * //@formatter:on
 * @author matthewliu
 * @since 2018/11/22
 */
@Slf4j
public class JsonRpcClient {
    public static SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        // impl X509TrustManager interface，not verify certificate
        X509TrustManager x509TrustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s){
                          log.info("x509Certificates");
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                           log.info("x509Certificates");
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
        return sslContext;
    }

    /**
     * Get a handler of IBrokerRpc.
     *
     * @param url the url
     * @return com.webank.weevent.client.IBrokerRpc
     * @throws MalformedURLException invalid url format
     */
    public static IBrokerRpc build(String url) throws MalformedURLException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = getSSLContext();

        JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(url));
        client.setSslContext(sslContext);
        //not verify HostName
        client.setHostNameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname,
                                  SSLSession sslsession) {
                return true;
            }
        });
        return ProxyUtil.createClientProxy(client.getClass().getClassLoader(), IBrokerRpc.class, client);
    }
}
