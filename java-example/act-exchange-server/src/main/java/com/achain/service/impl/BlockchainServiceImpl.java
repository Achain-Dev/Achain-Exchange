package com.achain.service.impl;


import com.achain.conf.Config;
import com.achain.domain.dto.TransactionDTO;
import com.achain.domain.entity.ActBlock;
import com.achain.domain.entity.ActTransaction;
import com.achain.domain.enums.ContractCoinType;
import com.achain.domain.enums.TaskDealStatus;
import com.achain.domain.enums.TrxType;
import com.achain.service.IActBlockMapperService;
import com.achain.service.IActTransactionMapperService;
import com.achain.service.IBlockchainService;
import com.achain.utils.SDKHttpClient;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * @author fyk
 * @since 2017-11-29 19:13
 */
@Service
@Slf4j
public class BlockchainServiceImpl implements IBlockchainService {

    @Autowired
    private SDKHttpClient httpClient;
    @Autowired
    private Config config;
    @Autowired
    private IActBlockMapperService actBlockMapperService;
    @Autowired
    private IActTransactionMapperService actTransactionMapperService;


    @Override
    public long getBlockCount() {
        log.info("BlockchainServiceImpl|getBlockCount 开始处理");
        String result =
            httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_block_count", new JSONArray());
        JSONObject createTaskJson = JSONObject.parseObject(result);
        return createTaskJson.getLong("result");
    }

    @Override
    public JSONArray getBlock(long blockNum) {
        log.info("BlockchainServiceImpl|getBlock 开始处理[{}]", blockNum);
        String result =
            httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_block", String.valueOf(blockNum));
        JSONObject createTaskJson = JSONObject.parseObject(result);
        return createTaskJson.getJSONObject("result").getJSONArray("user_transaction_ids");
    }


    /**
     * 需要判断交易类型，合约id，合约调用的方法和转账到的地址。
     *
     * @param trxId 交易单号
     */
    @Override
    public TransactionDTO getTransaction(long blockNum, String trxId) {
        try {
            log.info("BlockchainServiceImpl|getBlock 开始处理[{}]", trxId);
            String result = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_transaction", trxId);
            JSONObject createTaskJson = JSONObject.parseObject(result);
            JSONArray resultJsonArray = createTaskJson.getJSONArray("result");
            JSONObject operationJson = resultJsonArray.getJSONObject(1)
                                                      .getJSONObject("trx")
                                                      .getJSONArray("operations")
                                                      .getJSONObject(0);
            //判断交易类型
            String operationType = operationJson.getString("type");
            //不是合约调用就忽略
            if (!"transaction_op_type".equals(operationType)) {
                return null;
            }

            JSONObject operationData = operationJson.getJSONObject("data");
            log.info("BlockchainServiceImpl|operationData={}", operationData);

            String resultTrxId =
                resultJsonArray.getJSONObject(1).getJSONObject("trx").getString("result_trx_id");
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(StringUtils.isEmpty(resultTrxId) ? trxId : resultTrxId);
            log.info("getTransaction|transaction_op_type|[blockId={}][trxId={}][result_trx_id={}]", blockNum, trxId,
                     resultTrxId);
            String resultSignee =
                httpClient
                    .post(config.walletUrl, config.rpcUser, "blockchain_get_pretty_contract_transaction", jsonArray);
            JSONObject resultJson2 = JSONObject.parseObject(resultSignee).getJSONObject("result");
            //和广播返回的统一
            String origTrxId = resultJson2.getString("orig_trx_id");
            Integer trxType = Integer.parseInt(resultJson2.getString("trx_type"));

            Date trxTime = dealTime(resultJson2.getString("timestamp"));
            JSONArray reserved = resultJson2.getJSONArray("reserved");
            JSONObject temp = resultJson2.getJSONObject("to_contract_ledger_entry");
            String contractId = temp.getString("to_account");
            //不是游戏的合约id就忽略
            if (!config.contractId.equals(contractId)) {
                return null;
            }
            TrxType type = TrxType.getTrxType(trxType);
            if (TrxType.TRX_TYPE_DEPOSIT_CONTRACT == type) {
                TransactionDTO transactionDTO = new TransactionDTO();
                transactionDTO.setTrxId(origTrxId);
                transactionDTO.setBlockNum(blockNum);
                transactionDTO.setTrxTime(trxTime);
                transactionDTO.setContractId(contractId);
                //transactionDTO.setCallAbi(ContractGameMethod.RECHARGE.getValue());
                return transactionDTO;
            } else if (TrxType.TRX_TYPE_CALL_CONTRACT == type) {
                String fromAddr = temp.getString("from_account");
                Long amount = temp.getJSONObject("amount").getLong("amount");
                String callAbi = reserved.size() >= 1 ? reserved.getString(0) : null;
                String apiParams = reserved.size() > 1 ? reserved.getString(1) : null;
                //没有方法名
                if (StringUtils.isEmpty(callAbi)) {
                    return null;
                }
                jsonArray = new JSONArray();
                jsonArray.add(blockNum);
                jsonArray.add(trxId);
                String data = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_events", jsonArray);
                JSONObject jsonObject = JSONObject.parseObject(data);
                JSONArray jsonArray1 = jsonObject.getJSONArray("result");
                JSONObject resultJson = new JSONObject();
                parseEventData(resultJson, jsonArray1);
                TransactionDTO transactionDTO = new TransactionDTO();
                transactionDTO.setContractId(contractId);
                transactionDTO.setTrxId(origTrxId);
                transactionDTO.setEventParam(resultJson.getString("event_param"));
                transactionDTO.setEventType(resultJson.getString("event_type"));
                transactionDTO.setBlockNum(blockNum);
                transactionDTO.setTrxTime(trxTime);
                transactionDTO.setCallAbi(callAbi);
                transactionDTO.setFromAddr(fromAddr);
                transactionDTO.setAmount(amount);
                transactionDTO.setApiParams(apiParams);
                return transactionDTO;
            }
        } catch (Exception e) {
            log.error("BlockchainServiceImpl", e);
        }
        return null;
    }

