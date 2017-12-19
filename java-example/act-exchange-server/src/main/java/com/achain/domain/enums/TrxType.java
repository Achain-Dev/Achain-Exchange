package com.achain.domain.enums;


import java.util.Arrays;

/**
 * @author qiangkz on 2017/6/14.
 */
public enum TrxType {
    //  #账户相关
    //#普通转账
    TRX_TYPE_TRANSFER(0, "普通转账"),
    //#代理领工资
    TRX_TYPE_WITHDRAW_PAY(1, "代理领工资"),
    //#注册账户
    TRX_TYPE_REGISTER_ACCOUNT(2, "注册账户"),
    //#注册代理
    TRX_TYPE_REGISTER_DELEGATE(3, "注册代理"),
    //#升级代理
    TRX_TYPE_UPGRADE_ACCOUNT(4, "升级代理"),
    //#更新账户
    TRX_TYPE_UPDATE_ACCOUNT(5, "更新账户"),

    //#合约相关
    //#注册合约
    TRX_TYPE_REGISTER_CONTRACT(10, "注册合约"),
    //#合约充值
    TRX_TYPE_DEPOSIT_CONTRACT(11, "合约充值"),
    //#合约升级
    TRX_TYPE_UPGRADE_CONTRACT(12, "合约升级"),
    //#合约销毁
    TRX_TYPE_DESTROY_CONTRACT(13, "合约销毁"),
    //#调用合约
    TRX_TYPE_CALL_CONTRACT(14, "调用合约"),
    //#合约出账
    TRX_TYPE_CALCULATE_CONTRACT(15, "合约出账"),;

    private final int key;

    private final String desc;

    TrxType(int key, String desc) {
        this.key = key;
        this.desc = desc;
    }


    public int getIntKey() {
        return key;
    }


    public String getDesc() {
        return desc;
    }

    public static TrxType getTrxType(int value) {
        return Arrays.stream(TrxType.values()).filter(trxType -> trxType.getIntKey() == value).findFirst().orElse(null);
    }
}
