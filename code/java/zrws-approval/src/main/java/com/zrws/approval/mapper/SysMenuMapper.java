package com.zrws.approval.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zrws.approval.domain.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统菜单 Mapper
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 获取所有启用的菜单
     */
    List<SysMenu> selectActiveMenus();

    /**
     * 根据菜单组查询
     */
    List<SysMenu> selectByGroup(@Param("menuGroup") String menuGroup);

    /**
     * 根据父菜单ID查询子菜单
     */
    List<SysMenu> selectByParentId(@Param("parentId") Long parentId);
}
