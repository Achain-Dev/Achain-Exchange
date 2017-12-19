package com.achain.service;

import com.achain.domain.entity.ActTransaction;
import com.baomidou.mybatisplus.service.IService;

import java.util.Map;

/**
 * @author yujianjian
 * @since 2017-12-15 下午4:44
 */
public interface IActTransactionMapperService extends IService<ActTransaction> {
    Map<String, Object> walletAccountTransactionHistory(Long start, String address, String contractId);
}
