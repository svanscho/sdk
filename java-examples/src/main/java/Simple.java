import com.openmotics.OpenmoticsClient;
import com.openmotics.exceptions.ApiException;
import com.openmotics.exceptions.ClientException;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by svanscho on 13/12/2018.
 */
@Slf4j
public class Simple {
    public static void main(String[] args) throws ClientException, ApiException {
        OpenmoticsClient oM = new OpenmoticsClient("user", "pass");
        log.info(String.valueOf(oM.getInstallations()));
        log.info(String.valueOf(oM.getVersion()));
        log.info(String.valueOf(oM.getStatus()));
        log.info(String.valueOf(oM.getOutputStatus()));
        //log.info(String.valueOf(oM.lightsAllOff()));
        //log.info(String.valueOf(oM.resetMaster()));
    }
}
