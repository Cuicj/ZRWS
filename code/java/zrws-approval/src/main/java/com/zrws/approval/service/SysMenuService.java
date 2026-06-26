package com.zrws.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.SysMenu;
import com.zrws.approval.mapper.SysMenuMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统菜单服务
 */
@Slf4j
@Service
public class SysMenuService {

    @Autowired
    private SysMenuMapper menuMapper;

    /**
     * 获取所有菜单（树形结构，按分组）
     */
    public List<Map<String, Object>> getMenuTree() {
        List<SysMenu> allMenus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .eq(SysMenu::getStatus, 1)
                        .orderByAsc(SysMenu::getSortOrder)
        );

        Map<String, List<SysMenu>> groupMap = allMenus.stream()
                .collect(Collectors.groupingBy(menu -> 
                    menu.getMenuGroup() != null ? menu.getMenuGroup() : "其他"
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<SysMenu>> entry : groupMap.entrySet()) {
            Map<String, Object> group = new HashMap<>();
            group.put("title", entry.getKey());
            List<Map<String, Object>> items = new ArrayList<>();
            for (SysMenu menu : entry.getValue()) {
                if (menu.getParentId() == null || menu.getParentId() == 0) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("path", menu.getMenuPath());
                    item.put("name", menu.getMenuName());
                    item.put("icon", menu.getMenuIcon());
                    items.add(item);
                }
            }
            group.put("items", items);
            result.add(group);
        }

        result.sort((a, b) -> {
            String titleA = (String) a.get("title");
            String titleB = (String) b.get("title");
            return getGroupSort(titleA) - getGroupSort(titleB);
        });

        return result;
    }

    private int getGroupSort(String title) {
        return switch (title) {
            case "总览" -> 1;
            case "采集" -> 2;
            case "处理" -> 3;
            case "土地资源" -> 4;
            case "GIS" -> 5;
            case "审批" -> 6;
            case "系统" -> 7;
            default -> 99;
        };
    }

    /**
     * 获取所有菜单列表
     */
    public List<SysMenu> getAllMenus() {
        return menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .orderByAsc(SysMenu::getSortOrder)
        );
    }

    /**
     * 保存菜单
     */
    public void saveMenu(SysMenu menu) {
        if (menu.getMenuId() != null) {
            menuMapper.updateById(menu);
        } else {
            menuMapper.insert(menu);
        }
    }

    /**
     * 删除菜单
     */
    public boolean deleteMenu(Long menuId) {
        return menuMapper.deleteById(menuId) > 0;
    }

    /**
     * 根据ID获取菜单
     */
    public SysMenu getMenuById(Long menuId) {
        return menuMapper.selectById(menuId);
    }
}
