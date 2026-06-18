package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.ApprovalComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/** 审批意见 Mapper */
@Mapper
public interface ApprovalCommentMapper extends BaseMapper<ApprovalComment> {

    @Select("SELECT * FROM zrws_approval_comment WHERE task_id = #{taskId} ORDER BY created_time ASC")
    List<ApprovalComment> selectByTaskId(@Param("taskId") Long taskId);
}
