package org.chat21.android.connectivity;

import java.util.Observable;

/**
 * Created by stefanodp91 on 14/09/17.
 */

//bugfix Issue #61
public class ObservableNetwork extends Observable {
    private static ObservableNetwork instance = new ObservableNetwork();

    public static ObservableNetwork getInstance() {
        return instance;
    }

    private ObservableNetwork() {
    }

    public void updateValue(Object data) {
        synchronized (this) {
            setChanged();
            notifyObservers(data);
        }
    }
}