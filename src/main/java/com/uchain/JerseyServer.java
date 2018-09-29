package com.uchain;

import akka.actor.ActorRef;
import com.uchain.core.LevelDBBlockChain;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;

public class JerseyServer {
    public static final String BASE_URI = "http://localhost:1943/";

    public static HttpServer startServer(ActorRef nodeActor, ActorRef producerActor, LevelDBBlockChain chain){
        final APIApplication apiApplication = new APIApplication(nodeActor, producerActor,chain);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), apiApplication);
    }

    public static void runServer(ActorRef nodeActor, ActorRef producerActor, LevelDBBlockChain chain) throws IOException{
        final HttpServer server = startServer(nodeActor, producerActor, chain);
        System.out.println("Jersey server started...");
        System.in.read();
        server.shutdown();
    }
}
