package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;
import domain.FileDescriptor;

public class ServerDeleteResponse extends ServerMessage {
    static final long serialVersionUID = 101L;
    private FileDescriptor[] files;

    public ServerDeleteResponse(long id) {
        super(id, ServerResponse.SDELETE);
    }
    public FileDescriptor[] getFiles() {
        return files;
    }
}
