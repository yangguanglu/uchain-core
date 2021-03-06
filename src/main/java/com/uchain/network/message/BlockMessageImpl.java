package com.uchain.network.message;

import com.uchain.common.Serializabler;
import com.uchain.core.Block;
import com.uchain.crypto.CryptoUtil;
import com.uchain.crypto.UInt160;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;

public class BlockMessageImpl {
	@Getter
	@Setter
	public static class VersionMessage implements PackMessage {
		private MessageType messageType;
		private int height;

		public VersionMessage(int height) {
			this.messageType = MessageType.Version;
			this.height = height;
		}

		@Override
		public MessagePack pack() {
			return new MessagePack(messageType, new BigInteger(height + "").toByteArray(), null);
		}
	}

	@Getter
	@Setter
	public static class GetBlocksMessage implements PackMessage {
		private MessageType messageType;
		private GetBlocksPayload blockHashs;

		public GetBlocksMessage(GetBlocksPayload blockHashs) {
			this.messageType = MessageType.GetBlocks;
			this.blockHashs = blockHashs;
		}

		@Override
		public MessagePack pack() {
			return new MessagePack(messageType, Serializabler.toBytes(blockHashs), null);
		}

	}

	@Getter
	@Setter
	public static class BlockMessage implements PackMessage {
		private MessageType messageType;
		private Block block;

		public BlockMessage(Block block) {
			this.messageType = MessageType.Block;
			this.block = block;
		}

		@Override
		public MessagePack pack() {
			return new MessagePack(messageType, Serializabler.toBytes(block), null);
		}
	}

    @Getter
    @Setter
    public static class BlocksMessage implements PackMessage {
        private MessageType messageType;
        private BlocksPayload blocksPayload;

        public BlocksMessage(BlocksPayload blocksPayload) {
            this.messageType = MessageType.Blocks;
            this.blocksPayload = blocksPayload;
        }

        @Override
        public MessagePack pack() {
            return new MessagePack(messageType, Serializabler.toBytes(blocksPayload), null);
        }
    }

	@Getter
	@Setter
	public static class InventoryMessage implements PackMessage {
		private MessageType messageType;
		private InventoryPayload inv;

		public InventoryMessage(InventoryPayload inv) {
			this.messageType = MessageType.Inventory;
			this.inv = inv;
		}

		@Override
		public MessagePack pack() {
			return new MessagePack(messageType, Serializabler.toBytes(inv), null);
		}
	}

	@Getter
	@Setter
	public static class GetDataMessage implements PackMessage {
		private MessageType messageType;
		private InventoryPayload inv;

		public GetDataMessage(InventoryPayload inv) {
			this.messageType = MessageType.Getdata;
			this.inv = inv;
		}

		@Override
		public MessagePack pack() {
			return new MessagePack(messageType, Serializabler.toBytes(inv), null);
		}
	}

	@Getter
	@Setter
	public static class GetAccountMessage implements PackMessage {
		private MessageType messageType;
		private UInt160 accountAddress;

		public GetAccountMessage(UInt160 accountAddress) {
			this.messageType = MessageType.GetAccount;
			this.accountAddress = accountAddress;
		}

		@Override
		public MessagePack pack() {
			return new MessagePack(messageType,Serializabler.toBytes(accountAddress), null);
		}
	}
}
