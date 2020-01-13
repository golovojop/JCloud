package domain;

import java.io.Serializable;

public class FileDescriptor implements Serializable {
    static final long serialVersionUID = 100L;
    private String fileName;
    private long fileSize;

    public FileDescriptor(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }
}
