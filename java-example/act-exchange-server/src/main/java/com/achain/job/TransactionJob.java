package com.achain.job;

import com.achain.conf.Config;
import com.achain.domain.entity.ActBlock;
import com.achain.domain.enums.TaskDealStatus;
import com.achain.service.IActBlockMapperService;
import com.achain.service.IBlockchainService;
import com.achain.utils.SDKHttpClient;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yujianjian
 * @since 2017-12-15 下午4:18
 */
@Component
@Slf4j
public class TransactionJob {


    @Autowired
    private Config config;
    @Autowired
    private IBlockchainService blockchainService;
    @Autowired
    private IActBlockMapperService actBlockMapperService;
    @Autowired
    private SDKHttpClient httpClient;

    @Scheduled(fixedDelay = 10 * 1000)
    public void doTransactionJob() {
        ActBlock previousBlock = getBlockNum();
        Long preBlockNum = previousBlock.getBlockNum();
        log.info("doTransactionJob|开始|HeaderBlockNum={}",preBlockNum);
        long headerBlockCount = blockchainService.getBlockCount();
        if (headerBlockCount <= preBlockNum) {
            log.info("doTransactionJob|最大块号为[{}],不需要进行扫块", headerBlockCount);
            return;
        }
        if(previousBlock.getStatus() == 0){
            preBlockNum -= 1;
        }
        for (long blockCount = preBlockNum + 1; blockCount <= headerBlockCount; ++blockCount) {
            Map<String, JSONArray> map = blockchainService.saveActBlock(Long.toString(blockCount));
            if (!CollectionUtils.isEmpty(map)) {
                try {
                    blockchainService.saveTransactions(map);
                } catch (Exception e) {
                    log.error("doTransactionJob|本次任务执行出现异常", e);
                    continue;
                }
            } else {
                break;
            }

        }
        log.info("doTransactionJob|结束|nowHeaderBlockNum={}", headerBlockCount);
    }


    private ActBlock getBlockNum(){
        ActBlock actBlock = actBlockMapperService.getMaxBlock();
        if (Objects.nonNull(actBlock)) {
            return actBlock;
        } else {
            String result = httpClient.post(config.walletUrl, config.rpcUser, "blockchain_get_block_count", new JSONArray());
            JSONObject createTaskJson = JSONObject.parseObject(result);
            long headerBlockCount = createTaskJson.getLong("result");
            ActBlock act = new ActBlock();
            act.setBlockNum(headerBlockCount);
            act.setStatus(TaskDealStatus.TASK_TRX_CREATE.getIntKey());
            return act;
        }
    }

}
