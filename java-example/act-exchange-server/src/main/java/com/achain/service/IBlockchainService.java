package com.achain.service;


import com.achain.domain.dto.TransactionDTO;
import com.alibaba.fastjson.JSONArray;

import java.util.Map;

/**
 * @author fyk
 * @since 2017-11-29 19:09
 */
public interface IBlockchainService {

    /**
     * 获取当前最新区块
     *
     * @return 最新区块号
     */

    long getBlockCount();

    /**
     * 获取一个区块的数据信息
     *
     * @param blockNum 区块号
     * @return 这个区块上的交易
     */

    JSONArray getBlock(long blockNum);

    /**
     * 根据交易单号获取给指定地址打SMC币的地址
     *
     * @param trxId 交易单号
     * @return Map中包含两个参数，from是打币者的地址，amount是打币者的打币数量
     */
    TransactionDTO getTransaction(long blockNum, String trxId);


    Map<String, JSONArray> saveActBlock(String blocknum);



    /**
     * 保存一个块上数据
     * @param map
     */
    void saveTransactions(Map<String, JSONArray> map);

    /**
     * 查询地址余额
     * @param actAddress　act地址
     */
    long getBalance(String actAddress);


}
