package com.zrws.approval.config;

import com.zrws.approval.domain.entity.BpmnDraft;
import com.zrws.approval.mapper.BpmnDraftMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 流程草稿初始化器
 * 应用启动时自动插入默认流程草稿模板数据
 */
@Slf4j
@Component
@Order(15)
@RequiredArgsConstructor
public class BpmnDraftDataInitializer implements ApplicationRunner {

    private final BpmnDraftMapper draftMapper;

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("[初始化] 检查流程草稿模板数据...");

            initDraft("STANDARD", "标准审批流程",
                    "标准四级审批流程：校核→审核→批准→归档",
                    "STANDARD", "系统");

            initDraft("MATERIAL", "物资申领流程",
                    "物资采购审批流程：部门审批→库存核验→领导审批→签收",
                    "MATERIAL", "系统");

            initDraft("DRONE_FLIGHT", "无人机外出报备",
                    "无人机飞行任务审批：空域申请→安全审批→主管批准",
                    "DRONE_FLIGHT", "系统");

            initDraft("EMERGENCY", "应急快速通道",
                    "紧急事件快速响应：应急提交→指挥员批准",
                    "EMERGENCY", "系统");

            log.info("[初始化] 流程草稿模板数据检查完成");

        } catch (Exception e) {
            log.error("[初始化] 流程草稿数据初始化失败: {}", e.getMessage(), e);
        }
    }

    private void initDraft(String processKey, String processName, String description,
                           String category, String creator) {
        Long count = draftMapper.selectCount(
                new LambdaQueryWrapper<BpmnDraft>()
                        .eq(BpmnDraft::getProcessKey, processKey)
                        .eq(BpmnDraft::getIsDeleted, 0)
        );

        if (count > 0) {
            log.debug("[初始化] 流程草稿已存在，跳过: {}", processKey);
            return;
        }

        BpmnDraft draft = new BpmnDraft();
        draft.setProcessKey(processKey);
        draft.setProcessName(processName);
        draft.setDescription(description);
        draft.setXml(generateDefaultBpmnXml(processKey, processName));
        draft.setJsonDef(null);
        draft.setVersion(1);
        draft.setStatus(BpmnDraft.Status.DEPLOYED);
        draft.setCreatedTime(LocalDateTime.now());
        draft.setUpdatedTime(LocalDateTime.now());
        draft.setCreatorId(1L);
        draft.setCreatorName(creator);
        draft.setIsDeleted(0);

        draftMapper.insert(draft);
        log.info("[初始化] ✅ 已创建流程草稿: {} - {}", processKey, processName);
    }

    private String generateDefaultBpmnXml(String processKey, String processName) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" \n" +
                "  xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\"\n" +
                "  id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "  <bpmn:process id=\"" + processKey + "\" name=\"" + processName + "\" isExecutable=\"true\">\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" name=\"开始\"/>\n" +
                "    <bpmn:userTask id=\"UserTask_1\" name=\"审批\"/>\n" +
                "    <bpmn:endEvent id=\"EndEvent_1\" name=\"结束\"/>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"UserTask_1\"/>\n" +
                "    <bpmn:sequenceFlow id=\"Flow_2\" sourceRef=\"UserTask_1\" targetRef=\"EndEvent_1\"/>\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>";
    }
}
