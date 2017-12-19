package com.achain.service;

import com.achain.domain.entity.ActBlock;
import com.baomidou.mybatisplus.service.IService;

import java.util.List;

/**
 * @author yujianjian
 * @since 2017-12-15 下午4:44
 */
public interface IActBlockMapperService extends IService<ActBlock> {


    List<ActBlock> getBlocks(String blockId);

    List<ActBlock> getByBlockIdAndStatus(String blockId,Integer status);


    List<ActBlock> getByBlockNum(Long blockNum);

    List<ActBlock> getByBlockId(String blockId);

    ActBlock getMaxBlock();
}
