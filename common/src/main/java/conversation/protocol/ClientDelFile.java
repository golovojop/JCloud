package conversation.protocol;

import conversation.ClientMessage;
import conversation.ClientRequest;

public class ClientDelFile extends ClientMessage {
    static final long serialVersionUID = 101L;
    private String fileName;

    public ClientDelFile(long id, SessionId sessionId, String fileName) {
        super(id, sessionId, ClientRequest.DELETE);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
