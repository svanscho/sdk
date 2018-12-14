package com.openmotics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openmotics.exceptions.*;
import com.openmotics.model.Installation;
import com.openmotics.model.OutputStatus;
import com.openmotics.model.Status;
import com.openmotics.model.Version;
import com.openmotics.responses.ModulesResponse;
import com.openmotics.responses.OutputStatusResponse;
import com.openmotics.responses.ThermostatStatusResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A client that allows interaction with the openmotics cloud API
 *
 * @author  Sander Van Schoote
 */

@Slf4j
public class OpenmoticsClient {

    private static final String DEFAULT_HOST = "https://cloud.openmotics.com";
    private static final Integer DEFAULT_PORT = 443;

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private CloseableHttpClient httpClient;

    private final String SDK_VERSION = getClass().getPackage().getImplementationVersion();
    private final String USER_AGENT = String.format("api-java-sdk/%s", SDK_VERSION);

    @Getter
    private String username;
    @Getter
    private String password;
    @Getter
    private String hostname;
    @Getter
    private Boolean verifyHttps;
    @Getter
    private Integer port;

    @Getter
    private String token;
    @Setter
    private List<Installation> installations;
    @Getter
    private Installation activeInstallation;

    public OpenmoticsClient(String username, String password, String hostname, Integer port, Boolean verifyHttps) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.port = port;
        this.verifyHttps = verifyHttps;
        this.httpClient = Util.getHttpClient(USER_AGENT, verifyHttps);
    }

    public OpenmoticsClient(String username, String password) {
        this(username, password, true);
    }

    public OpenmoticsClient(String username, String password, Boolean verifyHttps) {
        this(username, password, DEFAULT_HOST, DEFAULT_PORT, verifyHttps);
    }

    public OpenmoticsClient(String username, String password, String hostname, Integer port) {
        this(username, password, hostname, port, true);
    }


    /**
     * Performs a login request on the API. If successful, an auth token is retrieved and stored.
     * @return boolean signalling whether logging in was successful or not.
     * @exception ClientException if there was an internal client error.
     * @exception ApiException if there was a problem getting an auth token from the API.
     */
    public boolean login() throws ApiException, ClientException {
        //1. prepare payload
        Map<String, String> payload = new HashMap<>();
        payload.put("username", this.username);
        payload.put("password", this.password);

        //2. perform API request
        InputStream data = execAction(Action.LOGIN, payload);
        try {
            Map<String, Object> response = jsonMapper.readValue(data, Map.class);
            if (!(boolean) response.get("success")) throw new AuthenticationException();
            String token = (String) response.get("token");
            if (token != null) this.token = token;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClientException("failed to parse login response");
        }
    }

    //get the status of the master
    //returns 'time' (HH:MM), 'date' (DD:MM:YYYY), 'mode', 'version' (a.b.c) and 'hw_version'
    public Status getStatus() throws ClientException, ApiException {
        InputStream data = execAction(Action.STATUS);
        try {
            return jsonMapper.readValue(data, Status.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClientException("failed to parse status response");
        }
    }

    //get the status of the master
    //returns 'time' (HH:MM), 'date' (DD:MM:YYYY), 'mode', 'version' (a.b.c) and 'hw_version'
    public List<OutputStatus> getOutputStatus() throws ClientException, ApiException {
        InputStream data = execAction(Action.OUTPUT_STATUS);
        try {
            return jsonMapper.readValue(data, OutputStatusResponse.class).getStatus();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClientException("failed to parse output status response");
        }
    }

    //get the status of the thermostats
    //returns: global status information about the thermostats:
    // 'thermostats_on'
    // 'automatic'
    // 'setpoint'
    // 'status': a list with status information for all thermostats:
    // 'id'
    // 'act'
    // 'csetp'
    // 'output0'
    // 'output1'
    // 'outside'
    // 'mode'
    public ThermostatStatusResponse getThermostatStatus() throws ClientException, ApiException {
        InputStream data = execAction(Action.THERMOSTAT_STATUS);
        try {
            return jsonMapper.readValue(data, ThermostatStatusResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClientException("failed to parse thermostat status response");
        }
    }

    public ThermostatStatusResponse getSensorBrightnessStatus() throws ClientException, ApiException {
        InputStream data = execAction(Action.SENSOR_BRIGHTNESS_STATUS);
        try {
            return jsonMapper.readValue(data, ThermostatStatusResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClientException("failed to parse sensor brightness status response");
        }
    }

    //get the version of the openmotics software.
    public Version getVersion() throws ApiException, ClientException {
        InputStream data = execAction(Action.VERSION);
        try {
            return jsonMapper.readValue(data, Version.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClientException("failed to parse version response");
        }
    }

    //get the modules connected
    public ModulesResponse getModules() throws ApiException, ClientException {
        InputStream data = execAction(Action.MODULES_GET);
        try {
            return jsonMapper.readValue(data, ModulesResponse.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClientException("failed to parse modules response");
        }
    }

    //get the known installations
    public List<Installation> getInstallations() throws ApiException, ClientException {
        InputStream data = execAction(Action.INSTALLATIONS);
        try {
            return jsonMapper.readValue(data, new TypeReference<List<Installation>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClientException("failed to parse installations response");
        }
    }

    public Map<String, Object> resetMaster() throws ApiException, ClientException {
        InputStream data = execAction(Action.MASTER_RESET);
        try {
            return jsonMapper.readValue(data, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClientException("failed to parse master reset response");
        }
    }

    public Map<String, Object> lightsAllOff() throws ApiException, ClientException {
        InputStream data = execAction(Action.LIGHTS_ALL_OFF);
        try {
            return jsonMapper.readValue(data, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClientException("failed to parse lights all off response");
        }
    }

    public Map<String, Object> setOutput(Integer id, Boolean isOn) throws ApiException, ClientException {
        //1. prepare payload
        Map<String, String> payload = new HashMap<>();
        payload.put("id", String.valueOf(id));
        payload.put("is_on", String.valueOf(isOn));

        //2. perform API request
        InputStream data = execAction(Action.OUTPUT_SET, payload);

        try {
            return jsonMapper.readValue(data, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClientException("failed to parse set output response");
        }
    }

    public Map<String, Object> flashLeds(Integer type, Integer id) throws ApiException, ClientException {
        //1. prepare payload
        Map<String, String> payload = new HashMap<>();
        payload.put("type", String.valueOf(type));
        payload.put("id", String.valueOf(id));

        //2. perform API request
        InputStream data = execAction(Action.LEDS_FLASH, payload);

        try {
            return jsonMapper.readValue(data, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ClientException("failed to parse flash leds response");
        }
    }

    public void setActiveInstallationById(String id) {
        Installation installation = this.installations.stream()
                .filter(install -> id.equals(install.getId()))
                .findFirst()
                .orElse(null);
        if (installation == null) log.error("installation with id: {} not found", id);
        else this.activeInstallation = installation;
        log.debug("active installation is: {}", this.activeInstallation);
    }

    //get the url for an action
    private String getUrl(Action action) {
        return String.format("%s:%s/api/%s", this.hostname, this.port, action.getPath());
    }

    private InputStream execAction(Action action) throws ApiException, ClientException {
        return execAction(action, null);
    }

    //execute an action: this method also performs the login if required
    private InputStream execAction(Action action, Map<String, String> payload) throws ApiException, ClientException {
        if (payload == null) payload = new HashMap<>();
        String url = getUrl(action);
        switch (action) {
            case LOGIN:
                return postAsForm(url, payload);
            case INSTALLATIONS:
                if (this.token == null && !login()) throw new AuthenticationException();
                payload.put("token", this.token);
                return postAsForm(url, payload);
            default:
                if (this.token == null && !login()) throw new AuthenticationException();
                if (this.installations == null) this.installations = getInstallations();
                if (this.activeInstallation == null &&
                        this.installations != null &&
                        this.installations.size() > 0) this.activeInstallation = installations.get(0);
                payload.put("token", this.token);
                payload.put("installation_id", activeInstallation.getId());
                InputStream response;
                try {
                    response = postAsForm(url, payload);
                } catch (AuthenticationException aE) {
                    if (login()) {
                        response = postAsForm(url, payload);
                    } else {
                        throw new AuthenticationException();
                    }
                }
                return response;
        }
    }

    private InputStream postAsJson(String url, Map payload) throws ApiException, ClientException, JsonProcessingException {
        //1. prepare http request
        HttpPost postRequest = new HttpPost(url);
        postRequest.addHeader(HttpHeaders.ACCEPT_ENCODING, String.valueOf(ContentType.APPLICATION_JSON));
        postRequest.addHeader(HttpHeaders.CONTENT_TYPE, String.valueOf(ContentType.APPLICATION_JSON));

        //2. set encoded payload
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(payload);
        postRequest.setEntity(new StringEntity(json, String.valueOf(ContentType.APPLICATION_JSON)));

        //3. perform POST
        return postRaw(postRequest);
    }

    private InputStream postAsForm(String url, Map<String, String> payload) throws ApiException, ClientException {
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
        return postRaw(postRequest);
    }

    private InputStream postRaw(HttpPost postRequest) throws ApiException, ClientException {
        //1. make request
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(postRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            log.debug("{} {} {} {}", postRequest.getMethod(), postRequest.getURI(),
                                     statusCode, response.getStatusLine().getReasonPhrase());

            InputStream data = response.getEntity().getContent();
            switch (statusCode) {
                case 401:
                    //TODO: parse msg field in json response
                    throw new AuthenticationException();
                case 503:
                    //TODO: parse msg field in json response
                    throw new MaintenanceModeException();
                case 403:
                    //TODO: parse msg field in json response
                    throw new ForbiddenException();
            }
            return data;
        } catch (IOException ex) {
            throw new ClientException(ex.getMessage());
        }
    }

    public void setVerifyHttps(Boolean verifyHttps) {
        try {
            this.httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.httpClient = Util.getHttpClient(USER_AGENT, verifyHttps);
        this.verifyHttps = verifyHttps;
    }


}