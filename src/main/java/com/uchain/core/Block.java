package com.uchain.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uchain.common.Serializabler;
import com.uchain.crypto.MerkleTree;
import com.uchain.crypto.UInt160;
import com.uchain.crypto.UInt256;
import lombok.Getter;
import lombok.Setter;
import lombok.val;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
@Getter
public class Block implements Identifier<UInt160>{
    private BlockHeader header;

    private List<Transaction> transactions;

    private UInt256 id;
    private Map<UInt256, Transaction> txMp;


    public Block (BlockHeader header, List<Transaction> transactions){
        this.header = header;
        this.transactions = transactions;
        transactions.forEach(tx ->{
            txMp.put(tx.id(),tx);
        });
    }

    @JsonInclude
    public UInt256 id(){
        return header.id();
    }

    public int height(){
        return header.getIndex();
    }
    
    public UInt256 prev() {
    	return header.getPrevBlock();
    }
    
    public long timeStamp(){
        return header.getTimeStamp();
    }

    public UInt256 merkleRoot() {
        return  MerkleTree.root(transactions.stream().map(tx -> tx.getId()).collect(Collectors.toList()));
    }

    public Transaction getTransaction(UInt256 id){
        return txMp.get(id);
    }

    public Transaction getTransaction(int index){
        if (index < 0 || index >= transactions.size()) return null;
        return transactions.get(index);
    }

    @Override
    public void serialize(DataOutputStream os){
        Serializabler.write(os, header);
        Serializabler.writeSeq(os, transactions);
    }

    public static Block deserialize(DataInputStream is) throws IOException{
        val header = BlockHeader.deserialize(is);
        val txs = readSeq(is);
        return new Block(header, txs);
    }
    
    public static List<Transaction> readSeq(DataInputStream is) throws IOException {
		int size = is.readInt();
		List<Transaction> transactions = new ArrayList<Transaction>();
		for(int i = 0; i < size; i++){
			transactions.add(Transaction.deserialize(is));
		}
		return transactions;
	}
    
    public static Block fromBytes(byte[] data){
        val bs = new ByteArrayInputStream(data);
        val is = new DataInputStream(bs);
        try {
			return deserialize(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
    }

    public static Block build(BlockHeader header, List<Transaction> txs){
        return new Block(header, txs);
    }
}