package gmail.models;

/**
 * Created for gmailapi on 17/01/21
 */
public interface MailService {

    MailRdo getMailInfo(String messageId, String userId);

    void sendEmail(EmailQdo emailQdo);

}
