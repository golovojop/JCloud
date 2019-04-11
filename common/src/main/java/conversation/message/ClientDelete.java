package conversation.message;

import conversation.ClientMessage;
import conversation.ClientRequest;

import java.io.Serializable;

public class ClientDelete extends ClientMessage implements Serializable {
    private String target;

    public ClientDelete(long id, String target) {
        super(id, ClientRequest.DELETE);
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
