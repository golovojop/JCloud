package conversation.protocol;

import conversation.ClientMessage;
import conversation.ClientRequest;

public class ClientDelete extends ClientMessage {
    static final long serialVersionUID = 101L;
    private String target;

    public ClientDelete(long id, SessionId sessionId, String target) {
        super(id, sessionId, ClientRequest.DELETE);
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
