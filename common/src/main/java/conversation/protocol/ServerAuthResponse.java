package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;

public class ServerAuthResponse extends ServerMessage {
    static final long serialVersionUID = 101L;

    private boolean authResult;
    private long sessionId;

    public ServerAuthResponse(long id, boolean authResult, long sessionId) {
        super(id, ServerResponse.SAUTH);
        this.authResult = authResult;
        this.sessionId = sessionId;
    }

    public boolean isAuthResult() {
        return authResult;
    }

    public long getSessionId() {
        return sessionId;
    }
}
