package controller;

import conversation.protocol.*;
import data.provider.FileProvider;
import domain.FileDescriptor;
import domain.Session;
import server.CloudServer;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CommandController {

    private FileProvider fileProvider;
    private CloudServer server;
    private String storageRoot;

    public CommandController(CloudServer server, FileProvider fileProvider, String storageRoot) {
        this.server = server;
        this.fileProvider = fileProvider;
        this.storageRoot = storageRoot;
    }

    public ServerDirResponse commandDir(ClientDir request, Session session){
        Path path = request.getTarget() == null ? session.getCurrentDir() : Paths.get(session.getCurrentDir().toString(), request.getTarget());
        ServerDirResponse response = new ServerDirResponse(request.getId(), fileProvider.collectFiles(path));
        return response;
    }

    public ServerDelResponse commandDel(ClientDelFile request, Session session){
        return new ServerDelResponse(request.getId(), fileProvider.deleteFile(Paths.get(session.getCurrentDir().toString(), request.getFileName())));
    }

}
