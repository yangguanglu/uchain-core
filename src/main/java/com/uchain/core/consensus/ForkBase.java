package com.uchain.core.consensus;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uchain.core.Block;
import com.uchain.core.datastore.DataStoreConstant;
import com.uchain.core.datastore.ForkItemStore;
import com.uchain.core.datastore.keyvalue.ForkItemValue;
import com.uchain.core.datastore.keyvalue.UInt256Key;
import com.uchain.crypto.BinaryData;
import com.uchain.crypto.PublicKey;
import com.uchain.crypto.UInt256;
import com.uchain.exceptions.UnExpectedError;
import com.uchain.main.Settings;
import com.uchain.main.Witness;
import com.uchain.storage.Batch;
import com.uchain.storage.ConnFacory;
import com.uchain.storage.LevelDbStorage;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Setter
@Getter
public class ForkBase {
	private static final Logger log = LoggerFactory.getLogger(ForkBase.class);
	private ForkItemStore forkStore;
	private Settings settings;
	private ForkItem _head;
	private LevelDbStorage db;
	private Map<UInt256, ForkItem> indexById = new HashMap();
	private MultiMap<UInt256, UInt256> indexByPrev = new MultiMap();
	private SortedMultiMap2<Integer,Boolean,UInt256> indexByHeight = new SortedMultiMap2<>("asc","reverse");
	private SortedMultiMap2<Integer,Integer,UInt256> indexByConfirmedHeight = new SortedMultiMap2<>("reverse","reverse");


	public ForkBase(Settings settings) {
        this.settings = settings;
        String path = settings.getChainSettings().getForkBaseSettings().getDir();
        this.db = ConnFacory.getInstance(path);
        forkStore = new ForkItemStore(db,settings.getChainSettings().getForkBaseSettings().getCacheSize(),DataStoreConstant.ForkItemPrefix,new UInt256Key(),new ForkItemValue());
		init();
	}

	/**
	 * 当前分叉头
	 * @return
	 */
	public ForkItem head() {
		return _head;
	}

	/**
	 * 根据id获取ForkItem
	 * @param id
	 * @return
	 */
	public ForkItem get(UInt256 id) {
		return indexById.get(id);
	}

	public ForkItem get(int height){
        List<UInt256> list = indexByHeight.get(height,true);
        if(list != null && list.size()>0) {
            UInt256 uInt256 = list.get(0);
            return get(uInt256);
        }else{
            return null;
        }
    }
	/**
	 *
	 * @param id
	 * @return
	 */
	public UInt256 getNext(UInt256 id) {
        List<UInt256> list = indexByPrev.get(id);
        for (int i = 0; i < list.size(); i++) {
            ForkItem current = indexById.get(list.get(i));
            if(current.isMaster())
                return current.getBlock().id();
        }
        return null;
	}

	/**
	 * Block添加到ForkItem
	 * @param block
	 * @return
	 */
	public ThreeTuple<Block,List<ForkItem>, List<ForkItem>> add(Block block) {
        ThreeTuple<Block,List<ForkItem>, List<ForkItem>> threeTuple = null;
		Map<PublicKey, Integer> lph = Maps.newHashMap();
		if(_head == null) {
			log.info("启动项目时，加入所有见证人");
			List<Witness> witnesses = settings.getConsensusSettings().getWitnessList();
			for(Witness witness : witnesses) {//map中放入所有见证人
				lph.put(PublicKey.apply(new BinaryData(witness.getPubkey())), 0);
			}
            ForkItem item = makeItem(block,lph, true);
            threeTuple = add(item);
		}else {
			if(!indexById.containsKey(block.id())) {
                ForkItem prev = indexById.get(block.prev());
                if(prev !=null){
                    Map<PublicKey, Integer> prodHeights = prev.getLastProducerHeight();
                    Boolean master = _head.id().equals(block.prev());
                    ForkItem item = makeItem(block,prodHeights, master);
                    threeTuple = add(item);
                }
			}
		}
		return threeTuple;
	}

