package conversation;

import java.io.Serializable;

public abstract class ClientMessage implements Serializable {
    static final long serialVersionUID = 100L;
    private final ClientRequest request;
    private long id;

    public ClientMessage(long id, ClientRequest request) {
        this.request = request;
        this.id = id;
    }

    public ClientRequest getRequest() {
        return request;
    }
    public long getId() {
        return id;
    }
}
