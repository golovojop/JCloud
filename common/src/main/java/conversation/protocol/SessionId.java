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

    /**
     * TODO: Переопределение методов equals и hashCode
     * TODO: Требуется потому, что SessionID используется как key в HashMap
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SessionId s = (SessionId) o;
        return this.id == s.id;
    }

    @Override
    public int hashCode() {
        return id ^ 0xabbadead;
    }

}
