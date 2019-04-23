package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;
import domain.FileDescriptor;

public class ServerPutResponse extends ServerMessage {
    static final long serialVersionUID = 101L;
    private FileDescriptor[] updatedFileList;

    public ServerPutResponse(long id, FileDescriptor[] updatedFileList) {
        super(id, ServerResponse.SPUT);
        this.updatedFileList = updatedFileList;
    }

    public FileDescriptor[] getUpdatedFileList() {
        return updatedFileList;
    }
}
