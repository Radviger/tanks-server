package gtanks.network.listeners;

import java.util.ArrayList;
import java.util.List;

public class DisconnectListeners {
    private final List<DisconnectListener> listeners = new ArrayList<>();

    public void addListener(DisconnectListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(DisconnectListener listener) {
        this.listeners.remove(listener);
    }

    public void onEvent() {
        for (DisconnectListener listener : this.listeners) {
            listener.onDisconnect();
        }
    }
}
