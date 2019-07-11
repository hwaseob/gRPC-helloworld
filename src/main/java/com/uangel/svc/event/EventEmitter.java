package com.uangel.svc.event;

import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

public class EventEmitter<T> {
    private CopyOnWriteArraySet<T> listeners=new CopyOnWriteArraySet<>();

    public  void register(T cb) {
        if (cb != null)
        {
            listeners.add(cb);
        }
    }

    public  void unregister(T cb) {
        listeners.remove(cb);
    }

    public void fireEvent(Consumer<T> e) {
        listeners.forEach(e);
    }


}
