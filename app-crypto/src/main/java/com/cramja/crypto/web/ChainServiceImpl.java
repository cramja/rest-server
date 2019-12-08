package com.cramja.crypto.web;

import static java.util.stream.Collectors.toList;

import com.cramja.crypto.core.Miner;
import com.cramja.crypto.core.Network;
import com.cramja.rest.core.exc.BadRequestException;
import com.cramja.rest.core.exc.ConflictException;
import com.cramja.rest.core.exc.NotFoundException;
import com.cramja.rest.core.exc.ServerError;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChainServiceImpl implements ChainService {
    private static final Logger logger = LoggerFactory.getLogger(ChainServiceImpl.class);

    private static final int MINER_COUNT = 4;
    private static final AtomicInteger MINER_ID = new AtomicInteger(0);

    private ExecutorService threadPool;
    private Network network = new Network();
    private List<Miner> miners = new LinkedList<>();

    public ChainServiceImpl() {
        this.threadPool = Executors.newFixedThreadPool(
                MINER_COUNT,
                (runnable) ->{
                    Thread t = new Thread(runnable);
                    t.setName("miner-" + MINER_ID.incrementAndGet());
                    t.setUncaughtExceptionHandler((td, ex) -> logger.error("thread {} died", t.getName(), ex));
                    return t;
                });
    }

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
        Miner m = new Miner(1L); // TODO
        if (miners.contains(m)) {
            throw new ConflictException("miner " + name + " already exists");
        } else if (miners.size() > MINER_COUNT) {
            throw new BadRequestException("exceeded max miners: " + MINER_COUNT);
        }

        m.init(network);
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
    public List<Long> listMiners() {
        return miners.stream().map(Miner::getId).collect(toList());
    }

}
