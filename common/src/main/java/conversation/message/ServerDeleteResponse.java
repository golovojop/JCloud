package conversation.message;

import conversation.ServerMessage;
import conversation.ServerResponse;
import domain.FileWrapper;

import java.io.Serializable;

public class ServerDeleteResponse extends ServerMessage implements Serializable {

    private FileWrapper[] files;

    public ServerDeleteResponse(long id) {
        super(id, ServerResponse.SDELETE);
    }

    public FileWrapper[] getFiles() {
        return files;
    }
}
