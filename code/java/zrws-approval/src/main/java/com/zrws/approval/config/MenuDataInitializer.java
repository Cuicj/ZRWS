package com.zrws.approval.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.SysMenu;
import com.zrws.approval.mapper.SysMenuMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单数据初始化
 * 在 SchemaSync 之后执行（Order=2，SchemaSyncRunner Order=1）
 */
@Slf4j
@Component
@Order(2)
public class MenuDataInitializer implements ApplicationRunner {

    @Autowired
    private SysMenuMapper menuMapper;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<SysMenu> existingMenus = menuMapper.selectList(null);
            Map<String, SysMenu> existingPathMap = new HashMap<>();
            for (SysMenu menu : existingMenus) {
                existingPathMap.put(menu.getMenuPath(), menu);
            }

            List<SysMenu> menus = Arrays.asList(
                    createMenu(0L, "运行仪表盘", "dashboard", "◧", "总览", 1),
                    createMenu(0L, "任务列表", "mission-list", "◫", "采集", 1),
                    createMenu(0L, "飞行控制", "flight-control", "➤", "采集", 2),
                    createMenu(0L, "GPS 航迹", "gps-track", "◎", "采集", 3),
                    createMenu(0L, "土壤采样", "soil-sample", "◉", "采集", 4),
                    createMenu(0L, "数据导入", "data-import", "↧", "处理", 1),
                    createMenu(0L, "数据导出", "data-export", "↥", "处理", 2),
                    createMenu(0L, "质量校验", "quality-check", "✓", "处理", 3),
                    createMenu(0L, "3D 重建", "reconstruction", "◰", "处理", 4),
                    createMenu(0L, "土地地图", "land-map", "◍", "土地资源", 1),
                    createMenu(0L, "公告栏", "announcement-board", "📰", "土地资源", 2),
                    createMenu(0L, "土质分类", "soil-classify", "✦", "土地资源", 3),
                    createMenu(0L, "岩层分析", "rock-stratum", "🪨", "土地资源", 4),
                    createMenu(0L, "灾害风险", "disaster-risk", "◬", "土地资源", 5),
                    createMenu(0L, "面积计算", "area-calc", "◭", "土地资源", 6),
                    createMenu(0L, "气候变暖监测", "climate-warming", "🌡️", "生态环境", 1),
                    createMenu(0L, "沙漠化监测", "desertification", "🏜️", "生态环境", 2),
                    createMenu(0L, "水土流失监测", "soil-erosion", "🌊", "生态环境", 3),
                    createMenu(0L, "生态标准库", "eco-standard", "📋", "生态环境", 4),
                    createMenu(0L, "三维地球", "cad-viewer", "🌍", "GIS", 1),
                    createMenu(0L, "图纸对比", "cad-compare", "◫", "GIS", 2),
                    createMenu(0L, "审批列表", "approval-list", "◐", "审批", 1),
                    createMenu(0L, "流程设计", "workflow-design", "◭", "审批", 2),
                    createMenu(0L, "设备管理", "device", "⊞", "系统", 1),
                    createMenu(0L, "用户管理", "user-manage", "◉", "系统", 2),
                    createMenu(0L, "角色管理", "role-manage", "◆", "系统", 3),
                    createMenu(0L, "组织管理", "org-manage", "◎", "系统", 4),
                    createMenu(0L, "系统配置", "sys-config", "⚙", "系统", 5),
                    createMenu(0L, "公告管理", "announcement", "✉", "系统", 6),
                    createMenu(0L, "报表中心", "report-center", "📊", "系统", 7),
                    createMenu(0L, "对外接口", "open-api", "🔌", "系统", 8)
            );

            int addedCount = 0;
            for (SysMenu menu : menus) {
                if (!existingPathMap.containsKey(menu.getMenuPath())) {
                    menuMapper.insert(menu);
                    addedCount++;
                    log.info("[菜单] 新增菜单项: {}", menu.getMenuName());
                }
            }

            if (addedCount > 0) {
                log.info("[菜单] 菜单数据补充完成，新增 {} 条", addedCount);
            } else {
                log.info("[菜单] 菜单数据已完整（{}条），无需补充", existingMenus.size());
            }
        } catch (Exception e) {
            log.error("[菜单] 菜单数据初始化失败: {}", e.getMessage(), e);
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
