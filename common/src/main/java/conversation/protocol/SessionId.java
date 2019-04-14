package conversation.protocol;

import java.io.Serializable;

public class SessionId implements Serializable {
    private int id;

    public SessionId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String toString() {
        return String.format("%d", id);
    }
}
