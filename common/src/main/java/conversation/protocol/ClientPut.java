package conversation.protocol;

import conversation.ClientMessage;
import conversation.ClientRequest;
import conversation.SessionId;

public class ClientPut extends ClientMessage {
    static final long serialVersionUID = 101L;
    private String fileName;
    private long length;

    public ClientPut(long id, SessionId sessionId, String fileName, long length) {
        super(id, sessionId, ClientRequest.PUT);
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
