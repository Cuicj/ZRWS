-- 系统菜单表
CREATE TABLE IF NOT EXISTS zrws_sys_menu (
    menu_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '菜单ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(100) NOT NULL COMMENT '菜单名称',
    menu_path VARCHAR(200) DEFAULT '' COMMENT '菜单路径',
    menu_icon VARCHAR(50) DEFAULT '' COMMENT '菜单图标',
    menu_type VARCHAR(20) DEFAULT 'MENU' COMMENT '菜单类型: GROUP/MENU/BUTTON',
    component VARCHAR(200) DEFAULT '' COMMENT '组件路径',
    permission VARCHAR(100) DEFAULT '' COMMENT '权限标识',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0禁用 1启用',
    menu_group VARCHAR(50) DEFAULT '' COMMENT '菜单分组',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除: 0未删除 1已删除',
    created_by BIGINT DEFAULT NULL COMMENT '创建人',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by BIGINT DEFAULT NULL COMMENT '更新人',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_menu_group (menu_group),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- 初始化菜单数据
INSERT INTO zrws_sys_menu (parent_id, menu_name, menu_path, menu_icon, menu_type, menu_group, sort_order, status) VALUES
-- 总览
(0, '运行仪表盘', 'dashboard', '◧', 'MENU', '总览', 1, 1),

-- 采集
(0, '任务列表', 'mission-list', '◫', 'MENU', '采集', 1, 1),
(0, '飞行控制', 'flight-control', '➤', 'MENU', '采集', 2, 1),
(0, 'GPS 航迹', 'gps-track', '◎', 'MENU', '采集', 3, 1),
(0, '土壤采样', 'soil-sample', '◉', 'MENU', '采集', 4, 1),

-- 处理
(0, '数据导入', 'data-import', '↧', 'MENU', '处理', 1, 1),
(0, '质量校验', 'quality-check', '✓', 'MENU', '处理', 2, 1),
(0, '3D 重建', 'reconstruction', '◰', 'MENU', '处理', 3, 1),

-- 土地资源
(0, '土地地图', 'land-map', '◍', 'MENU', '土地资源', 1, 1),
(0, '公告栏', 'announcement-board', '📰', 'MENU', '土地资源', 2, 1),
(0, '土质分类', 'soil-classify', '✦', 'MENU', '土地资源', 3, 1),
(0, '岩层分析', 'rock-stratum', '🪨', 'MENU', '土地资源', 4, 1),
(0, '灾害风险', 'disaster-risk', '◬', 'MENU', '土地资源', 5, 1),
(0, '面积计算', 'area-calc', '◭', 'MENU', '土地资源', 6, 1),

-- GIS
(0, '三维地球', 'cad-viewer', '🌍', 'MENU', 'GIS', 1, 1),
(0, '图纸对比', 'cad-compare', '◫', 'MENU', 'GIS', 2, 1),

-- 审批
(0, '审批列表', 'approval-list', '◐', 'MENU', '审批', 1, 1),
(0, '流程设计', 'workflow-design', '◭', 'MENU', '审批', 2, 1),

-- 系统
(0, '设备管理', 'device', '⊞', 'MENU', '系统', 1, 1),
(0, '用户管理', 'user-manage', '◉', 'MENU', '系统', 2, 1),
(0, '角色管理', 'role-manage', '◆', 'MENU', '系统', 3, 1),
(0, '系统配置', 'sys-config', '⚙', 'MENU', '系统', 4, 1),
(0, '公告管理', 'announcement', '✉', 'MENU', '系统', 5, 1);
