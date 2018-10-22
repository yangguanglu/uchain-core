package com.uchain.core.consensus;
/* *************************************************************
 * Copyright  2018 APEX Technologies.Co.Ltd. All rights reserved.
 *
 * FileName: SwitchResult
 *
 * @Author: bridge.bu@chinapex.com 2018/10/18 14:17
 *
 * @Version: 1.0
 * *************************************************************/

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SwitchResult {
    private boolean succeed;
    private ForkItem failedItem;

    public SwitchResult(boolean succeed, ForkItem failedItem){
        this.failedItem = failedItem;
        this.succeed = succeed;
    }

    public SwitchResult(boolean succeed){
        this.failedItem = null;
        this.succeed = succeed;
    }

}
