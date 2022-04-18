package gmail.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by emp350 on 3/02/21
 */
public class MessageDetailListRdo implements java.io.Serializable {

    @JsonProperty("result_size")
    public long resultSize;

    @JsonProperty("total_size")
    public long totalSize;

    @JsonProperty("next_page_token")
    public String nextPageToken;

    @JsonProperty("message_details")
    public List<MessageDetailRdo> messageDetailRdos;

    public MessageDetailListRdo() {
    }

    public MessageDetailListRdo(long resultSize, long totalSize, String nextPageToken, List<MessageDetailRdo> messageDetailRdos) {
        this.resultSize = resultSize;
        this.totalSize = totalSize;
        this.nextPageToken = nextPageToken;
        this.messageDetailRdos = messageDetailRdos;
    }
}
