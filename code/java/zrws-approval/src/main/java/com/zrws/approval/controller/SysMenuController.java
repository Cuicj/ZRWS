package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.SysMenu;
import com.zrws.approval.service.SysMenuService;
import com.zrws.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统菜单 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/menu")
@CrossOrigin(origins = "*")
public class SysMenuController {

    @Autowired
    private SysMenuService menuService;

    /**
     * 获取菜单树（按分组，用于侧边栏展示）
     */
    @GetMapping("/tree")
    public R<List<SysMenu>> getMenuTree() {
        return R.ok(menuService.getMenuTree());
    }

    /**
     * 获取所有菜单列表
     */
    @GetMapping("/list")
    public R<List<SysMenu>> getMenuList() {
        return R.ok(menuService.getAllMenus());
    }

    /**
     * 获取单个菜单
     */
    @GetMapping("/{id}")
    public R<SysMenu> getMenu(@PathVariable Long id) {
        SysMenu menu = menuService.getMenuById(id);
        if (menu != null) {
            return R.ok(menu);
        } else {
            return R.fail("菜单不存在");
        }
    }

    /**
     * 保存菜单
     */
    @PostMapping
    public R<SysMenu> saveMenu(@RequestBody SysMenu menu) {
        try {
            menuService.saveMenu(menu);
            return R.ok(menu);
        } catch (Exception e) {
            log.error("保存菜单失败", e);
            return R.fail("保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteMenu(@PathVariable Long id) {
        boolean result = menuService.deleteMenu(id);
        if (result) {
            return R.ok();
        } else {
            return R.fail("删除失败");
        }
    }
}
