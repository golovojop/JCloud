package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;
import domain.FileDescriptor;

public class ServerPutFinishedResponse extends ServerMessage {
    static final long serialVersionUID = 101L;
    private FileDescriptor[] updatedFileList;

    public ServerPutFinishedResponse(long id, FileDescriptor[] updatedFileList) {
        super(id, ServerResponse.SPUT_FINISH);
        this.updatedFileList = updatedFileList;
    }

    public FileDescriptor[] getUpdatedFileList() {
        return updatedFileList;
    }

}
