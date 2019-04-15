package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;

public class ServerSignupResponse extends ServerMessage {
    static final long serialVersionUID = 101L;

    private boolean status;

    public ServerSignupResponse(long id, boolean status) {
        super(id, ServerResponse.SSIGNUP);
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }
}
