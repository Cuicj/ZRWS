package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.BpmnDraft;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程草稿Mapper
 */
@Mapper
public interface BpmnDraftMapper extends BaseMapper<BpmnDraft> {

    /**
     * 根据流程Key查询草稿
     */
    @Select("SELECT * FROM zrws_bpmn_draft WHERE process_key = #{processKey} AND is_deleted = 0 LIMIT 1")
    BpmnDraft selectByProcessKey(@Param("processKey") String processKey);

    /**
     * 查询所有草稿（按更新时间倒序）
     */
    @Select("SELECT * FROM zrws_bpmn_draft WHERE is_deleted = 0 ORDER BY updated_time DESC")
    List<BpmnDraft> selectAllDrafts();

    /**
     * 根据状态查询草稿
     */
    @Select("SELECT * FROM zrws_bpmn_draft WHERE status = #{status} AND is_deleted = 0 ORDER BY updated_time DESC")
    List<BpmnDraft> selectByStatus(@Param("status") String status);

    /**
     * 获取最新版本号
     */
    @Select("SELECT MAX(version) FROM zrws_bpmn_draft WHERE process_key = #{processKey} AND is_deleted = 0")
    Integer selectMaxVersion(@Param("processKey") String processKey);
}
