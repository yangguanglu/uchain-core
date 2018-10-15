package com.uchain;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import com.uchain.core.LevelDBBlockChain;
import com.uchain.core.producer.SendRawTransaction;
import lombok.val;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.concurrent.TimeUnit;

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
        val blocks = CommandReceiverService.getBlocks(query, nodeActor, producerActor, chain);
        System.out.println(blocks);
        System.out.println("**************");
        String responseStr = requestParam + "[" + blocks + "]";
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

    @POST
    @Path("{param:sendrawtransaction}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String sendRawTransaction(@PathParam("param") String requestParam, @QueryParam("query") String query,
                                   @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        String rawTx = CommandReceiverService.sendRawTransaction(query, nodeActor, producerActor, chain);

        Future f = Patterns.ask(producerActor, new SendRawTransaction(), 1000);
        try {
            boolean re = (boolean) Await.result(f, Duration.create(6, TimeUnit.SECONDS));

            return String.valueOf(re);
        }
        catch (Exception e){
            e.printStackTrace();
            return "false";
        }
    }

}
