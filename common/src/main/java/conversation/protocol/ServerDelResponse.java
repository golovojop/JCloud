package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;

public class ServerDelResponse extends ServerMessage {
    static final long serialVersionUID = 101L;
    private boolean status;

    public ServerDelResponse(long id, boolean status) {
        super(id, ServerResponse.SDELETE);
        this.status = status;
    }

    public boolean isStatus() {
        return status;
    }
}