	private ForkItem makeItem(Block block,Map<PublicKey,Integer> heights,Boolean master){
	    Map<PublicKey,Integer> lph = Maps.newHashMap();
        heights.forEach((key,value) -> lph.put(key,value));
        PublicKey pub = block.getHeader().getProducer();
        if(lph.containsKey(pub)){
            lph.put(pub,block.height());
        }
        return new ForkItem(block, lph, master);
    }

    /**
     * ForkItem
     * @param item
     * @return
     */
	private ThreeTuple<Block,List<ForkItem>, List<ForkItem>> add(ForkItem item) {
        ThreeTuple<Block,List<ForkItem>, List<ForkItem>> threeTuple;
		if (insert(item)) {
            Block block = null;
            TwoTuple<List<ForkItem>, List<ForkItem>> twoTupleTemp = null;
            TwoTuple<ForkItem,ForkItem> twoItem = maybeReplaceHead();
            if (twoItem.second.prev().equals(twoItem.first.id())) {
                block = removeConfirmed(item.confirmedHeight());
            } else if (!twoItem.second.id().equals(twoItem.first.id())) {
                twoTupleTemp = switchAdd(twoItem.first, twoItem.second);
            }
            if(twoTupleTemp!=null) {
                threeTuple = new ThreeTuple(block, twoTupleTemp.first, twoTupleTemp.second);
            }else{
                threeTuple = new ThreeTuple(block, null, null);
            }
		}else {
            threeTuple = new ThreeTuple(null, null, null);
		}
		return threeTuple;
	}

	private TwoTuple maybeReplaceHead(){
        TwoTuple<ForkItem,ForkItem> twoTuple;
        ForkItem old = _head;
        _head = indexById.get(indexByConfirmedHeight.head().third);
        assert(_head != null);
        if(old ==null){
            twoTuple =new TwoTuple<>(_head,_head);
        }else{
            twoTuple =new TwoTuple<>(old,_head);
        }
        return twoTuple;
    }
	/**
	 * 切换分叉
	 * @param from
	 * @param to
	 */
	private TwoTuple<List<ForkItem>, List<ForkItem>> switchAdd(ForkItem from,ForkItem to) {
		TwoTuple<List<ForkItem>, List<ForkItem>> twoTuple = getForks(from, to);
		List<ForkItem> items = Lists.newArrayList();
		Batch batch = new Batch();
		twoTuple.first.forEach(item -> {
			ForkItem newItem = new ForkItem(item.getBlock(),item.getLastProducerHeight(),false);
            forkStore.set(newItem.getBlock().id(), newItem, batch);
			items.add(item);
		});
		twoTuple.second.forEach(item -> {
            ForkItem newItem = new ForkItem(item.getBlock(),item.getLastProducerHeight(),true);
            forkStore.set(newItem.getBlock().id(), newItem, batch);
            items.add(item);
        });
		if(db.applyBatch(batch)){
			items.forEach(item -> updateIndex(item));
            return twoTuple;
        }
        return null;
	}

	public void close() {
		db.close();
	}
	/**
	 * 初始化
	 */
	private void init() {
        byte[] kData = new byte[forkStore.getPrefixBytes().length];
        System.arraycopy(forkStore.getPrefixBytes(), 0, kData, 0, forkStore.getPrefixBytes().length);
        TwoTuple<byte[], byte[]> twoTuple = db.find(forkStore.getPrefixBytes());
        if(twoTuple!=null) {
            forkStore.getKeyConverter().fromBytes(kData);
            ForkItem forkItem = forkStore.getValConverter().fromBytes(twoTuple.second);
            createIndex(forkItem);
        }
        if (indexByConfirmedHeight.size() == 0) {
            _head = null;
        }else {
            _head = indexById.get(indexByConfirmedHeight.head().third);
        }
	}

