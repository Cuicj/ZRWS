package com.zrws.approval.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.SysMenu;
import com.zrws.approval.mapper.SysMenuMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 菜单数据初始化
 */
@Slf4j
@Component
public class MenuDataInitializer {

    @Autowired
    private SysMenuMapper menuMapper;

    @PostConstruct
    public void init() {
        try {
            Long count = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>());
            if (count != null && count > 0) {
                log.info("菜单数据已存在，跳过初始化");
                return;
            }

            log.info("开始初始化菜单数据...");

            List<SysMenu> menus = Arrays.asList(
                    createMenu(0L, "运行仪表盘", "dashboard", "◧", "总览", 1),
                    createMenu(0L, "任务列表", "mission-list", "◫", "采集", 1),
                    createMenu(0L, "飞行控制", "flight-control", "➤", "采集", 2),
                    createMenu(0L, "GPS 航迹", "gps-track", "◎", "采集", 3),
                    createMenu(0L, "土壤采样", "soil-sample", "◉", "采集", 4),
                    createMenu(0L, "数据导入", "data-import", "↧", "处理", 1),
                    createMenu(0L, "质量校验", "quality-check", "✓", "处理", 2),
                    createMenu(0L, "3D 重建", "reconstruction", "◰", "处理", 3),
                    createMenu(0L, "土地地图", "land-map", "◍", "土地资源", 1),
                    createMenu(0L, "公告栏", "announcement-board", "📰", "土地资源", 2),
                    createMenu(0L, "土质分类", "soil-classify", "✦", "土地资源", 3),
                    createMenu(0L, "岩层分析", "rock-stratum", "🪨", "土地资源", 4),
                    createMenu(0L, "灾害风险", "disaster-risk", "◬", "土地资源", 5),
                    createMenu(0L, "面积计算", "area-calc", "◭", "土地资源", 6),
                    createMenu(0L, "三维地球", "cad-viewer", "🌍", "GIS", 1),
                    createMenu(0L, "图纸对比", "cad-compare", "◫", "GIS", 2),
                    createMenu(0L, "审批列表", "approval-list", "◐", "审批", 1),
                    createMenu(0L, "流程设计", "workflow-design", "◭", "审批", 2),
                    createMenu(0L, "设备管理", "device", "⊞", "系统", 1),
                    createMenu(0L, "用户管理", "user-manage", "◉", "系统", 2),
                    createMenu(0L, "角色管理", "role-manage", "◆", "系统", 3),
                    createMenu(0L, "系统配置", "sys-config", "⚙", "系统", 4),
                    createMenu(0L, "公告管理", "announcement", "✉", "系统", 5)
            );

            for (SysMenu menu : menus) {
                menuMapper.insert(menu);
            }

            log.info("菜单数据初始化完成，共 {} 条", menus.size());
        } catch (Exception e) {
            log.warn("菜单数据初始化失败（可能表不存在）: {}", e.getMessage());
        }
    }

    private SysMenu createMenu(Long parentId, String name, String path, String icon, String group, int sort) {
        SysMenu menu = new SysMenu();
        menu.setParentId(parentId);
        menu.setMenuName(name);
        menu.setMenuPath(path);
        menu.setMenuIcon(icon);
        menu.setMenuType("MENU");
        menu.setMenuGroup(group);
        menu.setSortOrder(sort);
        menu.setStatus(1);
        menu.setIsDeleted(0);
        return menu;
    }
}
