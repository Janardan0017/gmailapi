package gmail.models;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created for gmailapi on 17/01/21
 */
@Service
public class GoogleMailService implements MailService {

    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.MAIL_GOOGLE_COM);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    Gmail gmailService;

    @PostConstruct
    public void postConstruct() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        gmailService = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = GoogleMailService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8080).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


    @Override
    public MailRdo getMailInfo(String messageId, String userId) {
        try {
            Message message = gmailService.users().messages().get(userId, messageId).setFormat("FULL").execute();
            MessagePart messagePart = message.getPayload();
            MailRdo mailRdo = new MailRdo();
            mailRdo.id = messageId;
            if (messagePart != null) {
                List<MessagePartHeader> headers = messagePart.getHeaders();
                for (MessagePartHeader header : headers) {
                    if (header.getName().equals("Date")) {
                        mailRdo.date = header.getValue();
                    } else if (header.getName().equals("Subject")) {
                        mailRdo.subject = header.getValue();
                    } else if (header.getName().equals("From")) {
                        mailRdo.from = header.getValue();
                    } else if (mailRdo.to.equals("To")) {
                        mailRdo.to = header.getValue();
                    }
                }
            }
            mailRdo.body = getContent(message);
            return mailRdo;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void sendEmail(EmailQdo emailQdo) {
        try {
            MimeMessage mimeMessage = createEmail(emailQdo.to, emailQdo.from, emailQdo.subject, emailQdo.body);
            Message message = createMessageWithEmail(mimeMessage);
            message = gmailService.users().messages().send("me", message).execute();

            System.out.println("Message id: " + message.getId());
            System.out.println(message.toPrettyString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param to       email address of the receiver
     * @param from     email address of the sender, the mailbox account
     * @param subject  subject of the email
     * @param bodyText body text of the email
     * @return the MimeMessage to be used to send email
     * @throws MessagingException
     */
    private MimeMessage createEmail(String to, String from, String subject, String bodyText) throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setText(bodyText);
        return email;
    }

    /**
     * Create a message from an email.
     *
     * @param emailContent Email to be set to raw of message
     * @return a message containing a base64url encoded email
     * @throws IOException
     * @throws MessagingException
     */
    private Message createMessageWithEmail(MimeMessage emailContent) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        emailContent.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        String encodedEmail = Base64.encodeBase64URLSafeString(bytes);
        Message message = new Message();
        message.setRaw(encodedEmail);
        return message;
    }


    private String getContent(Message message) {
        StringBuilder stringBuilder = new StringBuilder();
        getTextFromMessageParts(message.getPayload().getParts(), stringBuilder, "text/html");
        if (stringBuilder.length() == 0) {
            stringBuilder.append(message.getPayload().getBody().getData());
        }
        byte[] bodyBytes = Base64.decodeBase64(stringBuilder.toString());
        String text = new String(bodyBytes, StandardCharsets.UTF_8);
        return StringUtils.normalizeSpace(text);
    }

    private void getTextFromMessageParts(List<MessagePart> messageParts, StringBuilder stringBuilder, String mimeType) {
        if (messageParts != null) {
            for (MessagePart messagePart : messageParts) {
                if (messagePart.getMimeType().equals(mimeType)) {
                    stringBuilder.append(messagePart.getBody().getData());
                }
                if (messagePart.getParts() != null) {
                    getTextFromMessageParts(messagePart.getParts(), stringBuilder, mimeType);
                }
            }
        }
    }

    public void getAttachments(Gmail service, List<MessagePart> messageParts, List<String> fileNames, String dir, String userId) {
        if (!dir.endsWith("/")) {
            dir += "/";
        }

        if (messageParts != null) {
            for (MessagePart part : messageParts) {
                //For each part, see if it has a file name, if it does it's an attachment
                if ((part.getFilename() != null && part.getFilename().length() > 0)) {
                    String filename = part.getFilename();
                    String attachmentId = part.getBody().getAttachmentId();
                    MessagePartBody attachPart;
                    FileOutputStream fileOutputStream = null;
                    try {
                        //Go get the attachment part and get the bytes
                        attachPart = service.users().messages().attachments().get(userId, part.getPartId(), attachmentId).execute();
                        byte[] fileByteArray = Base64.decodeBase64(attachPart.getData());

                        //Write the attachment to the output dir
                        fileOutputStream = new FileOutputStream(dir + filename);
                        fileOutputStream.write(fileByteArray);
                        fileOutputStream.close();
                        fileNames.add(filename);
                    } catch (IOException e) {
                        System.out.println("IO Exception processing attachment: " + filename);
                    } finally {
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else if (part.getMimeType().equals("multipart/related") && part.getParts() != null) {
                    getAttachments(service, part.getParts(), fileNames, dir, userId);
                }
            }
        }
    }

    public String createQuery(List<String> fromAddresses, List<String> toAddresses, String subject, String hasTheWords,
                              boolean hasAttachments, boolean dontIncludeChats) {
        StringBuilder query = new StringBuilder();
        if (fromAddresses != null && !fromAddresses.isEmpty()) {
            query.append("from:(");
            fromAddresses.forEach(email -> query.append(email).append(","));
            query.append(")");
        }
        if (toAddresses != null && !toAddresses.isEmpty()) {
            query.append(" to:(");
            toAddresses.forEach(email -> query.append(email).append(","));
            query.append(")");
        }
        if (!StringUtils.isEmpty(subject)) {
            query.append(" from:").append(subject);
        }
        if (!StringUtils.isEmpty(hasTheWords)) {
            query.append(" ").append(hasTheWords);
        }
        if (hasAttachments) {
            query.append(" has:attachment");
        }
        if (dontIncludeChats) {
            query.append(" -in:chats");
        }
        // only inbox messages
        query.append(" in:inbox");
        return query.toString();
    }
}
