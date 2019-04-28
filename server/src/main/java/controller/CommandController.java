package controller;

import conversation.protocol.*;
import data.provider.FileProvider;
import domain.Session;
import server.CloudServer;

import java.io.IOException;
import java.nio.file.Files;
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
        boolean isDeleted = fileProvider.deleteFile(Paths.get(session.getCurrentDir().toString(), request.getFileName()));
        return new ServerDelResponse(request.getId(), isDeleted, fileProvider.collectFiles(session.getCurrentDir()));
    }

    public ServerGetResponse  commandGet(ClientGet request, Session session) {
        long length = -1;
        Path path = Paths.get(session.getCurrentDir().toString(), request.getFileName());
        try {
            length = Files.size(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ServerGetResponse(request.getId(), request.getFileName(), length);
    }

    public ServerPutReadyResponse commandPutReady(ClientPut request, Session session) {
        return new ServerPutReadyResponse(request.getId(), request.getFileName(), request.getLength());
    }

    public ServerPutFinishedResponse commandPutFinished(ClientPut request, Session session) {
        return new ServerPutFinishedResponse(request.getId(), fileProvider.collectFiles(session.getCurrentDir()));
    }
}
