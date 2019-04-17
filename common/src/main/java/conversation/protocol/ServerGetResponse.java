package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;

public class ServerGetResponse extends ServerMessage {
    private String fileName;
    private boolean status;

    public ServerGetResponse(long id, String fileName, boolean status) {
        super(id, ServerResponse.SGET);
        this.fileName = fileName;
        this.status = false;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isStatus() {
        return status;
    }
}
