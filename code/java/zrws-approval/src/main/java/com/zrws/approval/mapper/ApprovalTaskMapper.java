package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.ApprovalTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/** 审批任务 Mapper */
@Mapper
public interface ApprovalTaskMapper extends BaseMapper<ApprovalTask> {

    /** 我的待办：我是当前审批人 */
    @Select("SELECT t.* FROM zrws_approval_task t WHERE t.status = 'PROCESSING' AND t.task_id IN " +
            "(SELECT DISTINCT exec.task_id_ FROM act_ru_execution exec " +
            " INNER JOIN act_ru_task tk ON tk.execution_id_ = exec.id_ " +
            " WHERE tk.assignee_ = #{assignee}) " +
            " ORDER BY t.priority DESC, t.created_time DESC ")
    List<ApprovalTask> selectTodoList(@Param("assignee") String assignee);

    /** 我发起的审批 */
    @Select("SELECT * FROM zrws_approval_task WHERE applicant_id = #{applicantId} ORDER BY created_time DESC")
    List<ApprovalTask> selectMyApplied(@Param("applicantId") Long applicantId);
}
