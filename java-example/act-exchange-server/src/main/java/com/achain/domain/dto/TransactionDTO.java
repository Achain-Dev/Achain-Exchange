package com.achain.domain.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * @author yujianjian
 * @since 2017-12-11 上午10:56
 */
@Data
public class TransactionDTO implements Serializable {

    private static final long serialVersionUID = -8050539202820124272L;

    private String trxId;

    private String contractId;

    private String eventType;

    private String eventParam;

    private Long blockNum;

    private Date trxTime;
    /**
     * 调用方法名
     */
    private String callAbi;

    private String fromAddr;

    private Long amount;
    /**
     * 调用参数
     */
    private String apiParams;
}
