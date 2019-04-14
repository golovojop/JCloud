package conversation;

import conversation.protocol.SessionId;

import java.io.Serializable;

public abstract class ClientMessage implements Serializable {
    static final long serialVersionUID = 100L;
    private final ClientRequest request;
    private long id;
    private SessionId sessionId;

    public ClientMessage(long id, SessionId sessionId, ClientRequest request) {
        this.request = request;
        this.sessionId = sessionId;
        this.id = id;
    }

    public ClientRequest getRequest() {
        return request;
    }
    public long getId() {
        return id;
    }
    public SessionId getSessionId() {
        return sessionId;
    }
}
