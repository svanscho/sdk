package com.openmotics;

import com.openmotics.exceptions.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;

/**
 * Unit test for simple App.
 */
@Slf4j
public class OpenMoticsCloudApiTest
{
    @Mock
    private CloseableHttpClient httpClient;

    @InjectMocks
    private OpenMoticsCloudApi openMoticsCloudApi;

    @Test
    public void performCloudLogin() throws IOException, ApiException {
        OpenMoticsCloudApi api = new OpenMoticsCloudApi("vanschoote.sander@gmail.com", "test1234");
        if(api.login()){
            //logged in successfully
            log.info(String.valueOf(api.getInstallations()));
            log.info(String.valueOf(api.getVersion()));
            log.info(String.valueOf(api.getStatus()));
            log.info(String.valueOf(api.getOutputStatus()));
            log.info(String.valueOf(api.getThermostatStatus()));
        };
    }
}
