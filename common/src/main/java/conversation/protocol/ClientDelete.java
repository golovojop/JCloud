package conversation.protocol;

import conversation.ClientMessage;
import conversation.ClientRequest;

public class ClientDelete extends ClientMessage {
    static final long serialVersionUID = 101L;
    private String target;

    public ClientDelete(long id, String target) {
        super(id, ClientRequest.DELETE);
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
