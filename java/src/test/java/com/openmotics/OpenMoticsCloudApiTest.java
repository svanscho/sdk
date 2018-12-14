package com.openmotics;

import com.openmotics.exceptions.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;

/**
 * Unit test for simple App.
 */
@Slf4j
public class OpenMoticsCloudApiTest
{
    @Test
    public void performCloudLogin() throws IOException, ApiException {
        OpenMoticsCloudApi api = new OpenMoticsCloudApi("user", "pass");
        if(api.login()){
            //logged in successfully
            log.info(String.valueOf(api.getInstallations()));
            log.info(api.getVersion());

        };

    }
}
