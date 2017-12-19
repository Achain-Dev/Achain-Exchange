package com.achain.service.impl;

import com.achain.domain.entity.ActBlock;
import com.achain.mapper.ActBlockMapper;
import com.achain.service.IActBlockMapperService;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author yujianjian
 * @since 2017-12-15 下午4:45
 */
@Service
public class ActBlockMapperServiceImpl extends ServiceImpl<ActBlockMapper, ActBlock>
    implements IActBlockMapperService {

    @Override
    public List<ActBlock> getBlocks(String blockId) {
        EntityWrapper<ActBlock> wrapper = new EntityWrapper<>();
        wrapper.where("block_id={0}", blockId);
        RowBounds rowBounds = new RowBounds(0, 1);
        return baseMapper.selectPage(rowBounds, wrapper);
    }

    @Override
    public List<ActBlock> getByBlockIdAndStatus(String blockId, Integer status) {
        EntityWrapper<ActBlock> wrapper = new EntityWrapper<>();
        wrapper.where("block_id={0}", blockId).where("status={0}",status);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<ActBlock> getByBlockNum(Long blockNum) {
        EntityWrapper<ActBlock> wrapper = new EntityWrapper<>();
        wrapper.where("block_num={0}", blockNum);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<ActBlock> getByBlockId(String blockId) {
        EntityWrapper<ActBlock> wrapper = new EntityWrapper<>();
        wrapper.where("block_id={0}", blockId);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public ActBlock getMaxBlock() {
        EntityWrapper<ActBlock> wrapper = new EntityWrapper<>();
        wrapper.orderBy("block_num desc");
        RowBounds rowBounds = new RowBounds(0, 1);
        List<ActBlock> list = baseMapper.selectPage(rowBounds, wrapper);
        if(list.size() == 0){
            return null;
        }
        return list.get(0);
    }
}