    @Override
    public Map<String, JSONArray> saveActBlock(String blocknum) {
        JSONArray jsonArray = new JSONArray();
        Map<String, JSONArray> map = new HashMap<>();
        try {
            jsonArray.add(blocknum);
            String result = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_block", jsonArray);
            String resultSignee = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_block_signee", jsonArray);
            if (StringUtils.isEmpty(result) || StringUtils.isEmpty(resultSignee)) {
                return null;
            }
            JSONObject createTaskJson = JSONObject.parseObject(result);
            createTaskJson = createTaskJson.getJSONObject("result");
            JSONObject resultSigneeJ = JSONObject.parseObject(resultSignee);
            ActBlock actBlock = new ActBlock();
            actBlock.setSignee(resultSigneeJ.getString("result"));
            actBlock.setBlockId(createTaskJson.getString("id"));
            actBlock.setBlockNum(createTaskJson.getLong("block_num"));
            actBlock.setBlockSize(createTaskJson.getLong("block_size"));
            actBlock.setBlockTime(dealTime(createTaskJson.getString("timestamp")));
            actBlock.setNextSecretHash(createTaskJson.getString("next_secret_hash"));
            actBlock.setPrevious(createTaskJson.getString("previous"));
            actBlock.setPrevSecret(createTaskJson.getString("previous_secret"));
            actBlock.setRandomSeed(createTaskJson.getString("random_seed"));
            jsonArray = JSONObject.parseArray(createTaskJson.getString("user_transaction_ids"));
            map.put(actBlock.getBlockId(), jsonArray);
            actBlock.setTransNum(JSONObject.parseArray(createTaskJson.getString("user_transaction_ids")).size());
            actBlock.setTrxDigest(createTaskJson.getString("transaction_digest"));
            actBlock.setTransFee(
                createTaskJson.getLong("signee_shares_issued") + createTaskJson.getLong("signee_fees_collected"));
            actBlock.setTransAmount(0L);
            actBlock.setStatus(TaskDealStatus.TASK_INI.getIntKey());
            if(actBlockMapperService.getByBlockNum(actBlock.getBlockNum()).size() == 0 &&
                actBlockMapperService.getByBlockId(actBlock.getBlockId()).size() == 0){
                actBlockMapperService.insert(actBlock);
            }

        } catch (Exception er) {
            log.error("BlockchainServiceImpl|saveActBlock|[blocknum={}]出现异常", blocknum, er);
            return null;
        }
        return map;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveTransactions(Map<String, JSONArray> map) {
        log.info("BlockchainServiceImpl|saveTransactions 开始处理[map={}]", map);
        List<ActTransaction> actTransactions = new ArrayList<>();

        List<ActBlock> actBlocks = new ArrayList<>();
        map.keySet().forEach(s -> {
            ActBlock actBlock = new ActBlock();
            actBlock.setBlockId(s);
            actBlock.setTransFee(0L);
            actBlock.setTransAmount(0L);
            map.get(s).forEach(j -> {
                ActTransaction actTransaction = getTransactions(s, j.toString());
                if(Objects.isNull(actTransaction)){
                    return;
                }
                actTransactions.add(actTransaction);
                if ("ACT".equals(actTransaction.getCoinType())) {
                    actBlock.setTransAmount(actBlock.getTransAmount() + actTransaction.getAmount());
                }
                actBlock.setTransFee(actBlock.getTransFee() + actTransaction.getFee());

            });
            actBlocks.add(actBlock);
        });
        if (!CollectionUtils.isEmpty(actTransactions)) {
            actTransactionMapperService.insertBatch(actTransactions);
        }

        if (!CollectionUtils.isEmpty(actBlocks)) {
            actBlocks.forEach(actBlock -> {
                List<ActBlock> list =  actBlockMapperService.getByBlockIdAndStatus(actBlock.getBlockId(),TaskDealStatus.TASK_INI.getIntKey());
                actBlock.setStatus(TaskDealStatus.TASK_TRX_CREATE.getIntKey());
                actBlock.setTransAmount(actBlock.getTransAmount() + list.get(0).getTransAmount());
                actBlock.setTransFee(actBlock.getTransFee() + list.get(0).getTransFee());
                actBlock.setId(list.get(0).getId());
                actBlockMapperService.updateById(actBlock);

            });
        }
    }


    @Override
    public long getBalance(String actAddress) {
        try {
            JSONArray tempJson = new JSONArray();
            tempJson.add(actAddress);
            long result1 = 0L;
            String result =
                httpClient.post(config.walletUrl, config.rpcUser, "blockchain_list_address_balances", tempJson);
            JSONObject jsonObject = JSONObject.parseObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            if (jsonArray != null && jsonArray.size() > 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    log.info(jsonArray.getJSONArray(i).toJSONString());
                    log.info(jsonArray.getJSONArray(i).getJSONObject(1).toJSONString());
                    result1 = result1 + jsonArray.getJSONArray(i).getJSONObject(1).getLong("balance");
                }
                return result1;
            }
        } catch (Exception e) {
            log.error("BlockchainServiceImpl|getBalance|[userAddress={}]出现异常", actAddress, e);
        }
        return 0L;
    }


