package conversation;

import java.io.Serializable;

public abstract class ServerMessage implements Serializable {
    private ServerResponse response;
    private long id;

    public ServerMessage(long id, ServerResponse response) {
        this.response = response;
        this.id = id;
    }

    public ServerResponse getResponse() {
        return response;
    }

    public long getId() {
        return id;
    }
}
