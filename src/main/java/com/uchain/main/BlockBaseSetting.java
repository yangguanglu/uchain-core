package com.uchain.main;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlockBaseSetting {
    String dir;
    boolean cacheEnabled;
    int cacheSize;

    public BlockBaseSetting(String dir, boolean cacheEnabled, int cacheSize){
        this.dir = dir;
        this.cacheEnabled = cacheEnabled;
        this.cacheSize = cacheSize;
    }
}
