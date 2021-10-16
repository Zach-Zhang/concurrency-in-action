package com.zach.cocurrency.core.ch4.case02;

import java.util.Map;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/10/16 15:26
 * Version :1.0
 */
public interface StatProcessor {
    void process(String record);

    Map<Long, DelayItem> getResult();
}
