package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.SysMenu;
import com.zrws.approval.service.SysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<Map<String, Object>> getMenuTree() {
        return success(Map.of(
                "success", true,
                "data", menuService.getMenuTree()
        ));
    }

    /**
     * 获取所有菜单列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getMenuList() {
        return success(Map.of(
                "success", true,
                "list", menuService.getAllMenus()
        ));
    }

    /**
     * 获取单个菜单
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMenu(@PathVariable Long id) {
        SysMenu menu = menuService.getMenuById(id);
        if (menu != null) {
            return success(Map.of("success", true, "menu", menu));
        } else {
            return error("菜单不存在");
        }
    }

    /**
     * 保存菜单
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveMenu(@RequestBody SysMenu menu) {
        try {
            menuService.saveMenu(menu);
            return success(Map.of("success", true, "menu", menu));
        } catch (Exception e) {
            log.error("保存菜单失败", e);
            return error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMenu(@PathVariable Long id) {
        boolean result = menuService.deleteMenu(id);
        if (result) {
            return success(Map.of("success", true, "message", "删除成功"));
        } else {
            return error("删除失败");
        }
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<Map<String, Object>> success(Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        if (data instanceof Map) {
            result.putAll((Map<String, Object>) data);
        }
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> error(String message) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", message
        ));
    }
}
