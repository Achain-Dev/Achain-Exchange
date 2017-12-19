package com.achain.domain.entity;


import com.baomidou.mybatisplus.annotations.TableName;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@TableName("tb_act_transaction")
@Data
public class ActTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /***/
    protected Long id;

    /**
     * 交易id
     */
    protected String trxId;

    /**
     * 区块hash
     */
    protected String blockId;

    /**
     * 块号
     */
    protected Long blockNum;

    /**
     * 交易在块中的位置
     */
    protected Integer blockPosition;

    /**
     * 0 - 普通转账
     * 1 - 代理领工资
     * 2 - 注册账户
     * 3 - 注册代理
     * 10 - 注册合约
     * 11 - 合约充值
     * 12 - 合约升级
     * 13 - 合约销毁
     * 14 - 调用合约
     * 15 - 合约出账
     */
    protected Integer trxType;

    /***/
    protected String coinType;

    /***/
    protected String contractId;

    /**
     * 发起账号
     */
    protected String fromAcct;

    /**
     * 发起地址
     */
    protected String fromAddr;

    /**
     * 接收账号
     */
    protected String toAcct;

    /**
     * 接收地址
     */
    protected String toAddr;

    /***/
    protected String subAddress;

    /**
     * 金额
     */
    protected Long amount;

    /**
     * 手续费
     * 如果是合约交易，包含gas消耗，注册保证金等
     */
    protected Integer fee;

    /**
     * 备注
     */
    protected String memo;

    /**
     * 交易时间
     */
    protected Date trxTime;

    /**
     * 调用的合约函数，非合约交易该字段为空
     */
    protected String calledAbi;

    /**
     * 调用合约函数时传入的参数，非合约交易该字段为空
     */
    protected String abiParams;

    /***/
    protected String eventType;

    /***/
    protected String eventParam;

    /**
     * 结果交易id
     * 仅针对合约交易
     */
    protected String extraTrxId;

    /**
     * 合约调用结果
     * 0 - 成功
     * 1- 失败
     */
    protected Byte isCompleted;

    /***/
    protected Date createTime;

    /***/
    protected Date updateTime;


}