-- ============================================================
-- 智壤卫士 - 全局设置与公告栏表
-- 版本: v2.3 全局设置 + 公告栏 + 新闻管理
-- ============================================================

USE zrws_approval;

-- -----------------------------------------------------------
-- 7. 全局配置表 (sys_config)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_config;
CREATE TABLE sys_config (
    config_id       BIGINT          NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    config_key     VARCHAR(100)    NOT NULL COMMENT '配置键',
    config_value   VARCHAR(500)    COMMENT '配置值',
    config_type    VARCHAR(20)     DEFAULT 'STRING' COMMENT '类型: STRING/INT/BOOLEAN/JSON',
    config_name    VARCHAR(100)    COMMENT '配置名称',
    config_group   VARCHAR(50)     COMMENT '配置分组',
    description    VARCHAR(500)    COMMENT '描述',
    sort_order     INT             DEFAULT 0 COMMENT '排序',
    status         TINYINT(1)      DEFAULT 1 COMMENT '状态',
    is_system      TINYINT(1)      DEFAULT 0 COMMENT '系统内置',
    is_deleted     TINYINT(1)      DEFAULT 0 COMMENT '逻辑删除',
    created_by     BIGINT,
    created_time   DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by     BIGINT,
    updated_time  DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (config_id),
    UNIQUE KEY uk_config_key (config_key),
    KEY idx_group (config_group),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局配置表';

-- -----------------------------------------------------------
-- 7.1 公告分类表 (announcement_category)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS announcement_category;
CREATE TABLE announcement_category (
    category_id     BIGINT          NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    category_code   VARCHAR(50)     NOT NULL COMMENT '分类编码',
    category_name   VARCHAR(100)    NOT NULL COMMENT '分类名称',
    icon            VARCHAR(100)    COMMENT '图标',
    color           VARCHAR(20)     COMMENT '颜色',
    description     VARCHAR(500)    COMMENT '描述',
    sort_order      INT             DEFAULT 0 COMMENT '排序',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态: 0-禁用 1-启用',
    is_deleted      TINYINT(1)      DEFAULT 0 COMMENT '逻辑删除',
    created_by      BIGINT,
    created_time    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_time   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (category_id),
    UNIQUE KEY uk_category_code (category_code),
    KEY idx_status (status),
    KEY idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告分类表';

-- -----------------------------------------------------------
-- 7.2 公告表 (announcement)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS announcement;
CREATE TABLE announcement (
    announcement_id BIGINT          NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    title           VARCHAR(200)    NOT NULL COMMENT '标题',
    content         TEXT            NOT NULL COMMENT '内容',
    summary        VARCHAR(500)    COMMENT '摘要',
    category_id     BIGINT          COMMENT '分类ID',
    category_name   VARCHAR(100)    COMMENT '分类名称',
    cover_image     VARCHAR(500)    COMMENT '封面图片',
    author          VARCHAR(100)    COMMENT '作者',
    source          VARCHAR(100)    COMMENT '来源',
    source_url      VARCHAR(500)    COMMENT '来源URL',
    is_top          TINYINT(1)      DEFAULT 0 COMMENT '是否置顶',
    is_recommend    TINYINT(1)      DEFAULT 0 COMMENT '是否推荐',
    is_hot          TINYINT(1)      DEFAULT 0 COMMENT '是否热门',
    view_count      INT             DEFAULT 0 COMMENT '浏览次数',
    like_count      INT             DEFAULT 0 COMMENT '点赞次数',
    comment_count   INT             DEFAULT 0 COMMENT '评论次数',
    publish_time    DATETIME        COMMENT '发布时间',
    offline_time    DATETIME        COMMENT '下线时间',
    status          VARCHAR(20)     DEFAULT 'DRAFT' COMMENT '状态: DRAFT/PENDING/APPROVED/REJECTED/PUBLISHED/OFFLINE',
    audit_status    VARCHAR(20)     DEFAULT 'PENDING' COMMENT '审核状态',
    rejection_reason VARCHAR(500)   COMMENT '驳回原因',
    keywords        VARCHAR(500)    COMMENT '关键词(逗号分隔)',
    tags            VARCHAR(500)    COMMENT '标签(逗号分隔)',
    priority        INT             DEFAULT 0 COMMENT '优先级',
    is_deleted      TINYINT(1)      DEFAULT 0 COMMENT '逻辑删除',
    created_by      BIGINT,
    created_time   DATETIME        DEFAULT CURRENT_TIMESTAMP,
    updated_by      BIGINT,
    updated_time   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (announcement_id),
    KEY idx_category (category_id),
    KEY idx_status (status),
    KEY idx_audit_status (audit_status),
    KEY idx_publish_time (publish_time),
    KEY idx_is_top (is_top),
    KEY idx_keywords (keywords)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- -----------------------------------------------------------
-- 7.3 公告审核记录表 (announcement_audit)
-- -----------------------------------------------------------
DROP TABLE IF EXISTS announcement_audit;
CREATE TABLE announcement_audit (
    audit_id        BIGINT          NOT NULL AUTO_INCREMENT COMMENT '审核ID',
    announcement_id BIGINT          NOT NULL COMMENT '公告ID',
    audit_type      VARCHAR(20)     NOT NULL COMMENT '审核类型: SUBMIT/APPROVE/REJECT/PUBLISH/OFFLINE',
    audit_result    VARCHAR(20)     COMMENT '审核结果',
    comment         VARCHAR(500)    COMMENT '审核意见',
    reject_reason   VARCHAR(500)    COMMENT '驳回原因',
    auditor_id      BIGINT          COMMENT '审核人ID',
    auditor_name    VARCHAR(100)    COMMENT '审核人姓名',
    audit_time      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
    is_deleted      TINYINT(1)      DEFAULT 0 COMMENT '逻辑删除',
    created_time    DATETIME        DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (audit_id),
    KEY idx_announcement (announcement_id),
    KEY idx_auditor (auditor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告审核记录表';

-- ============================================================
-- 初始化配置数据
-- ============================================================

INSERT INTO sys_config (config_key, config_value, config_type, config_name, config_group, description, sort_order, status, is_system) VALUES
('FONT_SIZE', '14', 'INT', '字体大小', 'GLOBAL', '系统全局字体大小(px)', 1, 1, 1),
('THEME_COLOR', '#3498db', 'STRING', '主题色', 'GLOBAL', '系统主题颜色', 2, 1, 1),
('DISABLE_WORDS', '', 'STRING', '禁用词', 'GLOBAL', '内容中禁用的敏感词列表,用逗号分隔', 3, 1, 1),
('ANNOUNCEMENT_ENABLED', 'true', 'BOOLEAN', '启用公告栏', 'ANNOUNCEMENT', '是否启用公告栏功能', 1, 1, 1),
('ANNOUNCEMENT_PUBLISH_AUTO', 'false', 'BOOLEAN', '发布自动审核', 'ANNOUNCEMENT', '发布是否需要审核', 2, 1, 1),
('NEWS_AUTO_FETCH', 'true', 'BOOLEAN', '自动获取新闻', 'NEWS', '是否自动获取今日新闻', 1, 1, 1),
('NEWS_FETCH_URL', 'https://api.vvhan.com/api/hotlist/jstoutiao', 'STRING', '新闻接口', 'NEWS', '新闻数据获取接口地址', 2, 1, 1);

-- ============================================================
-- 初始化公告分类
-- ============================================================

INSERT INTO announcement_category (category_code, category_name, icon, color, description, sort_order, status) VALUES
('NEWS', '今日新闻', '📰', '#e74c3c', '每日最新新闻资讯', 1, 1),
('NOTICE', '系统公告', '📢', '#3498db', '系统重要通知公告', 2, 1),
('ACTIVITY', '活动资讯', '🎉', '#9b59b6', '最新活动信息', 3, 1),
('TECH', '技术分享', '💡', '#27ae60', '技术文章和教程', 4, 1),
('POLICY', '政策解读', '📋', '#f39c12', '相关政策文件解读', 5, 1);

-- ============================================================
-- 初始化示例公告数据
-- ============================================================

INSERT INTO announcement (title, content, summary, category_id, category_name, author, source, status, audit_status, is_top, is_recommend, publish_time, view_count, keywords) VALUES
('土壤修复技术研究取得新突破', '<p>近日,我国科研团队在土壤修复技术领域取得重大突破,研发出新型生物修复技术,可以有效治理重金属污染土壤。</p><p>该技术利用特殊微生物群落,在3-6个月内可将土壤重金属含量降低80%以上,同时改善土壤结构,恢复土壤肥力。</p><p>专家表示,这项技术的推广应用将对我国农业可持续发展产生深远影响。</p>', '我国科研团队研发出新型生物修复技术,可有效治理重金属污染土壤', 4, '技术分享', '科技日报', '科技日报', 'PUBLISHED', 'APPROVED', 1, 1, NOW(), 1256, '土壤修复,生物技术,环保'),

('全国春季农业生产工作电视电话会议召开', '<p>国务院召开全国春季农业生产工作电视电话会议,强调要切实抓好春季农业生产,为全年粮食丰收奠定坚实基础。</p><p>会议指出,要加快高标准农田建设,推进农业科技创新,提高农业机械化水平,确保粮食产量保持在1.3万亿斤以上。</p>', '国务院强调切实抓好春季农业生产,确保粮食丰收', 5, '政策解读', '新华社', '新华社', 'PUBLISHED', 'APPROVED', 1, 0, NOW(), 892, '农业,生产,粮食安全'),

('智慧农业示范项目在全国推广', '<p>农业农村部在全国范围内推广智慧农业示范项目,运用物联网、大数据、人工智能等技术,实现农业生产精准化管理。</p><p>项目涵盖智能灌溉、精准施肥、病虫害监测等多个环节,预计可提高农业生产效率30%以上,减少农药化肥使用20%以上。</p>', '智慧农业示范项目运用AI技术实现精准化管理', 3, '活动资讯', '农业农村部', '农业农村部官网', 'PUBLISHED', 'APPROVED', 0, 1, NOW(), 567, '智慧农业,物联网,AI'),

('今日要闻: 多地启动防汛备汛工作', '<p>随着汛期临近,全国各地陆续启动防汛备汛工作。水利部门加强水库调度,完善应急预案,确保安全度汛。</p><p>气象部门预测,今年汛期我国降水总体偏多,部分地区可能出现洪涝灾害。各地已做好防范准备。</p>', '多地启动防汛备汛工作,确保安全度汛', 1, '今日新闻', '中央气象台', '中央气象台', 'PUBLISHED', 'APPROVED', 0, 0, NOW(), 2341, '防汛,气象,灾害预防'),

('系统维护通知', '<p>智壤卫士系统将于本周六凌晨2:00-6:00进行系统升级维护,届时部分功能将暂时无法使用。</p><p>维护内容包括: 数据库优化、安全补丁更新、新功能上线等。给您带来的不便敬请谅解!</p>', '系统将于周六凌晨维护升级', 2, '系统公告', '系统管理员', '智壤卫士', 'PUBLISHED', 'APPROVED', 1, 0, NOW(), 456, '系统维护,通知');

-- -----------------------------------------------------------
-- 初始化公告审核记录
-- -----------------------------------------------------------

INSERT INTO announcement_audit (announcement_id, audit_type, audit_result, comment, auditor_id, auditor_name, audit_time) VALUES
(1, 'SUBMIT', 'PENDING', '提交审核', 1, '管理员', NOW()),
(1, 'APPROVE', 'PASSED', '内容符合要求,同意发布', 1, '管理员', NOW()),
(2, 'SUBMIT', 'PENDING', '提交审核', 1, '管理员', NOW()),
(2, 'APPROVE', 'PASSED', '政策信息准确,同意发布', 1, '管理员', NOW()),
(3, 'SUBMIT', 'PENDING', '提交审核', 1, '管理员', NOW()),
(3, 'APPROVE', 'PASSED', '活动信息完整,同意发布', 1, '管理员', NOW()),
(4, 'SUBMIT', 'PENDING', '提交审核', 1, '管理员', NOW()),
(4, 'APPROVE', 'PASSED', '新闻内容真实,同意发布', 1, '管理员', NOW()),
(5, 'SUBMIT', 'PENDING', '提交审核', 1, '管理员', NOW()),
(5, 'APPROVE', 'PASSED', '通知内容合理,同意发布', 1, '管理员', NOW());