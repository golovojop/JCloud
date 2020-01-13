package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;

public class ServerAlertResponse extends ServerMessage {

    private String message;

    public ServerAlertResponse(long id, String message) {
        super(id, ServerResponse.SALERT);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
