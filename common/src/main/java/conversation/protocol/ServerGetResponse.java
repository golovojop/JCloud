package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;

public class ServerGetResponse extends ServerMessage {
    private String fileName;
    private long length;

    public ServerGetResponse(long id, String fileName, long length) {
        super(id, ServerResponse.SGET);
        this.fileName = fileName;
        this.length = length;
    }

    public String getFileName() {
        return fileName;
    }
    public long getLength() {
        return length;
    }
}
