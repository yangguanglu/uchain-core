package com.uchain.core.datastore.keyvalue;import com.uchain.common.Serializable;import com.uchain.crypto.UInt256;import com.uchain.util.Utils;import java.io.DataOutputStream;import java.io.IOException;public class SwitchState implements Serializable {    private UInt256 oldHead;    private UInt256 newHead;    private UInt256 forkPoint;    private Integer height;    public SwitchState(UInt256 oldHead, UInt256 newHead, UInt256 forkPoint, Integer height) {        this.oldHead = oldHead;        this.newHead = newHead;        this.forkPoint = forkPoint;        this.height = height;    }    @Override    public void serialize(DataOutputStream os) {        oldHead.serialize(os);        newHead.serialize(os);        forkPoint.serialize(os);        try {            Utils.writeVarint(height,os);        } catch (IOException e) {            e.printStackTrace();        }    }}