    private ActTransaction getTransactions(String blockId, String trxId) {
        ActTransaction actTransaction = new ActTransaction();
        actTransaction.setBlockId(blockId);
        actTransaction.setTrxId(trxId);
        try {
            JSONArray jsonArray = new JSONArray();
            jsonArray.add(trxId);
            String result = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_transaction", jsonArray);
            JSONObject resultJson = JSONObject.parseObject(result);
            String firstOpType =
                resultJson.getJSONArray("result").getJSONObject(1).getJSONObject("trx").getJSONArray("operations")
                          .getJSONObject(0).getString("type");

            String alpAccount =
                resultJson.getJSONArray("result").getJSONObject(1).getJSONObject("trx").getString("alp_account");
            actTransaction.setSubAddress(alpAccount);
            JSONObject createTaskJson;
            actTransaction.setCoinType("ACT");
            if ("transaction_op_type".equals(firstOpType)) {
                String result_trx_id =
                    resultJson.getJSONArray("result").getJSONObject(1).getJSONObject("trx").getString("result_trx_id");
                jsonArray = new JSONArray();
                jsonArray.add(StringUtils.isEmpty(result_trx_id) ? trxId : result_trx_id);
                log.info("getTransactions|transaction_op_type|[blockId={}][trxId={}][result_trx_id={}]", blockId, trxId, result_trx_id);
                String resultSignee = httpClient.post(config.walletUrl, config.rpcUser,  "blockchain_get_pretty_contract_transaction", jsonArray);
                createTaskJson = JSONObject.parseObject(resultSignee);
                createTaskJson = createTaskJson.getJSONObject("result");
                actTransaction.setExtraTrxId(StringUtils.isEmpty(result_trx_id) ? trxId : result_trx_id);
                actTransaction.setTrxId(createTaskJson.getString("orig_trx_id"));

                JSONObject temp = createTaskJson.getJSONObject("to_contract_ledger_entry");
                actTransaction.setFromAddr(temp.getString("from_account"));
                actTransaction.setFromAcct(temp.getString("from_account_name"));
                actTransaction.setContractId(temp.getString("to_account"));
                actTransaction.setToAcct("");
                actTransaction.setToAddr("");
                actTransaction.setAmount(temp.getJSONObject("amount").getLong("amount"));
                actTransaction.setFee(temp.getJSONObject("fee").getInteger("amount"));
                actTransaction.setTrxTime(dealTime(createTaskJson.getString("timestamp")));
                actTransaction.setMemo(temp.getString("memo"));

                //不是他们的合约id忽略
                if(!config.contractIds.contains(actTransaction.getContractId())){
                    return null;
                }
                if ("false".equals(createTaskJson.getString("is_completed"))) {
                    actTransaction.setIsCompleted((byte) 0);
                }
                JSONArray reserved = createTaskJson.getJSONArray("reserved");
                actTransaction.setCalledAbi(reserved.size() >= 1 ? reserved.getString(0) : null);
                actTransaction.setAbiParams(reserved.size() > 1 ? reserved.getString(1) : null);
                JSONArray jsonArray1 = createTaskJson.getJSONArray("from_contract_ledger_entries");
                int trx_type = createTaskJson.getInteger("trx_type");
                JSONObject jsonObject = getEvent(blockId, trxId, actTransaction);
                actTransaction.setEventType(jsonObject.getString("event_type"));
                actTransaction.setEventParam(jsonObject.getString("event_param"));



                if (trx_type == TrxType.TRX_TYPE_CALL_CONTRACT.getIntKey() &&
                    actTransaction.getCalledAbi().contains(ContractCoinType.COIN_TRANSFER_COIN.getValue())) {
                    log.info("ActBrowserServiceImpl|saveActBlock|[actTransaction={}]", actTransaction);
                    String[] params = actTransaction.getAbiParams().split("\\|");
                    String userAddress = params[0];
                    if (userAddress.length() > 50) {
                        actTransaction.setSubAddress(userAddress);
                        userAddress = userAddress.substring(0, userAddress.length() - 32);
                    }
                    actTransaction.setToAddr(userAddress);
                    if (!StringUtils.isEmpty(actTransaction.getCalledAbi()) &&
                        StringUtils.isNotEmpty(actTransaction.getEventType()) &&
                        actTransaction.getEventType().contains("transfer_to_success")) {
                        boolean flag = true;
                        for (int i = 1; i < params.length; i++) {
                            log.info("getTransactions|gettempp[tempp={}]",params.length >= 2 ? params[i] : "");
                            if (!StringUtils.isEmpty(params[i])) {
                                if (flag) {
                                    String tempp = params.length >= 2 ? params[i] : "0";
                                    try {
                                        Double d = Double.parseDouble(tempp);
                                        actTransaction.setAmount(new BigDecimal(d < 0 ? "0" : d.toString()).multiply(new BigDecimal(100000)).longValue());
                                    } catch (Exception e) {
                                        log.info("getTransactions|gettempp[tempp={}]", params.length >= 2 ? params[i] : "");
                                        actTransaction.setMemo("0");
                                    }
                                    flag = false;
                                }else {
                                    actTransaction.setMemo(params[i]);
                                }
                            }
                        }
                    }
                }

//                String type = dealDataByTrxType(actTransaction.getContractId(), trx_type, actTransaction);
//                if (trx_type == TrxType.TRX_TYPE_CALL_CONTRACT.getIntKey()) {
//                    actTransaction.setCoinType(type);
//                }

            } else {
                String resultSignee = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_pretty_transaction", jsonArray);
                createTaskJson = JSONObject.parseObject(resultSignee);
                createTaskJson = createTaskJson.getJSONObject("result");
                JSONObject temp = (JSONObject) createTaskJson.getJSONArray("ledger_entries").get(0);
                actTransaction.setFromAddr(temp.getString("from_account"));
                actTransaction.setFromAcct(temp.getString("from_account_name"));
                actTransaction.setToAcct(temp.getString("to_account_name"));
                actTransaction.setToAddr(temp.getString("to_account"));
                actTransaction.setAmount(temp.getJSONObject("amount").getLong("amount"));
                actTransaction.setFee(createTaskJson.getJSONObject("fee").getInteger("amount"));
                actTransaction.setTrxTime(dealTime(createTaskJson.getString("timestamp")));
                actTransaction.setMemo(temp.getString("memo"));
                actTransaction.setIsCompleted((byte) 0);
            }


            if(!actTransaction.getFromAddr().contains(config.actAddresses) &&
               !actTransaction.getToAddr().contains(config.actAddresses)){
                return null;
            }
            actTransaction.setBlockNum(createTaskJson.getLong("block_num"));
            actTransaction.setBlockPosition(createTaskJson.getInteger("block_position"));
            actTransaction.setTrxType(createTaskJson.getInteger("trx_type"));
        } catch (Exception er) {
            log.error("BlockchainServiceImpl|saveActBlock|[trx_id={}]出现异常", trxId, er);
        }
        return actTransaction;
    }




