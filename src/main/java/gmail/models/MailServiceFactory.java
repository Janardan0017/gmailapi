package gmail.models;

import com.google.api.client.util.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created for gmailapi on 17/01/21
 */
@Service
public class MailServiceFactory {

    @Value("${mail.service}")
    private String mailService;

    private GoogleMailService googleMailService;

    private YahooMailService yahooMailService;

    @Autowired
    public void setGoogleMailService(GoogleMailService googleMailService) {
        this.googleMailService = googleMailService;
    }

    @Autowired
    public void setYahooMailService(YahooMailService yahooMailService) {
        this.yahooMailService = yahooMailService;
    }

    public MailService getService() {
        switch (mailService) {
            case "GMAIL":
                return googleMailService;
            case "YAHOO":
                return yahooMailService;
            default:
                return null;
        }
    }
}
