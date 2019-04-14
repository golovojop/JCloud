package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;

public class ServerAuthResponse extends ServerMessage {
    static final long serialVersionUID = 101L;

    private boolean isAuth;
    private SessionId sessionId;

    public ServerAuthResponse(long id, boolean isAuth, SessionId sessionId) {
        super(id, ServerResponse.SAUTH);
        this.isAuth = isAuth;
        this.sessionId = sessionId;
    }

    public boolean isAuth() {
        return isAuth;
    }

    public SessionId getSessionId() {
        return sessionId;
    }
}
