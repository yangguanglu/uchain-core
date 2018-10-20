package com.uchain;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.uchain.core.LevelDBBlockChain;
import com.uchain.core.LevelDBBlockChainBuilder;
import com.uchain.core.producer.Producer;
import com.uchain.main.Settings;
import com.uchain.network.NetworkManager;
import com.uchain.network.Node;
import com.uchain.network.peer.PeerHandlerManager;

import java.io.IOException;

public class Main {

    public static void main(String[] args){

        Settings settings = new Settings(args[0]);

        ActorSystem uchainSystem = ActorSystem.create("uchainSystem");
        ActorRef peerHandlerActor = uchainSystem.actorOf(PeerHandlerManager.props(settings), "peerHandlerManager");

        LevelDBBlockChain chain = LevelDBBlockChainBuilder.populate(settings);

        ActorRef producerActor = uchainSystem.actorOf(Producer.props(settings.getConsensusSettings(),chain,peerHandlerActor));

        ActorRef nodeActor = uchainSystem.actorOf(Node.props(chain, peerHandlerActor,producerActor), "nodeManager");

        uchainSystem.actorOf(NetworkManager.props(settings,peerHandlerActor,nodeActor), "networkManager");

        try {
            JerseyServer.runServer(nodeActor, producerActor, chain);
            System.out.println("OK");
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
