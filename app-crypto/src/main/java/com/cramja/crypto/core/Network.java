package com.cramja.crypto.core;

import java.util.LinkedList;
import java.util.List;

public class Network {

    public interface NetworkSubscriber {
        void accept(Block block);

        void accept(Txn txn);
    }

    private List<NetworkSubscriber> subscribers = new LinkedList<>();

    public void broadcast(Block block) {
        for (NetworkSubscriber subscriber : subscribers) {
            subscriber.accept(block);
        }
    }

    public void broadcast(Txn txn) {
        for (NetworkSubscriber subscriber : subscribers) {
            subscriber.accept(txn);
        }
    }

    public void subscribe(NetworkSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(NetworkSubscriber subscriber) {
        subscribers.remove(subscriber);
    }


}
