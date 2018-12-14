package com.openmotics;

import com.openmotics.exceptions.ApiException;
import com.openmotics.exceptions.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.mock;

/**
 * Unit test for simple App.
 */
@Slf4j
public class OpenmoticsClientTest
{
    @Mock
    OpenmoticsClient oM;

    @Test
    public void testLogin() throws ClientException, ApiException {

        Util util = mock(Util.class);

        // define return value for method getUniqueId()
        when(util.()).thenReturn(43);

        // use mock in test....
        assertEquals(test.getUniqueId(), 43);

        OpenmoticsClient oM = new OpenmoticsClient("user", "pass");

        when()
        oM.login()

        ClassToTest t  = new ClassToTest(databaseMock);
        boolean check = t.query("* from t");
        assertTrue(check);
        verify(databaseMock).query("* from t");
    }
}
