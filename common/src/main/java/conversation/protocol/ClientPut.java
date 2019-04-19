package conversation.protocol;

import conversation.ClientMessage;
import conversation.ClientRequest;

public class ClientPut extends ClientMessage {
    static final long serialVersionUID = 101L;
    private String fileName;

    public ClientPut(long id, SessionId sessionId, String fileName) {
        super(id, sessionId, ClientRequest.PUT);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
