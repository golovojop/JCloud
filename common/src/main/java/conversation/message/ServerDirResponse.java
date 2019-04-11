package conversation.message;

import conversation.ServerMessage;
import conversation.ServerResponse;
import domain.FileWrapper;

import java.io.Serializable;

public class ServerDirResponse extends ServerMessage implements Serializable {
    private FileWrapper[] files;

    public ServerDirResponse(long id, FileWrapper[] files) {
        super(id, ServerResponse.SDIR);
        this.files = files;
    }

    public FileWrapper[] getFiles() {
        return files;
    }
}
