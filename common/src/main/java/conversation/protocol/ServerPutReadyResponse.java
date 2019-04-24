package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;

public class ServerPutReadyResponse extends ServerMessage {
    static final long serialVersionUID = 101L;
    private String fileName;
    private long length;

    public ServerPutReadyResponse(long id, String fileName, long length) {
        super(id, ServerResponse.SPUT_READY);
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
