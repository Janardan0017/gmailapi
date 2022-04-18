package gmail.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by emp350 on 5/02/21
 */
public class MessageQdo {

    @JsonProperty("id")
    public String id;

    @JsonProperty("thread_id")
    public String threadId;

    @JsonProperty("to")
    public List<String> to;

    @JsonProperty("cc")
    public List<String> cc;

    @JsonProperty("bcc")
    public List<String> bcc;

    @JsonProperty("subject")
    public String subject;

    @JsonProperty("body")
    public String body;

    @JsonProperty("is_reply_email")
    public boolean isReplyEmail;

    @JsonProperty("message_id")
    public String messageId;

    @JsonProperty("references")
    public String references;

    @JsonProperty("reply_to")
    public String replyTo;

    @JsonProperty("in_reply_to")
    public String inReplyTo;
}
