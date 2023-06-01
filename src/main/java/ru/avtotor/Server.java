package ru.avtotor;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.avtotor.request.endpoint.Endpoint;
import ru.avtotor.request.params.Params;
import ru.avtotor.service.HandlerService;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

public class Server {

    private static final String host = "localhost";
    private static final int port = 4567;

    @Getter
    private static final Logger logger = LogManager.getLogger(Server.class);

    public static void main(String[] args) throws IOException, InvocationTargetException, IllegalAccessException {

        logger.info("Starting server");
        HttpServer server = HttpServer.create(new InetSocketAddress(host, port), 0);

        for (Method method : HandlerService.class.getDeclaredMethods()) {
            Endpoint endpoint = method.getAnnotation(Endpoint.class);
            Params params = method.getAnnotation(Params.class);

            server.createContext(endpoint.endpoint(),
                    (HttpHandler) method.invoke(endpoint, endpoint.endpoint(), endpoint.method().getValue(), params));
        }

        server.setExecutor(null);
        server.start();
    }
}