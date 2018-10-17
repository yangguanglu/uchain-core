package com.uchain;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import com.uchain.common.Serializabler;
import com.uchain.core.Account;
import com.uchain.core.Block;
import com.uchain.core.LevelDBBlockChain;
import com.uchain.core.producer.SendRawTransaction;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.CryptoUtil;
import com.uchain.crypto.PublicKeyHash;
import com.uchain.crypto.UInt160;
import com.uchain.network.message.BlockMessageImpl;
import lombok.val;
import scala.Serializable;
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
        val blocks = CommandReceiverService.getBlocks(query, nodeActor, producerActor, chain);
        System.out.println(blocks);
        String responseStr = requestParam + "[" + blocks + "]";
        return responseStr;
    }

    @GET
    @Path("{param:getblock}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String getBlock(@PathParam("param") String requestParam, @QueryParam("query") String query,
                                  @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        Block block = CommandReceiverService.getBlock(query, nodeActor, producerActor, chain);
        if(block == null) return "no such block";
        try {
            String blockInfo = Serializabler.JsonMapperTo(block);
            return blockInfo;
        }
        catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    @GET
    @Path("{param:getaccount}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String getAccount(@PathParam("param") String requestParam, @QueryParam("query") String query,
                           @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        String accountAddress = CommandReceiverService.getAccount(query, nodeActor, producerActor);
        UInt160 address = PublicKeyHash.fromAddress(accountAddress);
        Future f = Patterns.ask(nodeActor, new BlockMessageImpl.GetAccountMessage(address), 1000);
        try {
            Account re = (Account) Await.result(f, Duration.create(6, TimeUnit.SECONDS));
            return Serializabler.JsonMapperTo(re);
        }
        catch (Exception e){
            e.printStackTrace();
            return "false";
        }
    }

    @GET
    @Path("{param:getblockcount}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public int getBlockCount(@PathParam("param") String requestParam, @QueryParam("query") String heigth,
                           @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        return chain.getHeight();
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

    @GET
    @Path("{param:sendrawtransaction}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.TEXT_PLAIN)
    public String sendRawTransaction(@PathParam("param") String requestParam, @QueryParam("query") String query,
                                   @Context Request request, @Context UriInfo uriInfo, @Context HttpHeaders httpHeaders){
        BinaryData rawTx = CommandReceiverService.sendRawTransaction(query, nodeActor, producerActor, chain);

        //BinaryData tx = CryptoUtil.fromHexString(rawTx);
        Future f = Patterns.ask(producerActor, new SendRawTransaction(rawTx), 1000);
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