	/**
	 * 从ForkBase删除已经已经确认的块
	 * @param height
	 * @return
	 */
	private Block removeConfirmed(int height) {
        Block block = null;
		ThreeTuple<Integer, Boolean, UInt256> threeTuple = indexByHeight.head();
		if(threeTuple!=null){
			Integer first = 0;
			Object firstObject = threeTuple.first;
			if(firstObject instanceof String){
				String firstTemp = (String)firstObject;
				first = Integer.parseInt(firstTemp);
			}else if(firstObject instanceof Integer){
				first = (Integer)firstObject;
			}
			log.info("aaaaaaaaaa="+first+"   "+height);
			if(first < height){
				ForkItem item = indexById.get(threeTuple.third);
				if(item.isMaster()) {
                    block = item.getBlock();
				}
				Batch batch = new Batch();
                forkStore.delete(item.getBlock().id(), batch);
				if(db.applyBatch(batch)) {
                    deleteIndex(item);
                }
            }
		}
		return block;
	}

	/**
	 * 存入ForkBase
	 * @param item
	 * @return
	 */
	private Boolean insert(ForkItem item) {
		if (forkStore.set(item.getBlock().id(), item,null)) {
			createIndex(item);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * x和y两个分叉回溯到分叉点
	 * @param x
	 * @param y
	 * @return
	 */
	private TwoTuple<List<ForkItem>, List<ForkItem>> getForks(ForkItem x,ForkItem y) {
		ForkItem a = x;
		ForkItem b = y;
		TwoTuple<List<ForkItem>, List<ForkItem>> twoTuple;
		if(a.getBlock().id().equals(b.getBlock().id())) {
			List<ForkItem> aList = new ArrayList<>();
			List<ForkItem> bList = new ArrayList<>();
			twoTuple = new TwoTuple<>(aList,bList);
		}else {
			List<ForkItem> xs = new ArrayList<>();
			List<ForkItem> ys = new ArrayList<>();
			while(a.getBlock().height() > b.getBlock().height()) {
				xs.add(a);
				a = getPrev(a);
			}
			while(b.getBlock().height() > a.getBlock().height()) {
				ys.add(b);
				b = getPrev(b);
			}
			while(!a.getBlock().id().equals(b.getBlock().id())) {
				xs.add(a);
				ys.add(b);
				a = getPrev(a);
				b = getPrev(b);
			}
			twoTuple = new TwoTuple<>(xs,ys);
		}
		return twoTuple;
	}

	/**
	 * 获取上一个ForkItem
	 * @param item
	 * @return
	 */
	private ForkItem getPrev(ForkItem item) {
		ForkItem prev = get(item.getBlock().getHeader().getPrevBlock());
		if(prev == null) {
			throw new UnExpectedError("unexpected error");
		}
		return prev;
	}

	private void createIndex(ForkItem item) {
		Block blk = item.getBlock();
		indexById.put(blk.id(), item);
		indexByPrev.put(blk.prev(), blk.id());
		indexByHeight.put(blk.height(), item.isMaster(), blk.id());
		indexByConfirmedHeight.put(item.confirmedHeight(), blk.height(), blk.id());
	}

	private void deleteIndex(ForkItem item) {
		Block blk = item.getBlock();
		indexById.remove(blk.id());
		indexByPrev.remove(blk.prev());
		indexByHeight.remove(blk.height(), item.isMaster());
	    indexByConfirmedHeight.remove(item.confirmedHeight(), blk.height());
	}


	private void updateIndex(ForkItem newItem) {
		UInt256 id = newItem.getBlock().id();
		int height = newItem.getBlock().height();
		boolean branch = newItem.isMaster();
		List<UInt256> list = indexByHeight.remove(height, !branch);
		if(list != null) {
			for (UInt256 u : list) {
				if (!u.equals(id)) {
					indexByHeight.put(height, branch, u);
				}
			}
		}
        indexByHeight.put(height, branch, id);
		indexById.put(id, newItem);
	}
}
