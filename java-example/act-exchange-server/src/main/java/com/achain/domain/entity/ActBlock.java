package com.achain.domain.entity;


import com.baomidou.mybatisplus.annotations.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@TableName("tb_act_block")
@Data
public class ActBlock implements Serializable {
    private static final long serialVersionUID = 1L;

    /***/
    protected Long id;

    /**
     * 区块hash
     */
    protected String blockId;

    /**
     * 块号
     */
    protected Long blockNum;

    /**
     * 块大小（字节）
     */
    protected Long blockSize;

    /**
     * 前一个块块id
     */
    protected String previous;

    /**
     * 块中交易的摘要
     */
    protected String trxDigest;

    /**
     * 上轮secret
     */
    protected String prevSecret;

    /**
     * 本轮secret的hash
     */
    protected String nextSecretHash;

    /**
     * 随机种子
     */
    protected String randomSeed;

    /**
     * 产块者
     */
    protected String signee;

    /**
     * 产块时间
     */
    protected Date blockTime;

    /**
     * 区块内交易数量
     */
    protected Integer transNum;

    /**
     * 区块内交易总金额
     */
    protected Long transAmount;

    /**
     * 区块内交易总手续费
     */
    protected Long transFee;

    /***/
    protected Integer status;

    /***/
    protected Date createTime;

    /***/
    protected Date updateTime;
}