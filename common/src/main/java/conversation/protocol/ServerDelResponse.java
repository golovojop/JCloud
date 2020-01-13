package conversation.protocol;

import conversation.ServerMessage;
import conversation.ServerResponse;
import domain.FileDescriptor;

public class ServerDelResponse extends ServerMessage {
    static final long serialVersionUID = 101L;
    private FileDescriptor[] updatedFileList;
    private boolean status;

    public ServerDelResponse(long id, boolean status, FileDescriptor[] updatedFileList) {
        super(id, ServerResponse.SDELETE);
        this.updatedFileList = updatedFileList;
        this.status = status;
    }

    public FileDescriptor[] getUpdatedFileList() {
        return updatedFileList;
    }

    public boolean isStatus() {
        return status;
    }
}
