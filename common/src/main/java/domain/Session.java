package domain;


import conversation.protocol.SessionId;

import java.nio.file.Path;

public class Session {
    private SessionId sessionId;
    private Customer customer;
    private Path currentDir;

    public Session(SessionId sessionId, Customer customer, Path currentDir) {
        this.sessionId = sessionId;
        this.customer = customer;
        this.currentDir = currentDir;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Path getDir() {
        return currentDir;
    }
}
