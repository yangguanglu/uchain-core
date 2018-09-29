package com.uchain;

import akka.actor.ActorRef;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.uchain.core.LevelDBBlockChain;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;

public class APIApplication extends ResourceConfig{

    public APIApplication(ActorRef nodeActor, ActorRef producerActor, LevelDBBlockChain chain){
        packages("jersey");

        //load resource
        register(new RestfulResource(nodeActor, producerActor, chain));

        //register data transfer
        register(JacksonJsonProvider.class);

        //logging
        register(LoggingFilter.class);
    }

}
