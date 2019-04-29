package conversation.protocol;

import conversation.ClientMessage;
import conversation.ClientRequest;
import conversation.SessionId;

public class ClientBye extends ClientMessage {
    static final long serialVersionUID = 101L;
    private String message;

    public ClientBye(long id, SessionId sessionId, String message) {
        super(id, sessionId, ClientRequest.BYE);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
