package com.cramja.crypto.core;

import java.util.List;

public interface Peer {

    String getId();

    boolean isAlive();

    List<Peer> getPeers();

    void register(Peer peer);

    Block getWorkingBlock();

}
