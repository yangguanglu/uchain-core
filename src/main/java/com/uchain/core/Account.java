package com.uchain.core;

import com.google.common.collect.Maps;
import com.uchain.common.Serializabler;
import com.uchain.crypto.*;
import com.uchain.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

@Getter
@Setter
public class Account implements Identifier<UInt160> {
	private boolean active;  // 账户状态
	private String name;     //12字符串名字
	private Map<UInt256, Fixed8> balances; //UInt256资产类型，Fixed8资产余额
	private Long nextNonce; //  没发起一笔交易，增加1
	private int version;   //  预留
	private UInt160 id;   //

	public Account(boolean active, String name,Map<UInt256, Fixed8> balances, long nextNonce, int version, UInt160 id) {
		this.name = name;
		this.active = active;
		this.balances = balances;
		this.nextNonce = nextNonce;
		this.version = version;
		this.id = id;
	}


	public Fixed8 getBalance(UInt256 assetID) {
		return balances.getOrDefault(assetID, Fixed8.Zero);
	}

	private void serializeExcludeId(DataOutputStream os) {
		Map<UInt256, Fixed8> map = Maps.newLinkedHashMap();
		try {
			os.writeInt(version);
			os.writeBoolean(active);
			Serializabler.writeString(os, name);
			balances.forEach((key, value) -> {
				if (value.getValue() > Fixed8.Zero.getValue()) {
					map.put(key, value);
				}
			});
			Serializabler.writeMap(os, map);
			os.writeLong(nextNonce);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void serialize(DataOutputStream os) {
		serializeExcludeId(os);
		Serializabler.write(os, id());
	}

	private UInt160 genId() {
		val bs = new ByteArrayOutputStream();
		val os = new DataOutputStream(bs);
		serializeExcludeId(os);
		return UInt160.fromBytes(Crypto.hash160(bs.toByteArray()));
	}

	public static Account deserialize(DataInputStream is) {
		try {
			val version = is.readInt();
			val active = is.readBoolean();
			val name = Serializabler.readString(is);
			Map<UInt256, Fixed8> balances = readMap(is);
			val nextNonce = is.readLong();
			val id = UInt160.deserialize(is);
			return new Account(active,name,balances,nextNonce,version,id);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<UInt256, Fixed8> readMap(DataInputStream is) throws IOException {
		Map<UInt256, Fixed8> map = Maps.newLinkedHashMap();
		val size = Utils.readVarInt(is);
		for (int i = 1; i <= size; i++) {
			map.put(UInt256.deserialize(is), Fixed8.deserialize(is));
		}
		return map;
	}
	
	@Override
	public UIntBase id() {
		if (id == null) {
			id = genId();
		}
		return id;
	}

}
