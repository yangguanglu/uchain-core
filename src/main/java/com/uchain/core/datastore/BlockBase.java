package com.uchain.core.datastore;

import com.uchain.core.Block;
import com.uchain.core.BlockHeader;
import com.uchain.core.datastore.keyvalue.*;
import com.uchain.crypto.UInt256;
import com.uchain.main.BlockBaseSetting;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;
import lombok.Getter;
import lombok.Setter;
import org.iq80.leveldb.WriteBatch;

@Getter
@Setter
public class BlockBase{

    LevelDbStorage db;
    BlockBaseSetting settings;

    private BlockStore blockStore;

    private HeadBlockStore headBlkStore;

    private HeightStore heightStore;

    public BlockBase(BlockBaseSetting settings){
        this.settings = settings;
        this.db = ConnFacory.getInstance(settings.getDir());
        this.blockStore = new BlockStore(db, settings.getCacheSize(),DataStoreConstant.BlockPrefix, new UInt256Key(),
                new BlockValue());
        this.headBlkStore = new HeadBlockStore(db, DataStoreConstant.HeadBlockStatePrefix,
                new BlockHeaderValue());

        this.heightStore = new HeightStore(db, settings.getCacheSize(), DataStoreConstant.HeightToIdIndexPrefix,
                new IntKey(), new UInt256Value());
    }

    public  void add (Block block){
        if (head().id().toString().equals(block.prev().toString())){
            WriteBatch batchWrite = db.getBatchWrite();
            blockStore.set(block.id(), block, batchWrite);
            heightStore.set(block.height(), block.id(), batchWrite);
            headBlkStore.set(block.getHeader(), batchWrite);
            db.BatchWrite(batchWrite);
        }
        else throw new IllegalArgumentException("requirement failed");
    }

    public BlockHeader head(){
        return headBlkStore.get();
    }

    public Block getBlock(UInt256 id){
        return blockStore.get(id);
    }

    public Block getBlock(int height){
        UInt256 key = heightStore.get(height);
        return getBlock(key);
    }
}
