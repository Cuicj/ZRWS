-- 公告表添加AI分析和行政区域字段
-- 2026-06-27

-- 添加行政区域级别字段
ALTER TABLE zrws_announcement
ADD COLUMN IF NOT EXISTS admin_level VARCHAR(20) DEFAULT 'COUNTY' COMMENT '行政区域级别: PROVINCE/CITY/COUNTY/TOWNSHIP';

-- 添加省份字段
ALTER TABLE zrws_announcement
ADD COLUMN IF NOT EXISTS province VARCHAR(100) COMMENT '所属省份';

-- 添加城市字段
ALTER TABLE zrws_announcement
ADD COLUMN IF NOT EXISTS city VARCHAR(100) COMMENT '所属城市';

-- 添加县区字段
ALTER TABLE zrws_announcement
ADD COLUMN IF NOT EXISTS county VARCHAR(100) COMMENT '所属县区';

-- 添加乡镇字段
ALTER TABLE zrws_announcement
ADD COLUMN IF NOT EXISTS township VARCHAR(100) COMMENT '所属乡镇';

-- 添加地理坐标字段
ALTER TABLE zrws_announcement
ADD COLUMN IF NOT EXISTS longitude DOUBLE COMMENT '地理坐标-经度';

ALTER TABLE zrws_announcement
ADD COLUMN IF NOT EXISTS latitude DOUBLE COMMENT '地理坐标-纬度';

-- 添加AI分析字段
ALTER TABLE zrws_announcement
ADD COLUMN IF NOT EXISTS ai_summary TEXT COMMENT 'AI分析摘要';

ALTER TABLE zrws_announcement
ADD COLUMN IF NOT EXISTS ai_timeline TEXT COMMENT 'AI分析时间线（JSON格式）';

ALTER TABLE zrws_announcement
ADD COLUMN IF NOT EXISTS ai_keywords VARCHAR(500) COMMENT 'AI分析关键词';

-- 添加土地资源关联字段
ALTER TABLE zrws_announcement
ADD COLUMN IF NOT EXISTS land_type VARCHAR(50) COMMENT '相关土地类型';

ALTER TABLE zrws_announcement
ADD COLUMN IF NOT EXISTS land_resource_id BIGINT COMMENT '关联的土地资源ID';

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_announcement_admin_level ON zrws_announcement(admin_level);
CREATE INDEX IF NOT EXISTS idx_announcement_province ON zrws_announcement(province);
CREATE INDEX IF NOT EXISTS idx_announcement_city ON zrws_announcement(city);
CREATE INDEX IF NOT EXISTS idx_announcement_county ON zrws_announcement(county);
CREATE INDEX IF NOT EXISTS idx_announcement_township ON zrws_announcement(township);
CREATE INDEX IF NOT EXISTS idx_announcement_land_type ON zrws_announcement(land_type);

-- 添加注释
COMMENT ON COLUMN zrws_announcement.admin_level IS '行政区域级别: PROVINCE(省级)/CITY(市级)/COUNTY(县级)/TOWNSHIP(乡级)';
COMMENT ON COLUMN zrws_announcement.ai_summary IS 'AI分析生成的摘要内容';
COMMENT ON COLUMN zrws_announcement.ai_timeline IS 'AI分析生成的时间线，JSON数组格式';
COMMENT ON COLUMN zrws_announcement.ai_keywords IS 'AI分析提取的关键词，逗号分隔';
COMMENT ON COLUMN zrws_announcement.land_type IS '土地类型: 农田/林地/草地/水域/建设用地/未利用地/综合';
