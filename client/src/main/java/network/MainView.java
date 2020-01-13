package network;

import conversation.ClientMessage;
import conversation.ServerMessage;

public interface MainView {
    void renderResponse(ServerMessage message);
    void updateLocalStoreView();
    void updateRemoteStoreView();
    void startProgressView();
    void stopProgressView();
    void updateProgressView(Double progress);

    ClientMessage dequeueMessage();
}