    private JSONObject getEvent(String blockId, String trxId, ActTransaction actTransaction) {
        JSONArray jsonArrayEvent = new JSONArray();
        List<ActBlock> list = actBlockMapperService.getBlocks(blockId);
        jsonArrayEvent.add(list.get(0).getBlockNum());
        jsonArrayEvent.add(trxId);
        String resultEvent = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_events", jsonArrayEvent);
        if (StringUtils.isEmpty(resultEvent)) {
            return new JSONObject();
        }
        JSONObject jsonObject =JSONObject.parseObject(resultEvent);
        JSONArray jsonArray = jsonObject.getJSONArray("result");
        log.info("BlockchainServiceImpl|getEvent|[blockId={}][trx_id={}][result={}]", blockId,trxId,jsonArray);
        JSONObject result = new JSONObject();
        if (null != jsonArray && jsonArray.size() > 0) {
            StringBuffer eventType = new StringBuffer();
            StringBuffer eventParam = new StringBuffer();
            jsonArray.stream().forEach(json ->{
                JSONObject jso = (JSONObject) json;
                eventType.append(eventType.length() > 0 ? "|" : "").append(jso.getString("event_type"));
                eventParam.append(eventParam.length() > 0 ? "|" : "").append(jso.getString("event_param"));
            });
            result.put("event_type",eventType);
            result.put("event_param",eventParam);
        }
        return result;
    }


    private void parseEventData(JSONObject result, JSONArray jsonArray1) {
        if (Objects.nonNull(jsonArray1) && jsonArray1.size() > 0) {
            StringBuffer eventType = new StringBuffer();
            StringBuffer eventParam = new StringBuffer();
            jsonArray1.forEach(json -> {
                JSONObject jso = (JSONObject) json;
                eventType.append(eventType.length() > 0 ? "|" : "").append(jso.getString("event_type"));
                eventParam.append(eventParam.length() > 0 ? "|" : "").append(jso.getString("event_param"));
            });
            result.put("event_type", eventType);
            result.put("event_param", eventParam);
        }
    }

    private Date dealTime(String timestamp) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            return format.parse(timestamp);
        } catch (ParseException e) {
            log.error("dealTime|error|", e);
            return null;
        }
    }


}
