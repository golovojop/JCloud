package conversation.protocol;

import conversation.ClientMessage;
import conversation.ClientRequest;

public class ClientDir extends ClientMessage {
    static final long serialVersionUID = 101L;
    private String target;

    public ClientDir(long id, String target) {
        super(id, ClientRequest.DIR);
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
