package com.uchain.main;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DataBaseSetting {
    String dir;
    boolean cacheEnabled;
    int cacheSize;

    public DataBaseSetting(String dir, boolean cacheEnabled, int cacheSize){
        this.dir = dir;
        this.cacheEnabled = cacheEnabled;
        this.cacheSize = cacheSize;
    }
}
