package com.achain.controller;

import com.achain.conf.Config;
import com.achain.service.IActTransactionMapperService;
import com.achain.service.IBlockchainService;
import com.achain.utils.SDKHttpClient;
import com.alibaba.fastjson.JSONArray;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by qiangkz on 2017/8/15.
 */
@RestController
@RequestMapping("/api")
public class DealParamController {



    @Autowired
    private IActTransactionMapperService actTransactionMapperService;

    @Autowired
    private Config config;

    @Autowired
    private SDKHttpClient httpClient;
    @Autowired
    private IBlockchainService blockchainService;


    /**
     * 查询历史
     *
     * @param start
     * @param address
     * @return
     */
    @RequestMapping(value = "/wallet_address_transaction_history", method = RequestMethod.GET)
    public Map<String, Object> createWalletAccountTransactionHistory(@RequestParam(value = "start") long start,
                                                                     @RequestParam(value = "address") String address,
                                                                     @RequestParam(value = "contract_id",required = false) String contractId) {
        return actTransactionMapperService.walletAccountTransactionHistory(start, address,contractId);

    }

    @RequestMapping(value = "/wallet_transfer_to_address", method = RequestMethod.GET)
    public String walletTransferToAddress(@RequestParam(value = "amount_to_transfer") String amount_to_transfer,
                                          @RequestParam(value = "from_account_name") String from_account_name,
                                          @RequestParam(value = "to_address") String to_address,
                                          @RequestParam(value = "contract_id") String contractId) {
        JSONArray params = new JSONArray();
        String url = config.walletUrl;
        String rpcUser = config.rpcUser;
        String result;
        if (StringUtils.isEmpty(contractId)) {
            params.add(amount_to_transfer);
            params.add("ACT");
            params.add(from_account_name);
            params.add(to_address);
            params.add(contractId);
            result = httpClient.post(url, rpcUser, "wallet_transfer_to_address", params);
        } else {
            String param = to_address + "|" + amount_to_transfer + "|";
            params.add(contractId);
            params.add(from_account_name);
            params.add("transfer_to");
            params.add(param);
            params.add("ACT");
            params.add("1");
            result = httpClient.post(url, rpcUser, "call_contract", params);
        }
        return result;
    }


    /**
     * 查询账户act余额,获得的余额需要除以10的五次方
     * @param actAddress act地址
     * @return 余额
     */
    @GetMapping("balance")
    public Long getBalance(String actAddress){
        if(StringUtils.isEmpty(actAddress)){
            return 0L;
        }
        return blockchainService.getBalance(actAddress);
    }

}
