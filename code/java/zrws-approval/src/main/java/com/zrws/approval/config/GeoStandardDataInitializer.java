package com.zrws.approval.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.GeoStandard;
import com.zrws.approval.domain.entity.RockSample;
import com.zrws.approval.domain.entity.RockStratumAnalysis;
import com.zrws.approval.mapper.GeoStandardMapper;
import com.zrws.approval.mapper.RockSampleMapper;
import com.zrws.approval.mapper.RockStratumAnalysisMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 地质标准数据初始化器
 * 预置土壤分类、岩石分类、矿物成分等标准数据
 */
@Slf4j
@Component
public class GeoStandardDataInitializer {

    @Autowired
    private GeoStandardMapper geoStandardMapper;
    @Autowired
    private RockSampleMapper rockSampleMapper;
    @Autowired
    private RockStratumAnalysisMapper analysisMapper;

    @PostConstruct
    public void init() {
        try {
            initGeoStandards();
            initRockAnalyses();
            initRockSamples();
            log.info("地质标准数据初始化完成");
        } catch (Exception e) {
            log.warn("地质标准数据初始化失败（可能表不存在）: {}", e.getMessage());
        }
    }

    private void initGeoStandards() {
        Long count = geoStandardMapper.selectCount(new LambdaQueryWrapper<GeoStandard>());
        if (count != null && count > 0) {
            log.info("地质标准数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化地质标准数据...");
        List<GeoStandard> standards = new ArrayList<>();

        // ===== 中国土壤分类 =====
        standards.add(createSoilChina("SC-001", "水稻土", "人为土", "水稻土纲",
            "{\"SiO2\":58.5,\"Al2O3\":18.2,\"Fe2O3\":6.8,\"CaO\":1.2,\"MgO\":1.5,\"K2O\":2.3,\"Na2O\":0.8,\"有机质\":3.5,\"pH\":6.5}",
            "{\"黏土矿物\":45,\"石英\":25,\"长石\":15,\"云母\":8,\"铁氧化物\":5,\"其他\":2}",
            "{\"质地\":\"壤土-黏壤土\",\"结构\":\"团粒结构\",\"容重\":1.25,\"孔隙度\":52}",
            "人工水耕熟化", "秦岭-淮河以南广大地区", "灰棕色-蓝灰色", "壤质-黏质", "团粒-棱柱状",
            2.0, 4.0, 1.1, 1.4, 45.0, 58.0, 1e-7, 1e-5, 1));

        standards.add(createSoilChina("SC-002", "红壤", "铁铝土", "红壤纲",
            "{\"SiO2\":52.3,\"Al2O3\":22.5,\"Fe2O3\":10.8,\"CaO\":0.3,\"MgO\":0.5,\"K2O\":1.8,\"Na2O\":0.3,\"有机质\":1.5,\"pH\":4.8}",
            "{\"高岭石\":40,\"石英\":25,\"赤铁矿\":15,\"三水铝石\":8,\"伊利石\":7,\"其他\":5}",
            "{\"质地\":\"黏壤土-黏土\",\"结构\":\"块状结构\",\"容重\":1.35,\"孔隙度\":48}",
            "脱硅富铝化", "长江以南红壤丘陵区", "红色-棕红色", "黏重", "块状-核状",
            3.0, 5.0, 1.2, 1.5, 40.0, 52.0, 1e-8, 1e-6, 2));

        standards.add(createSoilChina("SC-003", "黄棕壤", "淋溶土", "黄棕壤纲",
            "{\"SiO2\":55.8,\"Al2O3\":20.3,\"Fe2O3\":8.2,\"CaO\":0.8,\"MgO\":1.2,\"K2O\":2.1,\"Na2O\":0.6,\"有机质\":2.5,\"pH\":5.8}",
            "{\"伊利石\":35,\"高岭石\":20,\"石英\":25,\"绿泥石\":8,\"蛭石\":7,\"其他\":5}",
            "{\"质地\":\"黏壤土\",\"结构\":\"棱柱状结构\",\"容重\":1.3,\"孔隙度\":50}",
            "弱富铝化黏化", "长江中下游北亚热带", "黄棕色-棕黄色", "黏壤质", "棱柱状-块状",
            2.5, 4.5, 1.2, 1.45, 42.0, 55.0, 1e-7, 1e-5, 3));

        standards.add(createSoilChina("SC-004", "黄壤", "铁铝土", "黄壤纲",
            "{\"SiO2\":50.2,\"Al2O3\":24.5,\"Fe2O3\":9.2,\"CaO\":0.2,\"MgO\":0.4,\"K2O\":1.5,\"Na2O\":0.2,\"有机质\":2.0,\"pH\":4.8}",
            "{\"蛭石\":30,\"高岭石\":25,\"石英\":20,\"针铁矿\":12,\"伊利石\":8,\"其他\":5}",
            "{\"质地\":\"黏壤土\",\"结构\":\"核状结构\",\"容重\":1.32,\"孔隙度\":49}",
            "富铝化黄化", "云贵高原、华南山地", "黄色-蜡黄色", "黏重", "核状-块状",
            2.0, 4.0, 1.15, 1.45, 42.0, 54.0, 1e-7, 1e-6, 4));

        standards.add(createSoilChina("SC-005", "紫色土", "初育土", "紫色土纲",
            "{\"SiO2\":58.2,\"Al2O3\":15.6,\"Fe2O3\":5.8,\"CaO\":3.5,\"MgO\":2.2,\"K2O\":2.8,\"Na2O\":1.0,\"有机质\":1.2,\"pH\":7.5}",
            "{\"长石\":30,\"石英\":25,\"云母\":15,\"方解石\":12,\"蒙脱石\":8,\"其他\":10}",
            "{\"质地\":\"壤土-黏壤土\",\"结构\":\"粒状结构\",\"容重\":1.28,\"孔隙度\":51}",
            "紫色岩风化", "四川盆地、云贵川", "紫色-紫棕色", "壤质-黏壤质", "粒状-小块状",
            2.0, 4.5, 1.2, 1.4, 45.0, 55.0, 1e-6, 1e-4, 5));

        standards.add(createSoilChina("SC-006", "石灰土", "初育土", "石灰土纲",
            "{\"SiO2\":48.5,\"Al2O3\":16.8,\"Fe2O3\":6.5,\"CaO\":8.5,\"MgO\":2.5,\"K2O\":1.8,\"Na2O\":0.5,\"有机质\":2.2,\"pH\":7.8}",
            "{\"方解石\":35,\"石英\":20,\"蒙脱石\":15,\"伊利石\":10,\"高岭石\":8,\"其他\":12}",
            "{\"质地\":\"黏壤土\",\"结构\":\"粒状结构\",\"容重\":1.25,\"孔隙度\":52}",
            "碳酸盐岩风化", "贵州、广西、云南", "棕色-红棕色", "黏壤质", "粒状-团粒状",
            2.5, 4.5, 1.15, 1.35, 48.0, 58.0, 1e-6, 1e-4, 6));

        standards.add(createSoilChina("SC-007", "潮土", "半水成土", "潮土纲",
            "{\"SiO2\":60.2,\"Al2O3\":15.8,\"Fe2O3\":5.5,\"CaO\":2.5,\"MgO\":1.8,\"K2O\":2.2,\"Na2O\":1.2,\"有机质\":1.8,\"pH\":7.2}",
            "{\"石英\":30,\"长石\":20,\"蒙脱石\":15,\"伊利石\":12,\"方解石\":8,\"其他\":15}",
            "{\"质地\":\"砂壤土-壤土\",\"结构\":\"层状结构\",\"容重\":1.3,\"孔隙度\":50}",
            "河流冲积", "黄淮海平原、长江中下游", "灰黄色-棕色", "砂壤-壤质", "层状-碎块状",
            3.0, 5.0, 1.2, 1.45, 45.0, 55.0, 1e-5, 1e-3, 7));

        standards.add(createSoilChina("SC-008", "菜园土", "人为土", "肥熟土纲",
            "{\"SiO2\":56.8,\"Al2O3\":17.5,\"Fe2O3\":6.2,\"CaO\":2.0,\"MgO\":1.6,\"K2O\":2.5,\"Na2O\":0.9,\"有机质\":3.8,\"pH\":6.8}",
            "{\"腐殖质\":20,\"黏土矿物\":35,\"石英\":20,\"长石\":10,\"磷灰石\":5,\"其他\":10}",
            "{\"质地\":\"壤土\",\"结构\":\"团粒结构\",\"容重\":1.15,\"孔隙度\":56}",
            "人工旱耕熟化", "城郊区、蔬菜基地", "暗棕-黑褐色", "壤质", "团粒状",
            2.0, 3.5, 1.05, 1.3, 50.0, 60.0, 1e-5, 1e-3, 8));

        // ===== 国际WRB土壤分类 =====
        standards.add(createSoilWRB("WRB-001", "高活性淋溶土", "Luvisols",
            "{\"SiO2\":55.0,\"Al2O3\":21.0,\"Fe2O3\":7.5,\"CaO\":1.0,\"MgO\":1.3,\"K2O\":2.0,\"有机质\":2.0,\"pH\":6.0}",
            "{\"蒙脱石-伊利石\":50,\"石英\":25,\"长石\":15,\"其他\":10}",
            "温带湿润地区"));
        standards.add(createSoilWRB("WRB-002", "低活性强酸土", "Acrisols",
            "{\"SiO2\":50.0,\"Al2O3\":25.0,\"Fe2O3\":11.0,\"CaO\":0.2,\"MgO\":0.3,\"K2O\":1.5,\"有机质\":1.5,\"pH\":4.5}",
            "{\"高岭石\":45,\"石英\":25,\"铁氧化物\":20,\"其他\":10}",
            "热带亚热带湿润地区"));
        standards.add(createSoilWRB("WRB-003", "铁铝土", "Ferralsols",
            "{\"SiO2\":42.0,\"Al2O3\":30.0,\"Fe2O3\":18.0,\"CaO\":0.1,\"MgO\":0.2,\"K2O\":0.5,\"有机质\":1.0,\"pH\":4.2}",
            "{\"高岭石\":35,\"三水铝石\":25,\"赤铁矿\":25,\"石英\":10,\"其他\":5}",
            "热带湿热地区"));
        standards.add(createSoilWRB("WRB-004", "钙积土", "Calcisols",
            "{\"SiO2\":50.0,\"Al2O3\":12.0,\"Fe2O3\":4.5,\"CaO\":15.0,\"MgO\":3.0,\"K2O\":1.8,\"有机质\":0.8,\"pH\":8.2}",
            "{\"方解石\":40,\"石英\":20,\"黏土矿物\":15,\"长石\":10,\"其他\":15}",
            "干旱半干旱地区"));
        standards.add(createSoilWRB("WRB-005", "潜育土", "Gleysols",
            "{\"SiO2\":52.0,\"Al2O3\":18.0,\"Fe2O3\":5.0,\"CaO\":1.5,\"MgO\":1.8,\"K2O\":2.0,\"有机质\":4.0,\"pH\":5.5}",
            "{\"黏土矿物\":40,\"石英\":25,\"硫化物\":5,\"有机质\":20,\"其他\":10}",
            "地下水位高的低洼地区"));
        standards.add(createSoilWRB("WRB-006", "有机土", "Histosols",
            "{\"SiO2\":15.0,\"Al2O3\":3.0,\"Fe2O3\":1.0,\"有机质\":65.0,\"pH\":4.0}",
            "{\"腐殖质\":70,\"植物残体\":20,\"矿物质\":10}",
            "泥炭沼泽地区"));
        standards.add(createSoilWRB("WRB-007", "雏形土", "Cambisols",
            "{\"SiO2\":58.0,\"Al2O3\":16.0,\"Fe2O3\":6.0,\"CaO\":2.0,\"MgO\":2.5,\"K2O\":2.8,\"有机质\":1.5,\"pH\":6.5}",
            "{\"原生矿物\":45,\"黏土矿物\":25,\"石英\":20,\"其他\":10}",
            "温带到热带广泛分布"));
        standards.add(createSoilWRB("WRB-008", "黏绨土", "Nitisols",
            "{\"SiO2\":45.0,\"Al2O3\":28.0,\"Fe2O3\":15.0,\"CaO\":0.3,\"MgO\":0.5,\"K2O\":1.2,\"有机质\":1.2,\"pH\":5.2}",
            "{\"高岭石-氧化铁\":60,\"石英\":15,\"其他\":25}",
            "热带火山岩地区"));

        // ===== 岩浆岩 =====
        standards.add(createRock("ROCK-IG-001", "花岗岩", GeoStandard.Category.ROCK_IGNEOUS.name(),
            "{\"SiO2\":72.0,\"Al2O3\":14.0,\"Fe2O3\":2.5,\"CaO\":1.5,\"MgO\":0.8,\"K2O\":4.5,\"Na2O\":3.5}",
            "{\"石英\":30,\"正长石\":40,\"斜长石\":15,\"黑云母\":8,\"角闪石\":5,\"其他\":2}",
            "{\"结构\":\"全晶质粗粒结构\",\"构造\":\"块状构造\",\"产状\":\"深成岩\"}",
            "{\"抗压强度\":100,\"抗拉强度\":5,\"弹性模量\":50,\"泊松比\":0.25}",
            "Si、K", "酸性岩浆结晶分异", "中国东南部、华南", "肉红色-灰白色", "粗粒结构", "块状构造",
            6.0, 6.5, 2.60, 2.75, 0.5, 1.5, 1e-9, 1e-7, 1));
        standards.add(createRock("ROCK-IG-002", "玄武岩", GeoStandard.Category.ROCK_IGNEOUS.name(),
            "{\"SiO2\":48.0,\"Al2O3\":16.0,\"Fe2O3\":12.0,\"CaO\":10.0,\"MgO\":8.0,\"K2O\":1.0,\"Na2O\":3.0}",
            "{\"斜长石\":55,\"辉石\":25,\"橄榄石\":10,\"磁铁矿\":5,\"玻璃质\":5}",
            "{\"结构\":\"隐晶质斑状结构\",\"构造\":\"气孔杏仁构造\",\"产状\":\"喷出岩\"}",
            "{\"抗压强度\":150,\"抗拉强度\":8,\"弹性模量\":70,\"泊松比\":0.28}",
            "Fe、Mg、Ca", "基性岩浆喷出冷凝", "中国西南部、东北", "灰黑色-深灰色", "致密隐晶", "块状-气孔",
            5.0, 6.0, 2.80, 3.10, 0.5, 2.0, 1e-10, 1e-8, 2));
        standards.add(createRock("ROCK-IG-003", "闪长岩", GeoStandard.Category.ROCK_IGNEOUS.name(),
            "{\"SiO2\":58.0,\"Al2O3\":17.0,\"Fe2O3\":7.5,\"CaO\":6.5,\"MgO\":3.5,\"K2O\":2.5,\"Na2O\":4.5}",
            "{\"斜长石\":50,\"角闪石\":25,\"黑云母\":10,\"石英\":5,\"辉石\":5,\"其他\":5}",
            "{\"结构\":\"半自形粒状结构\",\"构造\":\"块状构造\",\"产状\":\"浅成岩\"}",
            "{\"抗压强度\":120,\"抗拉强度\":6,\"弹性模量\":60,\"泊松比\":0.26}",
            "Al、Ca、Fe", "中性岩浆侵入", "华北、长江中下游", "灰色-深灰色", "中粒结构", "块状构造",
            5.5, 6.0, 2.70, 2.90, 0.5, 1.5, 1e-9, 1e-7, 3));
        standards.add(createRock("ROCK-IG-004", "流纹岩", GeoStandard.Category.ROCK_IGNEOUS.name(),
            "{\"SiO2\":73.0,\"Al2O3\":13.5,\"Fe2O3\":2.0,\"CaO\":1.2,\"MgO\":0.5,\"K2O\":5.0,\"Na2O\":3.8}",
            "{\"石英斑晶\":15,\"长石斑晶\":10,\"玻璃质基质\":70,\"其他\":5}",
            "{\"结构\":\"斑状结构\",\"构造\":\"流纹构造\",\"产状\":\"喷出岩\"}",
            "{\"抗压强度\":80,\"抗拉强度\":4,\"弹性模量\":40,\"泊松比\":0.22}",
            "Si、K", "酸性岩浆喷出", "东南沿海火山岩带", "灰白色-粉红色", "斑状隐晶", "流纹构造",
            6.0, 7.0, 2.40, 2.65, 1.0, 5.0, 1e-8, 1e-6, 4));
        standards.add(createRock("ROCK-IG-005", "橄榄岩", GeoStandard.Category.ROCK_IGNEOUS.name(),
            "{\"SiO2\":40.0,\"Al2O3\":5.0,\"Fe2O3\":10.0,\"CaO\":3.0,\"MgO\":35.0,\"K2O\":0.3,\"Na2O\":0.5}",
            "{\"橄榄石\":55,\"辉石\":35,\"角闪石\":5,\"其他\":5}",
            "{\"结构\":\"自形粒状结构\",\"构造\":\"块状构造\",\"产状\":\"深成岩\"}",
            "{\"抗压强度\":180,\"抗拉强度\":10,\"弹性模量\":80,\"泊松比\":0.30}",
            "Mg、Fe、Ni", "超基性岩浆结晶", "造山带、地幔岩", "暗绿色-黑绿色", "粗粒结构", "块状构造",
            6.5, 7.5, 3.10, 3.40, 0.1, 1.0, 1e-11, 1e-9, 5));

        // ===== 沉积岩 =====
        standards.add(createRock("ROCK-SE-001", "石灰岩", GeoStandard.Category.ROCK_SEDIMENTARY.name(),
            "{\"CaO\":55.0,\"MgO\":2.0,\"SiO2\":8.0,\"Al2O3\":2.5,\"Fe2O3\":1.0,\"烧失量\":41.0}",
            "{\"方解石\":85,\"白云石\":8,\"石英\":5,\"黏土矿物\":2}",
            "{\"结构\":\"微晶结构\",\"构造\":\"层理构造\",\"分类\":\"化学沉积岩\"}",
            "{\"抗压强度\":80,\"抗拉强度\":4,\"弹性模量\":45,\"泊松比\":0.24}",
            "Ca、Mg", "海相/湖相化学沉积", "分布广泛", "灰色-深灰色", "微晶-鲕粒", "层理-块状",
            3.0, 4.0, 2.50, 2.80, 1.0, 5.0, 1e-8, 1e-5, 1));
        standards.add(createRock("ROCK-SE-002", "砂岩", GeoStandard.Category.ROCK_SEDIMENTARY.name(),
            "{\"SiO2\":78.0,\"Al2O3\":8.0,\"Fe2O3\":2.5,\"CaO\":3.0,\"MgO\":1.0,\"K2O\":2.0,\"Na2O\":1.5}",
            "{\"石英\":65,\"长石\":15,\"岩屑\":10,\"胶结物\":10}",
            "{\"结构\":\"砂状结构\",\"构造\":\"层理构造\",\"分类\":\"陆源碎屑岩\"}",
            "{\"抗压强度\":60,\"抗拉强度\":3,\"弹性模量\":30,\"泊松比\":0.20}",
            "Si", "机械搬运沉积", "分布广泛", "灰黄色-灰白色", "砂粒结构", "层理构造",
            2.0, 3.5, 2.20, 2.65, 5.0, 25.0, 1e-5, 1e-2, 2));
        standards.add(createRock("ROCK-SE-003", "页岩", GeoStandard.Category.ROCK_SEDIMENTARY.name(),
            "{\"SiO2\":58.0,\"Al2O3\":16.0,\"Fe2O3\":5.5,\"CaO\":3.0,\"MgO\":2.5,\"K2O\":3.5,\"Na2O\":1.0,\"有机质\":1.5}",
            "{\"黏土矿物\":55,\"石英\":20,\"长石\":10,\"有机质\":8,\"其他\":7}",
            "{\"结构\":\"泥质结构\",\"构造\":\"页理构造\",\"分类\":\"黏土岩\"}",
            "{\"抗压强度\":40,\"抗拉强度\":2,\"弹性模量\":20,\"泊松比\":0.18}",
            "Al、K", "细粒悬浮物沉积", "分布广泛", "黑色-灰绿色", "泥质结构", "页理构造",
            2.5, 3.5, 2.40, 2.70, 5.0, 20.0, 1e-9, 1e-7, 3));
        standards.add(createRock("ROCK-SE-004", "白云岩", GeoStandard.Category.ROCK_SEDIMENTARY.name(),
            "{\"MgO\":22.0,\"CaO\":30.0,\"SiO2\":3.0,\"Fe2O3\":0.5,\"烧失量\":44.0}",
            "{\"白云石\":90,\"方解石\":5,\"石英\":3,\"黏土矿物\":2}",
            "{\"结构\":\"结晶结构\",\"构造\":\"层理构造\",\"分类\":\"化学沉积岩\"}",
            "{\"抗压强度\":100,\"抗拉强度\":5,\"弹性模量\":55,\"泊松比\":0.25}",
            "Mg、Ca", "白云岩化作用", "华北、华南", "灰白色-深灰色", "晶粒结构", "层理构造",
            3.5, 4.5, 2.70, 2.90, 0.5, 2.0, 1e-9, 1e-7, 4));
        standards.add(createRock("ROCK-SE-005", "砾岩", GeoStandard.Category.ROCK_SEDIMENTARY.name(),
            "{\"SiO2\":70.0,\"Al2O3\":10.0,\"Fe2O3\":4.0,\"CaO\":4.0,\"其他\":12}",
            "{\"岩屑砾石\":60,\"石英砾石\":20,\"填隙物\":20}",
            "{\"结构\":\"砾状结构\",\"构造\":\"层理构造\",\"分类\":\"粗碎屑岩\"}",
            "{\"抗压强度\":50,\"抗拉强度\":2.5,\"弹性模量\":25,\"泊松比\":0.22}",
            "Si", "快速堆积沉积", "山前地带、河流", "杂色-灰色", "砾状结构", "块状-层理",
            2.0, 3.0, 2.30, 2.70, 5.0, 20.0, 1e-4, 1e-2, 5));

        // ===== 变质岩 =====
        standards.add(createRock("ROCK-ME-001", "片麻岩", GeoStandard.Category.ROCK_METAMORPHIC.name(),
            "{\"SiO2\":68.0,\"Al2O3\":15.5,\"Fe2O3\":5.0,\"CaO\":2.5,\"MgO\":2.0,\"K2O\":3.5,\"Na2O\":3.5}",
            "{\"石英\":30,\"长石\":40,\"黑云母\":12,\"角闪石\":10,\"其他\":8}",
            "{\"结构\":\"鳞片粒状变晶结构\",\"构造\":\"片麻状构造\",\"变质程度\":\"区域中高温\"}",
            "{\"抗压强度\":120,\"抗拉强度\":6,\"弹性模量\":55,\"泊松比\":0.25}",
            "Si、Al", "区域变质作用", "秦岭-大别山、华北", "灰白色-浅灰色", "粒状鳞片", "片麻状构造",
            5.5, 6.5, 2.60, 2.80, 0.5, 1.5, 1e-9, 1e-7, 1));
        standards.add(createRock("ROCK-ME-002", "大理岩", GeoStandard.Category.ROCK_METAMORPHIC.name(),
            "{\"CaO\":53.0,\"MgO\":3.0,\"SiO2\":3.5,\"Al2O3\":1.0,\"Fe2O3\":0.5,\"烧失量\":42.0}",
            "{\"方解石\":75,\"白云石\":20,\"石英\":3,\"其他\":2}",
            "{\"结构\":\"粒状变晶结构\",\"构造\":\"块状/条带构造\",\"变质程度\":\"接触/区域\"}",
            "{\"抗压强度\":90,\"抗拉强度\":4.5,\"弹性模量\":50,\"泊松比\":0.24}",
            "Ca", "碳酸盐岩变质", "云南、四川、湖北", "白色-杂色", "粒状变晶", "块状-条带",
            3.0, 4.0, 2.60, 2.85, 0.5, 2.0, 1e-9, 1e-7, 2));
        standards.add(createRock("ROCK-ME-003", "板岩", GeoStandard.Category.ROCK_METAMORPHIC.name(),
            "{\"SiO2\":62.0,\"Al2O3\":18.0,\"Fe2O3\":7.0,\"CaO\":1.0,\"MgO\":2.5,\"K2O\":3.5,\"Na2O\":1.0,\"有机质\":1.5}",
            "{\"黏土矿物\":45,\"石英\":25,\"绿泥石\":10,\"绢云母\":10,\"其他\":10}",
            "{\"结构\":\"变余泥质结构\",\"构造\":\"板状构造\",\"变质程度\":\"低级区域变质\"}",
            "{\"抗压强度\":60,\"抗拉强度\":3,\"弹性模量\":30,\"泊松比\":0.20}",
            "Al、Si", "页岩低级变质", "板溪群、南华系", "深灰色-黑色", "隐晶质", "板状劈理",
            4.0, 5.0, 2.50, 2.80, 1.0, 5.0, 1e-9, 1e-7, 3));
        standards.add(createRock("ROCK-ME-004", "千枚岩", GeoStandard.Category.ROCK_METAMORPHIC.name(),
            "{\"SiO2\":60.0,\"Al2O3\":19.0,\"Fe2O3\":6.5,\"CaO\":0.8,\"MgO\":2.0,\"K2O\":4.0,\"Na2O\":0.8}",
            "{\"绢云母\":40,\"石英\":25,\"绿泥石\":15,\"钠长石\":10,\"其他\":10}",
            "{\"结构\":\"细粒鳞片变晶结构\",\"构造\":\"千枚状构造\",\"变质程度\":\"低级区域变质\"}",
            "{\"抗压强度\":50,\"抗拉强度\":2.5,\"弹性模量\":25,\"泊松比\":0.18}",
            "Al、K", "泥岩低级变质", "华南、西南", "灰绿色-浅灰色", "细粒鳞片", "千枚状构造",
            2.5, 3.5, 2.60, 2.80, 1.0, 4.0, 1e-8, 1e-6, 4));
        standards.add(createRock("ROCK-ME-005", "石英岩", GeoStandard.Category.ROCK_METAMORPHIC.name(),
            "{\"SiO2\":95.0,\"Al2O3\":2.0,\"Fe2O3\":0.8,\"CaO\":0.3,\"其他\":1.9}",
            "{\"石英\":95,\"长石\":2,\"磁铁矿\":1,\"其他\":2}",
            "{\"结构\":\"粒状变晶结构\",\"构造\":\"块状构造\",\"变质程度\":\"区域变质\"}",
            "{\"抗压强度\":200,\"抗拉强度\":12,\"弹性模量\":90,\"泊松比\":0.22}",
            "Si", "砂岩变质重结晶", "华北、西北", "白色-灰白色", "粒状变晶", "块状构造",
            6.5, 7.5, 2.60, 2.70, 0.2, 1.0, 1e-10, 1e-8, 5));

        // 插入所有标准数据
        for (GeoStandard standard : standards) {
            geoStandardMapper.insert(standard);
        }
        log.info("地质标准数据初始化完成，共 {} 条", standards.size());
    }

    private GeoStandard createSoilChina(String code, String name, String subcategory, String parentMaterial,
                                         String chemical, String mineral, String physical,
                                         String formation, String distribution, String color, String texture, String structure,
                                         double hMin, double hMax, double dMin, double dMax,
                                         double pMin, double pMax, double permMin, double permMax, int sort) {
        GeoStandard s = new GeoStandard();
        s.setStandardCode(code);
        s.setStandardName(name);
        s.setCategory(GeoStandard.Category.SOIL_CHINA.name());
        s.setSubcategory(subcategory);
        s.setClassificationSystem(GeoStandard.ClassificationSystem.CST.name());
        s.setParentMaterial(parentMaterial);
        s.setChemicalComposition(chemical);
        s.setMineralComposition(mineral);
        s.setPhysicalProperties(physical);
        s.setFormationEnvironment(formation);
        s.setDistribution(distribution);
        s.setColorDescription(color);
        s.setTextureDescription(texture);
        s.setStructureDescription(structure);
        s.setHardnessMin(hMin);
        s.setHardnessMax(hMax);
        s.setDensityMin(dMin);
        s.setDensityMax(dMax);
        s.setPorosityMin(pMin);
        s.setPorosityMax(pMax);
        s.setPermeabilityMin(permMin);
        s.setPermeabilityMax(permMax);
        s.setSortOrder(sort);
        s.setStatus("ACTIVE");
        s.setIsDeleted(0);
        return s;
    }

    private GeoStandard createSoilWRB(String code, String name, String englishName,
                                       String chemical, String mineral, String distribution) {
        GeoStandard s = new GeoStandard();
        s.setStandardCode(code);
        s.setStandardName(name);
        s.setCategory(GeoStandard.Category.SOIL_WRB.name());
        s.setSubcategory(englishName);
        s.setClassificationSystem(GeoStandard.ClassificationSystem.WRB.name());
        s.setChemicalComposition(chemical);
        s.setMineralComposition(mineral);
        s.setDistribution(distribution);
        s.setSortOrder(0);
        s.setStatus("ACTIVE");
        s.setIsDeleted(0);
        return s;
    }

    private GeoStandard createRock(String code, String name, String category,
                                    String chemical, String mineral, String physical, String mechanical,
                                    String elements, String formation, String distribution,
                                    String color, String texture, String structure,
                                    double hMin, double hMax, double dMin, double dMax,
                                    double pMin, double pMax, double permMin, double permMax, int sort) {
        GeoStandard s = new GeoStandard();
        s.setStandardCode(code);
        s.setStandardName(name);
        s.setCategory(category);
        s.setClassificationSystem(GeoStandard.ClassificationSystem.GB_T_17412.name());
        s.setChemicalComposition(chemical);
        s.setMineralComposition(mineral);
        s.setPhysicalProperties(physical);
        s.setMechanicalProperties(mechanical);
        s.setTypicalElements(elements);
        s.setFormationEnvironment(formation);
        s.setDistribution(distribution);
        s.setColorDescription(color);
        s.setTextureDescription(texture);
        s.setStructureDescription(structure);
        s.setHardnessMin(hMin);
        s.setHardnessMax(hMax);
        s.setDensityMin(dMin);
        s.setDensityMax(dMax);
        s.setPorosityMin(pMin);
        s.setPorosityMax(pMax);
        s.setPermeabilityMin(permMin);
        s.setPermeabilityMax(permMax);
        s.setSortOrder(sort);
        s.setStatus("ACTIVE");
        s.setIsDeleted(0);
        return s;
    }

    private void initRockSamples() {
        Long count = rockSampleMapper.selectCount(new LambdaQueryWrapper<RockSample>());
        if (count != null && count > 0) {
            log.info("岩矿样品数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化岩矿样品Mock数据...");
        RockSample[] samples = {
            createSample("RS-2026-0617-001", 1L, "RSA-2026-0617-001", "ZRS-2026-0617-001",
                "ZK1-01", RockSample.SampleType.ROCK.name(),
                "望城区乔口镇ZK1号孔", 28.45672, 112.83521, 35.2, 5.0, "m", 1.2,
                "王工", "2026-06-17T09:30:00",
                "粉质黏土", "黄褐色", "可塑", "块状", RockSample.WeatheringDegree.RESIDUAL_SOIL.name(),
                "{\"SiO2\":58.5,\"Al2O3\":18.2,\"Fe2O3\":6.8,\"CaO\":1.2,\"MgO\":1.5,\"K2O\":2.3,\"Na2O\":0.8,\"烧失量\":5.2,\"有机质\":3.5}",
                "{\"石英\":25,\"长石\":15,\"黏土矿物\":45,\"云母\":8,\"铁氧化物\":5,\"有机质\":2}",
                "{\"Cu\":25,\"Pb\":18,\"Zn\":32,\"Cr\":65,\"Ni\":28,\"As\":8,\"Hg\":0.05,\"Se\":0.3}",
                "{\"含水率\":32.5,\"密度\":1.85,\"孔隙比\":0.85,\"液限\":38,\"塑限\":22,\"塑性指数\":16}",
                RockSample.AnalysisMethod.XRF.name(), "X射线荧光光谱仪",
                "李工", "2026-06-17T14:00:00",
                "水稻土/粉质黏土", 92.5, "[\"SC-001\",\"SC-007\"]", "COMPLETED"),
            createSample("RS-2026-0617-002", 1L, "RSA-2026-0617-001", "ZRS-2026-0617-001",
                "ZK1-02", RockSample.SampleType.ROCK.name(),
                "望城区乔口镇ZK1号孔", 28.45672, 112.83521, 32.2, 12.5, "m", 2.5,
                "王工", "2026-06-17T10:00:00",
                "砂砾石层", "灰黄色", "中粗砂", "松散", RockSample.WeatheringDegree.FRESH.name(),
                "{\"SiO2\":68.5,\"Al2O3\":10.2,\"Fe2O3\":3.8,\"CaO\":2.5,\"MgO\":1.2,\"K2O\":2.0,\"Na2O\":1.8}",
                "{\"石英\":55,\"长石\":20,\"岩屑\":10,\"黏土矿物\":8,\"重矿物\":7}",
                "{\"Cu\":15,\"Pb\":10,\"Zn\":20,\"Cr\":45,\"Ni\":15}",
                "{\"含水率\":12.5,\"密度\":2.25,\"孔隙度\":32,\"渗透系数\":0.005}",
                RockSample.AnalysisMethod.ICP.name(), "ICP-OES光谱仪",
                "张工", "2026-06-17T15:00:00",
                "冲洪积砂砾石", 88.3, "[\"SC-007\"]", "COMPLETED"),
            createSample("RS-2026-0617-003", 1L, "RSA-2026-0617-001", "ZRS-2026-0617-001",
                "ZK1-03", RockSample.SampleType.ROCK.name(),
                "望城区乔口镇ZK1号孔", 28.45672, 112.83521, 28.5, 22.0, "m", 3.8,
                "王工", "2026-06-17T10:30:00",
                "强风化泥岩", "褐红色", "泥质", "碎块状", RockSample.WeatheringDegree.HIGHLY_WEATHERED.name(),
                "{\"SiO2\":52.0,\"Al2O3\":20.5,\"Fe2O3\":9.5,\"CaO\":1.0,\"MgO\":2.0,\"K2O\":3.0,\"Na2O\":0.8}",
                "{\"黏土矿物\":40,\"石英\":20,\"长石\":10,\"铁氧化物\":15,\"绿泥石\":10,\"其他\":5}",
                "{\"Cu\":22,\"Pb\":15,\"Zn\":28,\"Cr\":55,\"Ni\":22,\"V\":65}",
                "{\"含水率\":15.0,\"密度\":2.35,\"孔隙度\":25,\"抗压强度\":8}",
                RockSample.AnalysisMethod.XRD.name(), "X射线衍射仪",
                "李工", "2026-06-17T16:00:00",
                "强风化泥岩/红层", 90.8, "[\"ROCK-SE-003\"]", "COMPLETED"),
            createSample("RS-2026-0617-004", 1L, "RSA-2026-0617-001", "ZRS-2026-0617-001",
                "ZK1-04", RockSample.SampleType.ROCK.name(),
                "望城区乔口镇ZK1号孔", 28.45672, 112.83521, 18.2, 40.0, "m", 5.2,
                "王工", "2026-06-17T11:00:00",
                "中风化泥岩", "深灰色", "泥质", "中厚层状", RockSample.WeatheringDegree.MODERATELY_WEATHERED.name(),
                "{\"SiO2\":48.5,\"Al2O3\":22.8,\"Fe2O3\":7.2,\"CaO\":0.8,\"MgO\":2.5,\"K2O\":3.5,\"Na2O\":0.5,\"有机碳\":0.3}",
                "{\"伊利石\":35,\"蒙脱石\":15,\"石英\":20,\"长石\":8,\"黄铁矿\":3,\"有机质\":5,\"其他\":14}",
                "{\"Cu\":18,\"Pb\":12,\"Zn\":22,\"Cr\":60,\"Ni\":25,\"V\":80,\"U\":2.5}",
                "{\"含水率\":2.5,\"密度\":2.65,\"孔隙度\":8,\"抗压强度\":12,\"弹性模量\":1500}",
                RockSample.AnalysisMethod.COMPREHENSIVE.name(), "XRF+XRD+ICP",
                "张总工", "2026-06-17T17:00:00",
                "泥岩/碎屑沉积岩", 95.2, "[\"ROCK-SE-003\"]", "COMPLETED"),
            createSample("RS-2026-0617-005", 1L, "RSA-2026-0617-001", "ZRS-2026-0617-001",
                "ZK1-05", RockSample.SampleType.ROCK.name(),
                "望城区乔口镇ZK1号孔", 28.45672, 112.83521, 5.5, 65.0, "m", 4.8,
                "王工", "2026-06-17T11:30:00",
                "微风化石灰岩", "浅灰色", "微晶", "块状", RockSample.WeatheringDegree.SLIGHTLY_WEATHERED.name(),
                "{\"CaO\":54.5,\"MgO\":1.8,\"SiO2\":2.5,\"Al2O3\":0.8,\"Fe2O3\":0.3,\"烧失量\":42.0}",
                "{\"方解石\":92,\"白云石\":4,\"石英\":2,\"黏土矿物\":1,\"有机质\":1}",
                "{\"Sr\":800,\"Mn\":80,\"Fe\":500,\"Mg\":3000}",
                "{\"密度\":2.70,\"孔隙度\":1.5,\"抗压强度\":65,\"弹性模量\":45000,\"泊松比\":0.24}",
                RockSample.AnalysisMethod.CHEMICAL.name(), "化学分析+XRD",
                "李工", "2026-06-17T18:00:00",
                "石灰岩/碳酸盐岩", 96.8, "[\"ROCK-SE-001\",\"ROCK-ME-002\"]", "COMPLETED")
        };
        for (RockSample sample : samples) {
            rockSampleMapper.insert(sample);
        }
        log.info("岩矿样品Mock数据初始化完成，共 {} 条", samples.length);
    }

    private RockSample createSample(String code, Long analysisId, String analysisCode, String missionCode,
                                     String name, String type, String location, double lat, double lng, double elev,
                                     double depth, String depthUnit, double weight,
                                     String collector, String collectTime,
                                     String lithology, String color, String texture, String structure,
                                     String weathering,
                                     String chemical, String mineral, String trace, String physical,
                                     String method, String instrument,
                                     String analyst, String analysisTime,
                                     String aiId, double aiConf, String aiMatched, String status) {
        RockSample s = new RockSample();
        s.setSampleCode(code);
        s.setAnalysisId(analysisId);
        s.setAnalysisCode(analysisCode);
        s.setMissionCode(missionCode);
        s.setSampleName(name);
        s.setSampleType(type);
        s.setLocation(location);
        s.setLatitude(lat);
        s.setLongitude(lng);
        s.setElevation(elev);
        s.setDepth(depth);
        s.setDepthUnit(depthUnit);
        s.setWeight(weight);
        s.setCollector(collector);
        s.setCollectTime(LocalDateTime.parse(collectTime));
        s.setLithologyEstimate(lithology);
        s.setColor(color);
        s.setTexture(texture);
        s.setStructure(structure);
        s.setWeatheringDegree(weathering);
        s.setChemicalData(chemical);
        s.setMineralData(mineral);
        s.setTraceElements(trace);
        s.setPhysicalData(physical);
        s.setAnalysisMethod(method);
        s.setAnalysisInstrument(instrument);
        s.setAnalyst(analyst);
        s.setAnalysisTime(LocalDateTime.parse(analysisTime));
        s.setAiIdentification(aiId);
        s.setAiConfidence(aiConf);
        s.setAiMatchedStandards(aiMatched);
        s.setStatus(status);
        s.setIsDeleted(0);
        return s;
    }

    private void initRockAnalyses() {
        Long count = analysisMapper.selectCount(new LambdaQueryWrapper<RockStratumAnalysis>());
        if (count != null && count > 0) {
            log.info("岩层分析数据已存在，跳过初始化");
            return;
        }
        log.info("开始初始化岩层分析Mock数据...");
        RockStratumAnalysis[] analyses = {
            createAnalysis("RSA-2026-0617-001", 1L, "ZRS-2026-0617-001",
                "乔口镇综合地质勘察", "望城区乔口镇盘龙岭村", 28.45672, 112.83521,
                "COMPREHENSIVE", "钻孔+地质雷达", 6, 80.0, 5,
                "[{\"depth\":\"0-3m\",\"lithology\":\"粉质黏土\",\"thickness\":3.0},{\"depth\":\"3-8m\",\"lithology\":\"砂砾石层\",\"thickness\":5.0},{\"depth\":\"8-25m\",\"lithology\":\"强风化泥岩\",\"thickness\":17.0},{\"depth\":\"25-50m\",\"lithology\":\"中风化泥岩\",\"thickness\":25.0},{\"depth\":\"50-80m\",\"lithology\":\"微风化石灰岩\",\"thickness\":30.0}]",
                "[{\"name\":\"粉质黏土层\",\"depth\":\"0-3m\",\"type\":\"Q4al+pl\",\"strength\":\"120kPa\",\"permeability\":\"低渗透\",\"thickness\":3.0},{\"name\":\"砂砾石层\",\"depth\":\"3-8m\",\"type\":\"Q2al\",\"strength\":\"280kPa\",\"permeability\":\"中渗透\",\"thickness\":5.0},{\"name\":\"强风化泥岩\",\"depth\":\"8-25m\",\"type\":\"E2s\",\"strength\":\"350kPa\",\"permeability\":\"低渗透\",\"thickness\":17.0},{\"name\":\"中风化泥岩\",\"depth\":\"25-50m\",\"type\":\"E2s\",\"strength\":\"12MPa\",\"permeability\":\"微渗透\",\"thickness\":25.0},{\"name\":\"微风化石灰岩\",\"depth\":\"50-80m\",\"type\":\"C1d\",\"strength\":\"60MPa\",\"permeability\":\"微渗透\",\"thickness\":30.0}]",
                "{\"structure\":\"单斜构造\",\"dip\":\"15°\",\"dipDirection\":\"135°\",\"fractures\":2}",
                "DEEP_LEARNING", 94.8,
                "AI分析结果显示：该区域岩层结构较为稳定，从上到下依次为第四系覆盖层、强风化泥岩、中风化泥岩、微风化石灰岩。岩层倾角约15°，整体为单斜构造。50米以下为硬质石灰岩，承载力较高。取样法分析与物探结果吻合度94.2%。",
                "{\"boreholeCount\":6,\"avgRQD\":\"78.5%\",\"rockQuality\":\"良好\",\"bearingCapacity\":\"60MPa\",\"seismicLevel\":\"VIII度\",\"sampleCount\":5,\"matchRate\":\"94.2%\"}",
                "LOW", "建议在石灰岩地层进行基础施工时注意岩溶发育情况，施工前应进行详细勘察",
                "张总工", "2026-06-17T16:00:00", "COMPLETED"),
            createAnalysis("RSA-2026-0616-001", 2L, "ZRS-2026-0616-001",
                "莲花镇地质雷达探测", "岳麓区莲花镇", 28.38921, 112.76543,
                "GEOPHYSICAL", "地质雷达", 0, 30.0, 4,
                "[{\"depth\":\"0-2m\",\"lithology\":\"耕植土\",\"thickness\":2.0},{\"depth\":\"2-10m\",\"lithology\":\"粉质黏土\",\"thickness\":8.0},{\"depth\":\"10-20m\",\"lithology\":\"强风化砂岩\",\"thickness\":10.0},{\"depth\":\"20-30m\",\"lithology\":\"中风化砂岩\",\"thickness\":10.0}]",
                "[{\"name\":\"耕植土层\",\"depth\":\"0-2m\",\"thickness\":2.0},{\"name\":\"粉质黏土层\",\"depth\":\"2-10m\",\"thickness\":8.0},{\"name\":\"强风化砂岩层\",\"depth\":\"10-20m\",\"thickness\":10.0},{\"name\":\"中风化砂岩层\",\"depth\":\"20-30m\",\"thickness\":10.0}]",
                "{\"structure\":\"层状结构\",\"dip\":\"10°\",\"dipDirection\":\"210°\"}",
                "CNN", 87.3,
                "地质雷达探测结果显示：该区域存在一条小型正断层（F1），位于地表下15-20米处，断距约2米。岩层整体为砂岩地层，风化程度随深度增加而降低。建议对断层附近区域进行重点监测。",
                "{\"gprFrequency\":\"500MHz\",\"penetrationDepth\":\"30m\",\"resolution\":\"0.5m\"}",
                "MEDIUM", "建议对F1断层附近区域进行加密监测，防止不均匀沉降",
                "李工", "2026-06-16T14:00:00", "COMPLETED")
        };
        for (RockStratumAnalysis analysis : analyses) {
            analysisMapper.insert(analysis);
        }
        log.info("岩层分析Mock数据初始化完成，共 {} 条", analyses.length);
    }

    private RockStratumAnalysis createAnalysis(String code, Long missionId, String missionCode,
                                                String projectName, String location, double lat, double lng,
                                                String type, String dataSource, int boreholeCount, double maxDepth, int stratumCount,
                                                String stratumData, String lithologyData, String structureData,
                                                String aiAlgorithm, double aiConfidence, String aiSummary, String aiDetail,
                                                String riskLevel, String suggestion,
                                                String analyst, String analysisTime, String status) {
        RockStratumAnalysis a = new RockStratumAnalysis();
        a.setAnalysisCode(code);
        a.setMissionId(missionId);
        a.setMissionCode(missionCode);
        a.setProjectName(projectName);
        a.setLocation(location);
        a.setLatitude(lat);
        a.setLongitude(lng);
        a.setAnalysisType(type);
        a.setDataSource(dataSource);
        a.setBoreholeCount(boreholeCount);
        a.setMaxDepth(maxDepth);
        a.setStratumCount(stratumCount);
        a.setStratumData(stratumData);
        a.setLithologyData(lithologyData);
        a.setStructureData(structureData);
        a.setAiAlgorithm(aiAlgorithm);
        a.setAiConfidence(aiConfidence);
        a.setAiSummary(aiSummary);
        a.setAiDetail(aiDetail);
        a.setRiskLevel(riskLevel);
        a.setSuggestion(suggestion);
        a.setAnalyst(analyst);
        a.setAnalysisTime(LocalDateTime.parse(analysisTime));
        a.setStatus(status);
        a.setIsDeleted(0);
        return a;
    }
}
