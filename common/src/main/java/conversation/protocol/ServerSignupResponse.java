package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;

public class ServerSignupResponse extends ServerMessage {
    static final long serialVersionUID = 101L;

    private String message;
    private boolean status;

    public ServerSignupResponse(long id, boolean status, String message) {
        super(id, ServerResponse.SSIGNUP);
        this.status = status;
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
