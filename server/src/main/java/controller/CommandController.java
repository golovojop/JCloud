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

    public ServerDirResponse commandDir(ClientDir clientMessage, Session session){
        Path path = clientMessage.getTarget() == null ? session.getCurrentDir() : Paths.get(session.getCurrentDir().toString(), clientMessage.getTarget());
        ServerDirResponse response = new ServerDirResponse(clientMessage.getId(), fileProvider.collectFiles(path));
        return response;
    }

    public ServerDelResponse commandDel(ClientDelFile clientMessage, Session session){
        boolean isDeleted = fileProvider.deleteFile(Paths.get(session.getCurrentDir().toString(), clientMessage.getFileName()));
        return new ServerDelResponse(clientMessage.getId(), isDeleted, fileProvider.collectFiles(session.getCurrentDir()));
    }

    public ServerGetResponse  commandGet(ClientGet clientMessage, Session session) {
        long length = -1;
        Path path = Paths.get(session.getCurrentDir().toString(), clientMessage.getFileName());
        try {
            length = Files.size(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ServerGetResponse(clientMessage.getId(), clientMessage.getFileName(), length);
    }

    public ServerPutReadyResponse commandPutReady(ClientPut clientMessage, Session session) {
        return new ServerPutReadyResponse(clientMessage.getId(), clientMessage.getFileName(), clientMessage.getLength());
    }

    public ServerPutFinishedResponse commandPutFinished(ClientPut clientMessage, Session session) {
        return new ServerPutFinishedResponse(clientMessage.getId(), fileProvider.collectFiles(session.getCurrentDir()));
    }
}
