package com.achain.domain.enums;


import java.util.Arrays;
import java.util.List;

/**
 * Created by qiangkz on 2017/8/23.
 */
public enum ContractCoinType {


    COIN_QUERY_BALANCE(0, "query_balance", "查询余额"),

    COIN_TRANSFER_TO(1, "transfer_to", "转账"),

    COIN_TRANSFER_OUT(2, "transfer_out", "转出"),

    COIN_TRANSFER_IN(3, "transfer_in", "转入"),

    COIN_TRANSFER_ACT(4, "", "转账"),

    COIN_TRANSFER_COIN(1, "transfer", "转账");


    private final int key;
    private final String value;
    private final String desc;

    ContractCoinType(int key, String value, String desc) {
        this.key = key;
        this.value = value;
        this.desc = desc;
    }


    public int getIntKey() {
        return key;
    }


    public String getDesc() {
        return desc;
    }

    public String getValue() {
        return value;
    }

    public static List<String> getCoinTypeMeth() {
        return Arrays.asList(COIN_TRANSFER_TO.value);
    }

    public static ContractCoinType getCoinQueryType(String value) {
        return Arrays.stream(ContractCoinType.values()).filter(contractCoinType -> contractCoinType.value.equals(value))
                     .findFirst().orElse(COIN_TRANSFER_ACT);
    }

}
