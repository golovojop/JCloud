package network;

import conversation.ServerMessage;
import domain.TestSerialization;

public interface MessageHandler {
    void handleMessage(ServerMessage message);
    void handleMessage(TestSerialization message);

}
