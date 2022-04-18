package gmail.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * This class contains detail of an email message
 * <p>
 * Created by emp350 on 17/01/21
 */
public class MessageDetailRdo implements java.io.Serializable{

    @JsonProperty("id")
    public String id;

    @JsonProperty("thread_id")
    public String threadId;

    @JsonProperty("snippet")
    public String snippet;

    @JsonProperty("internal_date")
    public Long internalDate;

    @JsonProperty("from")
    public String from;

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

    @JsonProperty("date")
    public String date;

    @JsonProperty("attachments")
    public List<AttachmentRdo> attachments;

    @JsonProperty("message_id")
    public String messageId;

    @JsonProperty("references")
    public String references;

    @JsonProperty("reply_to")
    public String replyTo;
    @JsonProperty("in_reply_to")
    public String inReplyTo;
}
