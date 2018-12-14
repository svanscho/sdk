package com.openmotics;

import com.openmotics.exceptions.AuthenticationException;
import com.openmotics.exceptions.MaintenanceModeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Unit test for simple App.
 */
@Slf4j
@RunWith(PowerMockRunner.class)
@PrepareForTest({OpenmoticsClient.class, Util.class})
@PowerMockIgnore({"javax.net.ssl.*"})
public class OpenmoticsClientTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testLoginSuccess() throws Exception {
        //prepare
        String token = "abc1234";
        String response = String.format("{\"success\": true, \"token\":\"%s\"}", token);
        mockApiWithString(200, response);

        //test
        OpenmoticsClient oC = PowerMockito.spy(new OpenmoticsClient("user","pass"));
        oC.login();

        //verify
        verify(oC, times(1)).login();
        assertEquals(oC.getToken(), token);
    }

    @Test
    public void testLoginFailure() throws Exception {
        //prepare
        String token = "abc1234";
        String response = String.format("{\"success\": false, \"token\":\"%s\"}", token);
        mockApiWithString(200, response);

        //test
        OpenmoticsClient oC = PowerMockito.spy(new OpenmoticsClient("user","pass"));
        try {
            oC.login();
            Assert.fail("expecting AuthenticationException with success=false");
        } catch (AuthenticationException e) {
            verify(oC, times(1)).login();
            assertEquals(oC.getToken(), null);
        }
    }

    @Test
    public void testLogin401Failure() throws Exception {
        //prepare
        String token = "abc1234";
        String response = String.format("{\"success\": true, \"token\":\"%s\"}", token);
        mockApiWithString(401, response);

        //test
        OpenmoticsClient oC = PowerMockito.spy(new OpenmoticsClient("user","pass"));
        try {
            oC.login();
            Assert.fail("expecting AuthenticationException with 401 status code");
        } catch (AuthenticationException e) {
            verify(oC, times(1)).login();
            assertEquals(oC.getToken(), null);
        }
    }

    @Test
    public void testMaintenanceMode() throws Exception {
        //prepare
        mockApiWithString(503, "");

        //test
        OpenmoticsClient oC = PowerMockito.spy(new OpenmoticsClient("user","pass"));
        try {
            oC.getVersion();
            Assert.fail("expecting MaintenanceModeException");
        } catch (MaintenanceModeException e) {
            verify(oC, times(1)).getVersion();
        }
    }

    private void mockApiWithFile(int statusCode, String fileName) throws IOException {
        InputStream response = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
        mockApiWithStream(statusCode, response);

    }

    private void mockApiWithString(int statusCode, String payload) throws IOException {
        InputStream response = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
        mockApiWithStream(statusCode, response);
    }

    private void mockApiWithStream(int statusCode, InputStream response) throws IOException {
        CloseableHttpResponse httpResponse = PowerMockito.mock(CloseableHttpResponse.class, Mockito.RETURNS_DEEP_STUBS);
        when(httpResponse.getEntity().getContent()).thenReturn(response);
        when(httpResponse.getStatusLine().getStatusCode()).thenReturn(statusCode);

        CloseableHttpClient httpClient = PowerMockito.mock(CloseableHttpClient.class, Mockito.RETURNS_MOCKS);
        when(httpClient.execute(any(HttpUriRequest.class))).thenReturn(httpResponse);

        PowerMockito.mockStatic(Util.class);
        when(Util.getHttpClient(any(String.class), any(Boolean.class))).thenReturn(httpClient);
    }
}
