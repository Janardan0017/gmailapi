package gmail.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by emp350 on 1/02/21
 */
public class AttachmentRdo implements java.io.Serializable {

    @JsonProperty("attachment_id")
    public String attachmentId;

    @JsonProperty("file_name")
    public String fileName;

    @JsonProperty("size")
    public String size;
}
