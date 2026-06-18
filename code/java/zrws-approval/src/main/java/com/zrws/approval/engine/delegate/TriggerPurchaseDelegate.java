package com.zrws.approval.engine.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * 物资申领流程 - 库存不足时触发采购 (Service Task)
 * <p>在 MATERIAL.bpmn20.xml 中被引用
 * <p>flowable:class="com.zrws.approval.engine.delegate.TriggerPurchaseDelegate"
 */
public class TriggerPurchaseDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // 获取流程变量
        Object bizTitle = execution.getVariable("bizTitle");
        Object applicant = execution.getVariable("applicant");

        // 实际实现：
        // 1. 调用物资管理微服务，创建采购申请单
        // 2. 通过 MQ 消息通知采购部门
        // 3. 将流程状态更新为"转采购"

        // 演示环境：仅打印日志
        System.out.println("\n=====================================================");
        System.out.println("  [物资申领] 库存不足，触发采购申请");
        System.out.println("  业务标题: " + bizTitle);
        System.out.println("  申请人  : " + applicant);
        System.out.println("  流程实例: " + execution.getProcessInstanceId());
        System.out.println("=====================================================\n");

        // 设置流程变量，供前端区分流程结束原因
        execution.setVariable("purchaseTriggered", true);
    }
}
