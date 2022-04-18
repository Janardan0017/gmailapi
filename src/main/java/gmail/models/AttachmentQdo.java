package gmail.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by emp350 on 12/02/21
 */
public class AttachmentQdo {

    @JsonProperty("file_name")
    public String fileName;

    @JsonProperty("size")
    public int size;

    @JsonProperty("data")
    public String data;
}
