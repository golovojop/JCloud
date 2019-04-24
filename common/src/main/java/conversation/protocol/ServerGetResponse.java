package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;

public class ServerGetResponse extends ServerMessage {
    private String fileName;
    private boolean status;
    private long length;

    public ServerGetResponse(long id, String fileName, boolean status) {
        super(id, ServerResponse.SGET);
        this.fileName = fileName;
        this.status = false;
        this.length = 0;
    }

    public ServerGetResponse(long id, String fileName, long length, boolean status) {
        super(id, ServerResponse.SGET);
        this.fileName = fileName;
        this.status = false;
        this.length = length;
    }

    public String getFileName() {
        return fileName;
    }
    public boolean isStatus() {
        return status;
    }
    public long getLength() {
        return length;
    }
}
