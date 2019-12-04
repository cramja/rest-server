package com.cramja.crypto;

import com.cramja.crypto.web.ChainServiceImpl;
import com.cramja.crypto.web.ExceptionInterceptor;
import com.cramja.rest.core.annotations.HttpServiceBinder;
import com.cramja.rest.core.route.Endpoint;
import com.cramja.rest.core.route.RouterImplBuilder;
import com.cramja.rest.core.server.NettyHttpServer;
import java.io.IOException;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    NettyHttpServer server;
    ChainServiceImpl service;

    private App() {}

    public void start() {
        service = new ChainServiceImpl();
        Endpoint endpoint = new RouterImplBuilder()
                .addRoutes(new HttpServiceBinder(service).getBindings())
                .addInterceptor("/", new ExceptionInterceptor())
                .build();
        logger.info("{}", endpoint);
        server = new NettyHttpServer(endpoint::accept);
        server.start();
    }

    public void shutdown() {
        service.stop();
        server.shutdown();
    }

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.start();
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                break;
            }
        }
        app.shutdown();
    }

}
