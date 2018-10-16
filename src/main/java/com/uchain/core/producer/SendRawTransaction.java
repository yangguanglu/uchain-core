package com.uchain.core.producer;

import com.uchain.crypto.BinaryData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendRawTransaction {
    private BinaryData rawTx;

    public SendRawTransaction(BinaryData rawTx){
        this.rawTx = rawTx;
    }
}
