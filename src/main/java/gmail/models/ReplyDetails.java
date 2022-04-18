package gmail.models;

/**
 * Created by emp350 on 23/02/21
 */
public class ReplyDetails {

    public String threadId;
    public String from;
    public String date;
    public String subject;
    public String to;
    public String messageId;
    public String references;
    public String replyTo;
    public String inReplyTo;

    @Override
    public String toString() {
        return "gmail.models.ReplyDetails{" +
                "threadId='" + threadId + '\'' +
                ", from='" + from + '\'' +
                ", date='" + date + '\'' +
                ", subject='" + subject + '\'' +
                ", to='" + to + '\'' +
                ", messageId='" + messageId + '\'' +
                ", references='" + references + '\'' +
                ", replyTo='" + replyTo + '\'' +
                ", inReplyTo='" + inReplyTo + '\'' +
                '}';
    }
}
