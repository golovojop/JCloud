package conversation.protocol;

import conversation.ClientMessage;
import conversation.ClientRequest;
import conversation.SessionId;

public class ClientGet extends ClientMessage {
    static final long serialVersionUID = 101L;
    private String fileName;

    public ClientGet(long id, SessionId sessionId, String fileName) {
        super(id, sessionId, ClientRequest.GET);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
