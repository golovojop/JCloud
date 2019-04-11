package conversation.message;

import conversation.ClientMessage;
import conversation.ClientRequest;

import java.io.Serializable;

public class ClientDir extends ClientMessage implements Serializable {
    private String target;

    public ClientDir(long id, String target) {
        super(id, ClientRequest.DIR);
        this.target = target;
    }

    public String getTarget() {
        return target;
    }
}
