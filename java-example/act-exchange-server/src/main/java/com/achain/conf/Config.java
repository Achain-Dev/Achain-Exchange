package com.achain.conf;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yujianjian
 * @since 2017-11-29 下午5:22
 */
@Component
@Slf4j
public class Config {


    @Value("${wallet_url}")
    public String walletUrl;

    @Value("${rpc_user}")
    public String rpcUser;


    @Value("${contract_id}")
    public String contractId;

    @Value("${act_addresses}")
    public String actAddresses;

    public long headerBlockCount;

    public List<String> checkActAddress;

    public List<String> contractIds;



    @PostConstruct
    public void getHeaderBlockCount() {
        checkActAddress = Arrays.asList(actAddresses.split(","));
        contractIds = Arrays.asList(contractId.split(","));
    }


}
