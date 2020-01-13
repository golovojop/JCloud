package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;
import domain.FileDescriptor;

public class ServerDirResponse extends ServerMessage {
    static final long serialVersionUID = 101L;
    private FileDescriptor[] files;

    public ServerDirResponse(long id, FileDescriptor[] files) {
        super(id, ServerResponse.SDIR);
        this.files = files;
    }

    public FileDescriptor[] getFiles() {
        return files;
    }
}
