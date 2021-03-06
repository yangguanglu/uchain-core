package com.uchain.network.peer;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.io.Tcp;
import com.uchain.crypto.UInt256;
import com.uchain.main.Settings;
import com.uchain.network.NetworkUtil.*;
import com.uchain.network.message.BlockMessageImpl.BlockMessage;
import com.uchain.network.message.BlockMessageImpl.GetBlocksMessage;
import com.uchain.network.message.BlockMessageImpl.InventoryMessage;
import com.uchain.network.message.GetBlocksPayload;
import com.uchain.network.message.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;

public class PeerHandlerManager extends AbstractActor{
	private static Logger log = LoggerFactory.getLogger(PeerHandlerManager.class);
	private Settings settings;
	private ActorRef tcpManager;
	
	//握手成功
	private Map<InetSocketAddress, ConnectedPeer> connectedPeers = new HashMap<InetSocketAddress, ConnectedPeer>();

	//握手前
	private Set<InetSocketAddress> connectingPeers = new HashSet<InetSocketAddress>();
	  
	
	public PeerHandlerManager(Settings settings) {
		this.settings = settings;
	}
	
	public static Props props(Settings settings) {
		return Props.create(PeerHandlerManager.class,settings);
	}


	@Override
	public void preStart() throws Exception {
	    if (tcpManager == null) {
			tcpManager = Tcp.get(getContext().system()).manager();
        }
	}
	
	@Override
	public Receive createReceive() {
		return receiveBuilder()
		      .match(DoConnecting.class, msg -> {
					log.info(msg.getRemote() + ":" + msg.getDirection());
					ActorRef peerConnectionHandler = getSender();
					boolean isIncoming = true;
					if (msg.getDirection() != null) {
						isIncoming = false;
					}
					boolean isAlreadyConnecting = connectingPeers.contains(msg.getRemote());
					if (isAlreadyConnecting && !isIncoming) {
						log.info("尝试连接两次 " + msg.getRemote() + ", 将删除重复连接");
						peerConnectionHandler.tell(new CloseConnection(), getSelf());
					} else {
						if (!isIncoming) {
							log.info("远程链接 " + msg.getRemote());
							connectingPeers.add(msg.getRemote());
						}
						peerConnectionHandler.tell(new StartInteraction(), getSelf());
					}
		      })
		      .match(Handshaked.class, msg -> {
		    	  ConnectedPeer peer = msg.getConnectedPeer();
		    	  if(peer.getDirection() !=null && settings.getBindAddress().equals(peer.getSocketAddress().getAddress()+":"+peer.getSocketAddress().getPort())) {
		    		  peer.getHandlerRef().tell(new CloseConnection(), getSelf());
		    	  }
		    	  connectedPeers.put(peer.getSocketAddress(), peer);
//		          log.info("更新本节点连接的节点="+connectedPeers);
                  Thread.sleep(100);
                  peer.getHandlerRef().tell(new GetBlocksMessage(new GetBlocksPayload(Arrays.asList( UInt256.Zero()), UInt256.Zero())).pack(), getSelf());
	          })
		      .match(PeerHandler.class, msg -> {
		    	  ActorRef handler = msg.getHandlerRef();
		    	  Message message = new Message("1","message_test");
		          //获取远程hangler，测发送消息
		          handler.tell(message, getSelf());
	          })
		      .match(Disconnected.class, msg -> {
		    	  connectedPeers.remove(msg.getRemoteAddress());
		    	  connectingPeers.remove(msg.getRemoteAddress());
	          })
		      .match(BlockMessage.class, msg -> {
		    	  log.info("broadcasting BlockMessage:");
		    	  connectedPeers.forEach((socketAddress, connectedPeer) -> {
		    		  connectedPeer.getHandlerRef().tell(msg.pack(), getSelf());
		    		  log.info("send block "+msg.getBlock().height()+"("+msg.getBlock().id()+")" /*to "+connectedPeer.toString()*/);
		    	  });
	          })
		      .match(InventoryMessage.class, msg -> {
		    	  connectedPeers.forEach((socketAddress, connectedPeer) -> {
		    		  connectedPeer.getHandlerRef().tell(msg.pack(), getSelf());
		    	  });
	          })
		      .match(MessagePack.class, msg -> {
		    	  if(msg.getAddress() !=null) {
		    		  ConnectedPeer peer = connectedPeers.get(msg.getAddress());
		    		  if(peer != null) {
		    			  peer.getHandlerRef().tell(new MessagePack(msg.getMessageType(),msg.getData(),null),getSelf());
		    		  }else {
		    			  log.error("peer("+msg.getAddress()+") not exists");
		    		  }
		    	  }else {
		    		  connectedPeers.forEach((socketAddress, connectedPeer) -> {
			    		  connectedPeer.getHandlerRef().tell(new MessagePack(msg.getMessageType(),msg.getData(),null),getSelf());
			    	  });
		    	  }
	          })
		      .build();
	}
}
