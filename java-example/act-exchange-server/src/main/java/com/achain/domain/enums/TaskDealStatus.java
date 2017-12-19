package com.achain.domain.enums;


/**
 * Created by qiangkz on 2017/6/14.
 */
public enum TaskDealStatus {


    TASK_INI(0, "区块初保存"),

    TASK_TRX_CREATE(1, "区块下的交易记录入库完毕"),

    TASK_CALCULATE_USER_INFO(2, "用户余额计算完毕"),

    TASK_SUBMIT_SUCCESS(3, "任务提交成功"),

    TASK_SUBMIT_FAIL(4, "任务提失败"),

    TASK_SUCCESS(5, "任务完成成功"),;

    private final int key;

    private final String desc;

    TaskDealStatus(int key) {
        this.key = key;
        this.desc = "";
    }

    TaskDealStatus(int key, String desc) {
        this.key = key;
        this.desc = desc;
    }


    public int getIntKey() {
        return key;
    }


    public String getDesc() {
        return desc;
    }
}
