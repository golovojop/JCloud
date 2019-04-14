package domain;


import conversation.protocol.SessionId;

public class Session {
    private SessionId sessionId;
    private Customer customer;
    private String dir;

    public Session(SessionId sessionId, Customer customer, String dir) {
        this.sessionId = sessionId;
        this.customer = customer;
        this.dir = dir;
    }
}
