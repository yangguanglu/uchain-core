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
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


@Setter
@Getter
public class ForkBase {
	private static final Logger log = LoggerFactory.getLogger(ForkBase.class);
	private ForkItemStore forkStore;
	private Settings settings;
	private ForkItem _head;
	private LevelDbStorage db;
	private FuncConfirmed funcConfirmed;
	private FuncOnSwitch funcOnSwitch;
	private Map<UInt256, ForkItem> indexById = new HashMap();
	private MultiMap<UInt256, UInt256> indexByPrev = new MultiMap();
	private SortedMultiMap2<Integer,Boolean,UInt256> indexByHeight = new SortedMultiMap2<>("asc","reverse");
	private SortedMultiMap2<Integer,Integer,UInt256> indexByConfirmedHeight = new SortedMultiMap2<>("reverse","reverse");


	public ForkBase(Settings settings, FuncConfirmed funcConfirmed,FuncOnSwitch funcOnSwitch) {
        this.settings = settings;
        String path = settings.getChainSettings().getForkBaseSettings().getDir();
        this.db = ConnFacory.getInstance(path);
        forkStore = new ForkItemStore(db,settings.getChainSettings().getForkBaseSettings().getCacheSize(),DataStoreConstant.ForkItemPrefix,new UInt256Key(),new ForkItemValue());
		init();
        this.funcConfirmed = funcConfirmed;
        this.funcOnSwitch = funcOnSwitch;
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
        if(list == null) return null;
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
        if(list == null) return null;
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
	public boolean add(Block block) {
        ThreeTuple<Block,List<ForkItem>, List<ForkItem>> threeTuple = null;
		Map<PublicKey, Integer> lph = Maps.newHashMap();
		if(_head == null) {
			log.info("启动项目时，加入所有见证人");
			List<Witness> witnesses = settings.getConsensusSettings().getWitnessList();
			for(Witness witness : witnesses) {//map中放入所有见证人
				lph.put(PublicKey.apply(new BinaryData(witness.getPubkey())), 0);
			}
            ForkItem item = makeItem(block,lph, true);
            return add(item);
		}else {
			if(!indexById.containsKey(block.id())) {
                ForkItem prev = indexById.get(block.prev());
                if(prev !=null){
                    Map<PublicKey, Integer> prodHeights = prev.getLastProducerHeight();
                    Boolean master = _head.id().equals(block.prev());
                    ForkItem item = makeItem(block,prodHeights, master);
                    return add(item);
                }else
                    return false;
			}else {
			    return false;
            }
		}
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
	private boolean add(ForkItem item) {
        ThreeTuple<Block,List<ForkItem>, List<ForkItem>> threeTuple;
		if (insert(item)) {
            TwoTuple<ForkItem,ForkItem> twoItem = maybeReplaceHead();
            if (twoItem.second.prev().equals(twoItem.first.id())) {
                removeConfirmed(item.confirmedHeight());
            } else if (!twoItem.second.id().equals(twoItem.first.id())) {
                switchAdd(twoItem.first, twoItem.second);
            }
            return true;
		}else {
            return false;
		}
	}

	private TwoTuple maybeReplaceHead(){
        TwoTuple<ForkItem,ForkItem> twoTuple;
        ForkItem old = _head;
        _head = indexById.get(indexByConfirmedHeight.head().third);
        assert(_head != null);
        if(old == null){
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
	private void switchAdd(ForkItem from,ForkItem to) {
		TwoTuple<List<ForkItem>, List<ForkItem>> twoTuple = getForks(from, to);
		List<ForkItem> items = Lists.newArrayList();
		Batch batch = new Batch();
		twoTuple.first.forEach(item -> {
			ForkItem newItem = new ForkItem(item.getBlock(),item.getLastProducerHeight(),false);
            forkStore.set(newItem.getBlock().id(), newItem, batch);
			items.add(newItem);
		});
		twoTuple.second.forEach(item -> {
            ForkItem newItem = new ForkItem(item.getBlock(),item.getLastProducerHeight(),true);
            forkStore.set(newItem.getBlock().id(), newItem, batch);
            items.add(newItem);
        });
		if(db.applyBatch(batch)){
			items.forEach(item -> updateIndex(item));
            funcOnSwitch.onSwitch(twoTuple.first,twoTuple.second);
        }
	}

	public void close() {
		db.close();
	}
	/**
	 * 初始化
	 */

	public void init(){
		val entryList = db.find(forkStore.getPrefixBytes());
		entryList.forEach(entry -> {
			byte[] kData = new byte[forkStore.getPrefixBytes().length];
			System.arraycopy(entry.getKey(), 0, kData, 0, forkStore.getPrefixBytes().length);
			forkStore.getKeyConverter().fromBytes(kData);
			ForkItem forkItem = forkStore.getValConverter().fromBytes(entry.getValue());
			createIndex(forkItem);
		});
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
	private void removeConfirmed(int height) {
		ThreeTuple<Integer, Boolean, UInt256> threeTuple = indexByHeight.head();
		if(threeTuple!=null){
			if(threeTuple.first < height){
				ForkItem item = indexById.get(threeTuple.third);
				if(item !=null && item.isMaster()) {
                    funcConfirmed.onConfirmed(item.getBlock());
				}
				if(item!=null) {
                    Batch batch = new Batch();
                    forkStore.delete(item.getBlock().id(), batch);
                    if (db.applyBatch(batch)) {
                        deleteIndex(item);
                    }
                }
            }
		}
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
		ForkItem prev = get(item.getBlock().prev());
		if(prev == null) {
			throw new UnExpectedError("unexpected error");
		}
		return prev;
	}

	private void createIndex(ForkItem item) {
		Block blk = item.getBlock();
		indexById.put(blk.id(), item);
		indexByPrev.put(blk.prev(), blk.id());
		//log.info("createIndex before************************");
		//log.info(blk.height() +":::::::::::::::::" + blk.id());
		indexByHeight.put(blk.height(), item.isMaster(), blk.id());
		//log.info("createIndex after************************");
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

	public boolean removeFork(UInt256 id){
		ForkItem item = indexById.get(id);
		if(null == item) return false;
		List<ForkItem> queue = new LinkedList<ForkItem>();
		queue.add(item);
		//队列里存放应该是indexByPrev
		/*List<ForkItem> ancestors = getAncestors(indexByPrev.get(id));
		ancestors.forEach(fortItem -> queue.offer(fortItem));*/

		if (removeAll(db.batchWrite(),queue)) {
			queue.forEach(forkItem->deleteIndex(forkItem));
			return true;
		} else {
			return false;
		}
	}

	public List<ForkItem> getAncestors(List<UInt256> ancestors) {
		List<ForkItem> res = new ArrayList<>();
		if(ancestors != null)
			ancestors.forEach(id->{
				ForkItem tmp = indexById.get(id);
				if(tmp != null){
					res.add(tmp);
				}
			});
		return res;
	}

	public Boolean removeAll(Batch batch,List<ForkItem> queue){
		try {
			int i = 0;
			while (i < queue.size()) {
				ForkItem toRemove = queue.get(i);
				UInt256 toRemoveId = toRemove.getBlock().id();
				List<ForkItem> ancestors = getAncestors(indexByPrev.get(toRemoveId));
				if(ancestors != null)
					ancestors.forEach(fortItem -> queue.add(fortItem));
				forkStore.delete(toRemoveId, batch);
				i++;
			}
			return true;
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}
}
