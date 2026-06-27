package com.zrws.approval.controller;

import com.zrws.approval.domain.entity.SysMenu;
import com.zrws.approval.service.SysMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", menuService.getMenuTree());
        return success(result);
    }

    /**
     * 获取所有菜单列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getMenuList() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("list", menuService.getAllMenus());
        return success(result);
    }

    /**
     * 获取单个菜单
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMenu(@PathVariable Long id) {
        SysMenu menu = menuService.getMenuById(id);
        if (menu != null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("menu", menu);
            return success(result);
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
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("menu", menu);
            return success(result);
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
            Map<String, Object> map = new HashMap<>();
            map.put("success", true);
            map.put("message", "删除成功");
            return success(map);
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
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("error", message);
        return ResponseEntity.badRequest().body(result);
    }
}
