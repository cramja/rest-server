package com.cramja.crypto.web;

import static java.util.stream.Collectors.toList;

import com.cramja.crypto.core.Miner;
import com.cramja.rest.core.exc.BadRequestException;
import com.cramja.rest.core.exc.ConflictException;
import com.cramja.rest.core.exc.NotFoundException;
import com.cramja.rest.core.exc.ServerError;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ChainServiceImpl implements ChainService {

    private static final int MAX_MINERS = 8;

    private ExecutorService threadPool = Executors.newFixedThreadPool(MAX_MINERS);
    private List<Miner> miners = new LinkedList<>();

    public void stop() {
        for (Miner m : miners) {
            m.stop();
        }
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                throw new ServerError("failed to shutdown miners");
            }
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Override
    public void createMiner(String name) {
        Miner m = new Miner(name);
        if (miners.contains(m)) {
            throw new ConflictException("miner " + name + " already exists");
        } else if (miners.size() > MAX_MINERS) {
            throw new BadRequestException("exceeded max miners: " + MAX_MINERS);
        }

        if (!miners.isEmpty()) {
            m.register(miners.get(0));
        }
        miners.add(m);
        threadPool.submit(m);
    }

    @Override
    public void deleteMiner(String name) {
        for (Miner m : miners) {
            if (m.getId().equals(name)) {
                m.stop();
                return;
            }
        }
        throw new NotFoundException("miner '" + name + "' was not found");
    }

    @Override
    public List<String> listMiners() {
        return miners.stream().map(Miner::getId).collect(toList());
    }

}
