package gmail;

import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import gmail.models.*;
import gmail.util.NumberUtil;
import gmail.util.UserInfoUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This service class is implementation of google gmail services
 * <p>
 * Created by emp350 on 17/01/21
 */
public class GoogleMailService {

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

//    @Value("${google.api.credentials.file.path}")
    private String credentialsFilePath = "/credentials.json";

    @Value("${google.application.name:}")
    private String applicationName="Gmail Api Test";

    private static final String TOKEN_SERVER_ENCODED_URL = "https://oauth2.googleapis.com/token";
    private static final String REVOKE_ACCESS_URL = "https://accounts.google.com/o/oauth2/revoke?token=";

    private static final String DATE = "Date";
    private static final String REFERENCES = "References";
    private static final String REPLY_TO = "Reply-To";
    private static final String IN_REPLY_TO = "In-Reply-To";
    private static final String MESSAGE_ID = "Message-ID";
    private static final String SUBJECT = "Subject";
    private static final String FROM = "From";
    private static final String TO = "To";
    private static final String CC = "Cc";
    private static final String BCC = "Bcc";
    private static final String NAME = "name";
    private static final String PICTURE = "picture";

    /**
     * Get gmail service from accessToken
     *
     * @param accessToken : access token issued by the google authorization server
     * @return Gmail service
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public Gmail getGmailServiceFromAccessToken(String accessToken) throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        GoogleCredential googleCredential = new GoogleCredential().setAccessToken(accessToken);
        return new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, googleCredential)
                .setApplicationName(applicationName).build();
    }


    /**
     * Get accessToken and refreshToken from one time code
     *
     * @param userSignupQdo : sign up user details
     * @return users detail
     */
    public SignUpUserDetails authenticateUser(UserSignupQdo userSignupQdo) throws IOException, GeneralSecurityException {
        InputStream in = GoogleMailService.class.getResourceAsStream(credentialsFilePath);
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                httpTransport,
                JSON_FACTORY,
                TOKEN_SERVER_ENCODED_URL,
                clientSecrets.getDetails().getClientId(),
                clientSecrets.getDetails().getClientSecret(),
                userSignupQdo.accessCode,
                userSignupQdo.redirectUri).execute();

        GoogleIdToken.Payload payload = tokenResponse.parseIdToken().getPayload();

