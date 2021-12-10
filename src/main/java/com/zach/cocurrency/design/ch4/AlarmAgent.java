package com.zach.cocurrency.design.ch4;

import java.util.Timer;

/**
 * @author Zhangshengzhi
 * @version 1.0.0
 * @Description 负责连接告警服务器, 并发送告警信息至告警服务器
 * @createTime 2021年12月10日 13:04:00
 */
public class AlarmAgent {
    /**
     * 用于记录AlarmAgent是否连接上告警服务器
     */
    private volatile boolean connectedToServer = false;

    /**
     * 模式角色: GuradedSuspension.Predicate
     */
    private final Predicate agentConnected = () -> connectedToServer;
    /**
     * 模式角色: GuardedSuspension.Blocker
     */
    private final Blocker blocker = new ConditionVarBlocker();

    /**
     * 心跳定时器
     */
    private final Timer heartbeateTimer = new Timer(true);

    public void sendAlarm(final AlarmInfo alarmInfo) throws Exception {
        //模式角色:GuardedSuspension.GuardedAction
        GuardedAction<Void> guardedAction = new GuardedAction<Void>(agentConnected) {
            // 模式角色：GuardedSuspension.GuardedAction
            @Override
            public Void call() throws Exception {
                doSendAlarm(alarmInfo);
                return null;
            }
        };
        blocker.callWithGuard(guardedAction);
    }

    // 通过网络连接将告警信息发送给告警服务器
    private void doSendAlarm(AlarmInfo alarmInfo) {
        try {
            // 模拟发送告警至服务器的耗时
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void init() {
        //告警连接线程

    }
}
