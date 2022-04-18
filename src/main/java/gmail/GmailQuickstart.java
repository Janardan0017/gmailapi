package gmail;

import gmail.models.MessageDetailListRdo;
import gmail.models.UserSignupQdo;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GmailQuickstart {

    // jordandemo001@gmail.com
    private static final String refreshToken = "1//0gWh5YGmltXsRCgYIARAAGBASNwF-L9Ir6ZnHy-HXEhlW2ZLCa6JPZUGlNMMvP0TVfZki6QDjjVjB2FqxcoA62kHhO6sGUrsmWqA";
    //jnrdn0011@gmail.com
//    private static final String refreshToken = "1//0gDI0uvfsQAtICgYIARAAGBASNwF-L9IrfoMJVvGoN0Mzp89bvCfiK2J31faER5GEuzKz9u6An8NyB4qEsBxTKlQR8dcx7zfA1Ts";
    //janardan.ft@gmail.com
//    private static final String refreshToken = "1//0gfmmL3Y5UeiACgYIARAAGBASNwF-L9Ir6SUxiK1f5-msdsEBE1KaH6cIXC614w1b3tGMxjh0ScfAnOUQjsc7eLnDwPk91uygX-o";

    public static void main(String... args) throws GeneralSecurityException, IOException {
        GoogleMailService googleMailService = new GoogleMailService();
        MessageDetailListRdo messageDetailListRdo = googleMailService.fetchSupportEmails("me", refreshToken, 10, null);
//        Gmail service = googleMailService.getGmailServiceFromRefreshToken(refreshToken);
//        ListMessagesResponse listMessagesResponse = service.users().messages().list("me").execute();
//        List<Message> messages = listMessagesResponse.getMessages();
//        messages.forEach(message -> {
//            System.out.println(message.getId()+"\n"+message.getThreadId()+"\n"+message.getInternalDate()+"\n"+message.getSnippet()+"\n");
//        });
        messageDetailListRdo.messageDetailRdos.forEach(message -> {
            System.out.println(message.id+"\n"+message.threadId+"\n"+message.messageId+"\n"+message.snippet+"\n"+message.internalDate+"\n");
        });
    }

    public static UserSignupQdo getUserSignQdo(String accessCode, String redirectUri) {
        UserSignupQdo userSignupQdo = new UserSignupQdo();
        userSignupQdo.accessCode = accessCode;
        userSignupQdo.redirectUri = redirectUri;
        return userSignupQdo;
    }
}