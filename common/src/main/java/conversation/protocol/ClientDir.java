package conversation.protocol;

import conversation.ClientMessage;
import conversation.ClientRequest;

public class ClientDir extends ClientMessage {
    static final long serialVersionUID = 101L;
    private String target;

    public ClientDir(long id, SessionId sessionId, String target) {
        super(id, sessionId, ClientRequest.DIR);
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
