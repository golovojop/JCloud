package controller;

import conversation.protocol.ClientDir;
import conversation.protocol.ServerDirResponse;
import domain.FileDescriptor;
import server.CloudServer;

public class CommandController {

    CloudServer server;

    public CommandController(CloudServer server) {
        this.server = server;
    }

    public ServerDirResponse commandDir(ClientDir request){
        ServerDirResponse response = new ServerDirResponse(request.getId(), new FileDescriptor[]{
                new FileDescriptor("File1", 10),
                new FileDescriptor("File2", 20),
                new FileDescriptor("File3", 30),
        });

        return response;

    }
}
