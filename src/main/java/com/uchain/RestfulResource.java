package com.uchain;

import akka.actor.ActorRef;
import com.uchain.core.LevelDBBlockChain;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

@Singleton
@Path("")
public class RestfulResource {

    ActorRef nodeActor;
    ActorRef producerActor;
    LevelDBBlockChain chain;

    public RestfulResource(ActorRef nodeActor, ActorRef producerActor, LevelDBBlockChain chain){
        this.nodeActor = nodeActor;
        this.producerActor = producerActor;
        this.chain = chain;
    }

    @GET
    @Path("{param:getblocks}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String getBlocks(@PathParam("param") String requestParam, @QueryParam("query") String query,
                                        @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        System.out.println("**************");
        System.out.println(CommandReceiverService.getBlocks(query, nodeActor, producerActor, chain));
        System.out.println("**************");
        String responseStr = requestParam + "[" + query + "]";
        return responseStr;
    }

    @GET
    @Path("{param:getblock}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String getBlock(@PathParam("param") String requestParam, @QueryParam("query") String query,
                                  @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        String responseStr = CommandReceiverService.getBlock(query, nodeActor, producerActor);
        return responseStr;
    }

    @GET
    @Path("{param:getblockcount}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String getBlockCount(@PathParam("param") String requestParam, @QueryParam("query") String heigth,
                           @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        String responseStr = requestParam + "[" + heigth + "]";
        return responseStr;
    }

    @POST
    @Path("{param:produceblock}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String postProduceBlock(@PathParam("param") String requestParam, @QueryParam("query") String heigth,
                                @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        String responseStr = requestParam + "[" + heigth + "]";
        return responseStr;
    }

}
