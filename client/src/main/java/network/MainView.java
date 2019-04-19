package network;

import conversation.ClientMessage;
import conversation.ServerMessage;

public interface MainView {
    void renderResponse(ServerMessage message);
    void updateLocalStoreView();
    void updateRemoteStoreView();

    ClientMessage dequeueMessage();
}
