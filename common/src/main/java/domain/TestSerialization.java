package domain;

import java.io.Serializable;

public class TestSerialization implements Serializable {
    static final long serialVersionUID = 100L;
    private String text;
    private long value;

    public TestSerialization(String text, long value) {
        this.text = text;
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public long getValue() {
        return value;
    }
}
