package com.openmotics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openmotics.exceptions.ApiException;
import com.openmotics.exceptions.AuthenticationException;
import com.openmotics.exceptions.ForbiddenException;
import com.openmotics.exceptions.MaintenanceModeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class OpenMoticsCloudApi {
    private final String VERSION = getClass().getPackage().getImplementationVersion();
    private final String USER_AGENT = String.format("api-java-sdk/%s", VERSION);
    private final CloseableHttpClient httpClient = getHttpClient();
    private ObjectMapper jsonMapper = new ObjectMapper();

    private String username;
    private String password;
    private String hostname = "cloud.openmotics.com";
    private boolean verifyHttps = true;
    private int port = 443;
    private String token = null;
    private List<Installation> installations;
    private Installation currentInstallation;

    public OpenMoticsCloudApi(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public OpenMoticsCloudApi(String username, String password, String hostname) {
        this(username, password);
        this.hostname = hostname;
    }

    public OpenMoticsCloudApi(String username, String password, String hostname, boolean verifyHttps) {
        this(username, password, hostname);
        this.verifyHttps = verifyHttps;
    }

    public OpenMoticsCloudApi(String username, String password, String hostname, int port) {
        this(username, password, hostname);
        this.port = port;
    }

    public OpenMoticsCloudApi(String username, String password, String hostname, boolean verifyHttps, int port) {
        this(username, password, hostname);
        this.verifyHttps = verifyHttps;
        this.port = port;
    }

    //get the url for an action
    private String getUrl(String action) {
        return String.format("https://%s:%s/api/%s", this.hostname, this.port, action);
    }

    //adds the authentication token to the payload
    //TODO: make token part of the HTTP header instead of payload
    private HashMap addTokenToPayload(HashMap<String, Object> payload) {
        if (this.token != null) payload.put("token", this.token);
        return payload;
    }

    //get the version of the openmotics software.
    //returns: 'version': String (a.b.c).
    public String getVersion() throws ApiException, IOException {
        InputStream data = execAction("get_version");
        String version = IOUtils.toString(data, "UTF-8");
        return version;
    }

    //get the version of the openmotics software.
    //returns: 'version': String (a.b.c).
    public List<Installation> getInstallations() throws ApiException, IOException {
        InputStream data = execAction("get_installations");
        List<Installation> response = jsonMapper.readValue(data, new TypeReference<List<Installation>>(){});
        if (response.size()>0){
            installations = response;
            currentInstallation = response.get(0);
        }
        return response;
    }

    //perform login and store the authentication token
    public boolean login() throws ApiException {
        //1. prepare payload
        Map<String, String> payload = new HashMap<>();
        payload.put("username", this.username);
        payload.put("password", this.password);

        //2. perform API request
        String url = getUrl("login");
        try {
            Map<String, Object> response = jsonMapper.readValue(postAsForm(url, payload), Map.class);
            if(!(boolean)response.get("success")) throw new AuthenticationException();
            String token = (String) response.get("token");
            if (token!=null) this.token = token;
            return true;
        } catch (AuthenticationException e) {
            this.token = null;
            log.error(e.getMessage());
            return false;
        } catch (ApiException e) {
            log.error(e.getMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    private InputStream execAction(String action) throws IOException, ApiException {
        Map<String, String> payload = new HashMap<>();
        return execAction(action, payload);
    }

    //execute an action: this method also performs the login if required
    private InputStream execAction(String action, Map<String, String> payload) throws ApiException, IOException {
        if (this.token == null) {
            if(!login()) throw new AuthenticationException();
        };
        if (currentInstallation != null) payload.put("installation_id", currentInstallation.id);
        InputStream response;
        String url = getUrl(action);
        try{
            if (payload != null) payload.put("token", this.token);
            response = postAsForm(url, payload);
        } catch (AuthenticationException aE){
            if(login()){
                if (payload != null) payload.put("token", this.token);
                response = postAsForm(url, payload);
            } else {
                throw new AuthenticationException();
            }
        }
        return response;
    }

    private InputStream postAsJson(String url, Map payload) throws IOException, ApiException {
        //1. prepare http request
        HttpPost postRequest = new HttpPost(url);
        postRequest.addHeader(HttpHeaders.ACCEPT_ENCODING, String.valueOf(ContentType.APPLICATION_JSON));
        postRequest.addHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(ContentType.APPLICATION_JSON));

        //2. set encoded payload
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(payload);
        postRequest.setEntity(new StringEntity(json, String.valueOf(ContentType.APPLICATION_JSON)));

        //3. perform POST
        HttpResponse response = postRaw(postRequest);
        return response.getEntity().getContent();
    }

    private InputStream postAsForm(String url, Map<String, String> payload) throws IOException, ApiException {
        //1. prepare http request
        HttpPost postRequest = new HttpPost(url);
        postRequest.addHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(ContentType.APPLICATION_FORM_URLENCODED));

        //2. set encoded payload
        List<NameValuePair> payloadForm = new ArrayList<>();
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            payloadForm.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(payloadForm, Consts.UTF_8);
        postRequest.setEntity(entity);

        //3. perform POST
        HttpResponse response = postRaw(postRequest);
        return response.getEntity().getContent();
    }

    private HttpResponse postRaw(HttpPost postRequest) throws ApiException, IOException {
        //1. make request
        HttpResponse response = httpClient.execute(postRequest);
        int statusCode = response.getStatusLine().getStatusCode();
        switch (statusCode) {
            case 401:
                throw new AuthenticationException();
            case 503:
                throw new MaintenanceModeException();
            case 403:
                throw new ForbiddenException();
        }
        return response;
    }

    private CloseableHttpClient getHttpClient() {
        // Trust own CA and all self-signed certs
        SSLContext sslcontext = null;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            File keystoreFile = new File(classLoader.getResource("openmotics-certs.key").getFile());

            sslcontext = SSLContexts.custom()
                    .loadTrustMaterial(keystoreFile, "openmotics".toCharArray(),
                            new TrustSelfSignedStrategy())
                    .build();
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
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        return HttpClientBuilder.create()
                .setUserAgent(USER_AGENT)
                .setSSLSocketFactory(sslsf)
                .build();
    }

}