        SignUpUserDetails signUpUserDetails = new SignUpUserDetails();
        signUpUserDetails.name = (String) payload.get(NAME);
        signUpUserDetails.email = payload.getEmail();
        signUpUserDetails.pictureUrl = (String) payload.get(PICTURE);
        signUpUserDetails.accessToken = tokenResponse.getAccessToken();
        signUpUserDetails.refreshToken = tokenResponse.getRefreshToken();
        return signUpUserDetails;
    }

    /**
     * Get Gmail service from refresh token
     *
     * @param refreshToken : authorization refresh token generated by the google authorization server
     * @return
     */
    public Gmail getGmailServiceFromRefreshToken(String refreshToken) throws GeneralSecurityException, IOException {
        InputStream in = GoogleMailService.class.getResourceAsStream(credentialsFilePath);
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(httpTransport, JSON_FACTORY, refreshToken,
                clientSecrets.getDetails().getClientId(),
                clientSecrets.getDetails().getClientSecret()).execute();

        GoogleCredential googleCredential = new GoogleCredential().setAccessToken(tokenResponse.getAccessToken());

        return new Gmail.Builder(httpTransport, JSON_FACTORY, googleCredential)
                .setApplicationName(applicationName).build();
    }

    /**
     * Get the accessToken from a given refreshToken
     *
     * @param refreshToken : refresh token string
     * @return access token
     */
    public String getAccessTokenFromRefreshToken(String refreshToken) throws GeneralSecurityException, IOException {
        InputStream in = GoogleMailService.class.getResourceAsStream(credentialsFilePath);
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        // Load client secrets.
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(httpTransport, JSON_FACTORY, refreshToken,
                clientSecrets.getDetails().getClientId(),
                clientSecrets.getDetails().getClientSecret()).execute();

        return tokenResponse.getAccessToken();
    }


    /**
     * Get details of a single message
     *
     * @param message : message to get the details
     * @return : full details of message
     */
    public MessageDetailRdo getMessageDetail(Message message) {
        MessagePart messagePart = message.getPayload();
        MessageDetailRdo messageDetailRdo = new MessageDetailRdo();
        List<AttachmentRdo> attachments = new ArrayList<>();

        messageDetailRdo.id = message.getId();
        messageDetailRdo.threadId = message.getThreadId();
        messageDetailRdo.internalDate = message.getInternalDate();
        messageDetailRdo.snippet = message.getSnippet();

        if (messagePart != null) {
            List<MessagePartHeader> headers = messagePart.getHeaders();
            for (MessagePartHeader header : headers) {
                switch (header.getName()) {
                    case DATE:
                        messageDetailRdo.date = header.getValue();
                        break;
                    case SUBJECT:
                        messageDetailRdo.subject = header.getValue();
                        break;
                    case FROM:
                        messageDetailRdo.from = UserInfoUtil.getEmailId(header.getValue());
                        break;
                    case TO:
                        messageDetailRdo.to = UserInfoUtil.getEmailIds(header.getValue());
                        break;
                    case CC:
                        messageDetailRdo.cc = UserInfoUtil.getEmailIds(header.getValue());
                        break;
                    case BCC:
                        messageDetailRdo.bcc = UserInfoUtil.getEmailIds(header.getValue());
                        break;
                    case REPLY_TO:
                        messageDetailRdo.replyTo = header.getValue();
                        break;
                    case MESSAGE_ID:
                        messageDetailRdo.messageId = header.getValue();
                        break;
                    case REFERENCES:
                        messageDetailRdo.references = header.getValue();
                        break;
                    case IN_REPLY_TO:
                        messageDetailRdo.inReplyTo = header.getValue();
                        break;
                    default:
                }
            }
            attachments = fetchAttachments(messagePart.getParts());
        }
        messageDetailRdo.body = getContent(message, "text/html");
        messageDetailRdo.attachments = attachments;
        return messageDetailRdo;
    }

    /**
     * Send an email message
     *
     * @param messageQdo   : message details
     * @param attachments  : email attachment data
     * @param from         : sender address
     * @param refreshToken : gmail access token
     * @return
     */
    public void sendMessage(MessageQdo messageQdo, List<MultipartFile> attachments, String from, String refreshToken)
            throws MessagingException, IOException, GeneralSecurityException {
        Gmail gmailService = getGmailServiceFromRefreshToken(refreshToken);
        // set basic details for email
        MimeMessage mimeMessage = createEmail(messageQdo, from);
        // add attachments
        if (!CollectionUtils.isEmpty(attachments)) {
            addAttachmentDetails(mimeMessage, messageQdo.body, attachments);
        } else {
            mimeMessage.setText(messageQdo.body, "UTF-8", "html");
        }
        // add reply details
        if (messageQdo.isReplyEmail) {
            addReplyEmailDetails(mimeMessage, messageQdo);
        }

        Message message = createMessageWithEmail(mimeMessage);
        // if thread id is given  then send the reply to given thread
        if (messageQdo.isReplyEmail && !StringUtils.isEmpty(messageQdo.threadId))
            message.setThreadId(messageQdo.threadId);
        gmailService.users().messages().send(from, message).execute();
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param email       : Mime message to set the attachment details
     * @param attachments : attachments for email
     * @param body        : message body
     * @return MimeMessage to be used to send email
     * @throws MessagingException
     */
    public void addAttachmentDetails(MimeMessage email, String body, List<MultipartFile> attachments) throws MessagingException, IOException {

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(body, "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        int fileIndex = 0;
        for (MultipartFile multipartFile : attachments) {
            InputStream inputStream = multipartFile.getInputStream();
            mimeBodyPart = new MimeBodyPart();
            DataSource source = new ByteArrayDataSource(inputStream, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            mimeBodyPart.setDataHandler(new DataHandler(source));
            mimeBodyPart.setFileName(multipartFile.getOriginalFilename());

            multipart.addBodyPart(mimeBodyPart, fileIndex);
            fileIndex++;
        }
        email.setContent(multipart);
    }

    /**
     * Create a MimeMessage using the parameters provided.
     *
     * @param messageQdo message details
     * @param from       email address of the sender, the mailbox account
     * @return the MimeMessage to be used to send email
     * @throws MessagingException
     */
    private MimeMessage createEmail(MessageQdo messageQdo, String from) throws MessagingException {
        Properties properties = new Properties();
        Session session = Session.getDefaultInstance(properties, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        if (!CollectionUtils.isEmpty(messageQdo.to)) {
            for (String user : messageQdo.to) {
                email.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(user));
            }
        }
        if (!CollectionUtils.isEmpty(messageQdo.cc)) {
            for (String user : messageQdo.cc) {
                email.addRecipient(javax.mail.Message.RecipientType.CC, new InternetAddress(user));
            }
        }
        if (!CollectionUtils.isEmpty(messageQdo.bcc)) {
            for (String user : messageQdo.bcc) {
                email.addRecipient(javax.mail.Message.RecipientType.BCC, new InternetAddress(user));
            }
        }
        email.setSubject(messageQdo.subject);
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


    /**
     * get content/body of message
     *
     * @param message  : message details
     * @param mimeType : message mime type (text/plain or text/html)
     * @return email message text in html format
     */
    private String getContent(Message message, String mimeType) {
        StringBuilder stringBuilder = new StringBuilder();
        getTextFromMessageParts(message.getPayload().getParts(), stringBuilder, mimeType);
        String data = stringBuilder.toString();
        if (StringUtils.isEmpty(data)) {
            data = message.getPayload().getBody().getData();
        }
        if (StringUtils.isEmpty(data))
            return null;
        byte[] bodyBytes = Base64.decodeBase64(data);
        String text = new String(bodyBytes, StandardCharsets.UTF_8);
        return StringUtils.normalizeSpace(text);
    }

    /**
     * recursive function to search the required mimetype message
     *
     * @param messageParts : first level of message part
     * @param mimeType     : message format (text/plain, text/html)
     */
    private void getTextFromMessageParts(List<MessagePart> messageParts, StringBuilder stringBuilder, String mimeType) {
        if (messageParts != null) {
            for (MessagePart messagePart : messageParts) {
                // if this body contains the html content and is not a file
                if (messagePart.getMimeType().equals(mimeType) && messagePart.getBody().getData() != null) {
                    stringBuilder.append(messagePart.getBody().getData());
                }
                if (messagePart.getParts() != null) {
                    getTextFromMessageParts(messagePart.getParts(), stringBuilder, mimeType);
                }
            }
        }
    }

    /**
     * This method is used to revoke the google account access
     *
     * @param refreshToken : Refresh token of user account to revoke the access
     * @return http status code
     * @throws IOException
     */
//    public int revokeUserAccess(String refreshToken) throws IOException, GeneralSecurityException {
//        HttpClient httpClient = HttpClientBuilder.create().build();
//        HttpPost httpPost = new HttpPost(REVOKE_ACCESS_URL + getAccessTokenFromRefreshToken(refreshToken));
//        HttpResponse response = httpClient.execute(httpPost);
//        return response.getStatusLine().getStatusCode();
//    }

    /**
     * Fetch inbox data for given user and filter
     *
     * @param email          : Logged in user email id
     * @param refreshToken   : Refresh token credentials to access gmail api
     * @param pageSize       : Number of items per page
     * @param pageToken      : Gmail page token to access a particular page data
     * @param employeeEmails : Email ids of employees of logged in user
     * @return inbox message details
     */
    public MessageDetailListRdo fetchInbox(String email, String refreshToken, long pageSize, String pageToken,
                                           List<String> employeeEmails) throws IOException, GeneralSecurityException {
        // create the filter query
        String query = createQuery(employeeEmails);
        return fetchEmails(email, refreshToken, pageSize, pageToken, query);
    }

    /**
     * This method is used to get the attachment data as bytes array
     *
     * @param messageId    : Id of the message where attachment belongs to
     * @param attachmentId : Id of attachment
     * @param refreshToken : Gmail access token
     * @return attachment data in bytes
     */
    public ByteArrayInputStream getAttachmentData(String userId, String messageId, String attachmentId, String refreshToken) throws IOException, GeneralSecurityException {
        Gmail service = getGmailServiceFromRefreshToken(refreshToken);
        byte[] bytes = service.users().messages().attachments().get(userId, messageId, attachmentId).execute().decodeData();
        return new ByteArrayInputStream(bytes);
    }

    /**
     * This method is used to fetch all the attachments for given message part
     *
     * @param messageParts : Message parts details
     * @return attachment list with basic details
     */
    private List<AttachmentRdo> fetchAttachments(List<MessagePart> messageParts) {
        List<AttachmentRdo> attachmentRdos = new ArrayList<>();
        if (messageParts != null) {
            for (MessagePart part : messageParts) {
                //For each part, see if it has a file name, if it does it's an attachment
                if (!StringUtils.isEmpty(part.getFilename())) {
                    AttachmentRdo attachmentRdo = new AttachmentRdo();
                    attachmentRdo.fileName = part.getFilename();
                    attachmentRdo.attachmentId = part.getBody().getAttachmentId();
                    attachmentRdo.size = NumberUtil.convertBytes(part.getBody().getSize());
                    attachmentRdos.add(attachmentRdo);
                } else if (part.getMimeType().equals("multipart/related") && part.getParts() != null) {
                    fetchAttachments(part.getParts());
                }
            }
        }
        return attachmentRdos;
    }

    /**
     * This method is used to add the additional detail to send the email as reply email
     *
     * @param email      : Reply email details
     * @param messageQdo : User provided data to sedn with email
     * @throws MessagingException
     */
    private void addReplyEmailDetails(MimeMessage email, MessageQdo messageQdo) throws MessagingException {
        if (messageQdo.subject.startsWith("Re: "))
            email.setSubject(messageQdo.subject);
        else
            email.setSubject("Re: " + messageQdo.subject);
        if (messageQdo.inReplyTo != null)
            email.setHeader(IN_REPLY_TO, messageQdo.inReplyTo);
        else
            email.setHeader(IN_REPLY_TO, messageQdo.messageId);

        if (messageQdo.references != null)
            email.setHeader(REFERENCES, messageQdo.references);
        else
            email.setHeader(REFERENCES, messageQdo.messageId);
    }

    /**
     * This method is used to create the search query for email filtering
     *
     * @param users : list of users gmail to fetch the conversation
     * @return advanced gmail filter query
     */
    private String createQuery(List<String> users) {
        StringBuilder query = new StringBuilder();
        // create query for users with OR condition
        StringBuilder userQuery = new StringBuilder();
        if (!CollectionUtils.isEmpty(users)) {
            int i = 0;
            while (i < users.size() - 1) {
                userQuery.append(users.get(i)).append(" OR ");
                i++;
            }
            userQuery.append(users.get(i));
        }
        query.append("((from:me AND to:(").append(userQuery).append("))");
        query.append(" OR (from:(").append(userQuery).append(") AND to:me)) AND (in:inbox OR in:sent)  AND -in:chats");
        return query.toString();
    }

    /**
     * Fetch inbox data for given user and filter
     *
     * @param email          : Logged in user email id
     * @param refreshToken   : Refresh token credentials to access gmail api
     * @param pageSize       : Number of items per page
     * @param pageToken      : Gmail page token to access a particular page data
     * @return inbox message details
     */
    public MessageDetailListRdo fetchSupportEmails(String email, String refreshToken, long pageSize, String pageToken) throws IOException, GeneralSecurityException {
        String query = "-in:chats";
        return fetchEmails(email, refreshToken, pageSize, pageToken, query);
    }

    private MessageDetailListRdo fetchEmails(String email, String refreshToken, long pageSize, String pageToken, String query)
            throws IOException, GeneralSecurityException {
        List<MessageDetailRdo> messageDetailRdos = new ArrayList<>();
        Gmail gmailService = getGmailServiceFromRefreshToken(refreshToken);
        BatchRequest batchRequest = gmailService.batch();
        JsonBatchCallback<Message> batchCallback = new JsonBatchCallback<Message>() {
            @Override
            public void onFailure(GoogleJsonError e, HttpHeaders responseHeaders) {
            }

            @Override
            public void onSuccess(Message message, HttpHeaders responseHeaders) {
                MessageDetailRdo messageDetail = getMessageDetail(message);
                if (messageDetail != null)
                    messageDetailRdos.add(messageDetail);
            }
        };
        Gmail.Users.Messages.List list = gmailService.users().messages().list(email).setQ(query).setMaxResults(pageSize);
        if (!StringUtils.isEmpty(pageToken)) {
            list.setPageToken(pageToken);
        }
        ListMessagesResponse messagesResponse = list.execute();
        List<Message> messages = messagesResponse.getMessages();
        for (Message message : messages) {
            gmailService.users().messages().get(email, message.getId()).queue(batchRequest, batchCallback);
        }
        batchRequest.execute();
        MessageDetailListRdo messageDetailListRdo = new MessageDetailListRdo();
        messageDetailListRdo.messageDetailRdos = messageDetailRdos;
        messageDetailListRdo.resultSize = pageSize;
        messageDetailListRdo.totalSize = messagesResponse.getResultSizeEstimate();
        messageDetailListRdo.nextPageToken = messagesResponse.getNextPageToken();
        return messageDetailListRdo;
    }
}