package com.achain.service.impl;

import com.achain.domain.entity.ActTransaction;
import com.achain.mapper.ActTransactionMapper;
import com.achain.service.IActTransactionMapperService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.toolkit.CollectionUtils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yujianjian
 * @since 2017-12-15 下午4:45
 */
@Service
public class ActTransactionMapperServiceImpl extends ServiceImpl<ActTransactionMapper, ActTransaction>
    implements IActTransactionMapperService {

    @Override
    public Map<String, Object> walletAccountTransactionHistory(Long start, String address, String contractId) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", null);
        if (start == null || StringUtils.isEmpty(address)) {
            map.put("msg", "地址不能为空");
            return map;
        }
        Wrapper<ActTransaction> wrapper = new EntityWrapper<>();

        if(StringUtils.isEmpty(contractId)){
            wrapper.where("to_addr = '" + address+"'")
                   .and("block_num >= " + start)
                   .orNew("from_addr = '" + address+"'")
                   .and("block_num >= " + start)
                   .orderBy("block_num ASC");
        }else {
            wrapper.where("to_addr = '" + address+"'")
                   .and("block_num >= " + start)
                   .and("contract_id= '"+contractId+"'")
                   .orNew("from_addr = '" + address+"'")
                   .and("contract_id= '"+contractId+"'")
                   .and("block_num >= " + start)
                   .orderBy("block_num ASC");
        }

        List<ActTransaction> actTransactionList = baseMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(actTransactionList)) {
            map.put("msg", "没有更多的交易记录");
            return map;
        }
        map.put("data", actTransactionList);
        return map;
    }
}
