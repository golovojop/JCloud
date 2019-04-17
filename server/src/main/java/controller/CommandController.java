package controller;

import conversation.protocol.ClientDir;
import conversation.protocol.ServerDirResponse;
import conversation.protocol.ServerSignupResponse;
import data.provider.FileProvider;
import domain.FileDescriptor;
import domain.Session;
import server.CloudServer;

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
        String path = request.getTarget() != null ? request.getTarget() : session.getDir().toString();
        FileDescriptor[] fd = fileProvider.collectFiles(Paths.get(storageRoot, path));

        ServerDirResponse response = new ServerDirResponse(request.getId(), fileProvider.collectFiles(Paths.get(storageRoot, path)));
        return response;
    }

}
