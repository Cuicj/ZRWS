package com.zrws.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zrws.approval.domain.entity.GeoStandard;
import com.zrws.approval.mapper.GeoStandardMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 地质标准数据库 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/geo-standards")
@CrossOrigin(origins = "*")
public class GeoStandardController {

    @Autowired
    private GeoStandardMapper geoStandardMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String system,
            @RequestParam(required = false) String keyword) {
        try {
            LambdaQueryWrapper<GeoStandard> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GeoStandard::getStatus, "ACTIVE");
            wrapper.eq(GeoStandard::getIsDeleted, 0);
            if (category != null && !category.isEmpty()) {
                wrapper.eq(GeoStandard::getCategory, category);
            }
            if (system != null && !system.isEmpty()) {
                wrapper.eq(GeoStandard::getClassificationSystem, system);
            }
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.like(GeoStandard::getStandardName, keyword);
            }
            wrapper.orderByAsc(GeoStandard::getSortOrder);
            List<GeoStandard> list = geoStandardMapper.selectList(wrapper);
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", list.size());
            return success(result);
        } catch (Exception e) {
            log.error("查询地质标准失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        try {
            GeoStandard standard = geoStandardMapper.selectById(id);
            if (standard == null) {
                return error("标准不存在");
            }
            return success(Collections.singletonMap("data", standard));
        } catch (Exception e) {
            log.error("查询地质标准详情失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getByCategory(@PathVariable String category) {
        try {
            List<GeoStandard> list = geoStandardMapper.selectByCategory(category);
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", list.size());
            return success(result);
        } catch (Exception e) {
            log.error("按分类查询地质标准失败", e);
            return error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> search(@RequestParam String keyword) {
        try {
            List<GeoStandard> list = geoStandardMapper.searchByName(keyword);
            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", list.size());
            return success(result);
        } catch (Exception e) {
            log.error("搜索地质标准失败", e);
            return error("搜索失败: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody GeoStandard standard) {
        try {
            standard.setStatus("ACTIVE");
            standard.setIsDeleted(0);
            geoStandardMapper.insert(standard);
            Map<String, Object> result = new HashMap<>();
            result.put("data", standard);
            result.put("message", "创建成功");
            return success(result);
        } catch (Exception e) {
            log.error("创建地质标准失败", e);
            return error("创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody GeoStandard standard) {
        try {
            standard.setStandardId(id);
            geoStandardMapper.updateById(standard);
            return success(Collections.singletonMap("message", "更新成功"));
        } catch (Exception e) {
            log.error("更新地质标准失败", e);
            return error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            geoStandardMapper.deleteById(id);
            return success(Collections.singletonMap("message", "删除成功"));
        } catch (Exception e) {
            log.error("删除地质标准失败", e);
            return error("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/ai/classify")
    public ResponseEntity<Map<String, Object>> aiClassify(@RequestBody SoilClassifyRequest request) {
        try {
            List<GeoStandard> standards = geoStandardMapper.selectList(
                    new LambdaQueryWrapper<GeoStandard>()
                            .eq(GeoStandard::getStatus, "ACTIVE")
                            .eq(GeoStandard::getIsDeleted, 0)
                            .in(GeoStandard::getCategory,
                                    GeoStandard.Category.SOIL_CHINA.name(),
                                    GeoStandard.Category.SOIL_WRB.name()));

            List<SoilClassifyResult> results = new ArrayList<>();
            for (GeoStandard standard : standards) {
                SoilClassifyResult result = calculateSimilarity(request, standard);
                results.add(result);
            }

            results.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));

            List<SoilClassifyResult> topResults = results.stream()
                    .limit(10)
                    .collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("list", topResults);
            data.put("total", topResults.size());
            return success(data);
        } catch (Exception e) {
            log.error("AI土壤分类比对失败", e);
            return error("分类比对失败: " + e.getMessage());
        }
    }

    @PostMapping("/ai/compare")
    public ResponseEntity<Map<String, Object>> aiBatchCompare(@RequestBody List<SoilClassifyRequest> requests) {
        try {
            List<GeoStandard> standards = geoStandardMapper.selectList(
                    new LambdaQueryWrapper<GeoStandard>()
                            .eq(GeoStandard::getStatus, "ACTIVE")
                            .eq(GeoStandard::getIsDeleted, 0)
                            .in(GeoStandard::getCategory,
                                    GeoStandard.Category.SOIL_CHINA.name(),
                                    GeoStandard.Category.SOIL_WRB.name()));

            List<BatchCompareResult> batchResults = new ArrayList<>();
            for (int i = 0; i < requests.size(); i++) {
                SoilClassifyRequest request = requests.get(i);
                List<SoilClassifyResult> results = new ArrayList<>();
                for (GeoStandard standard : standards) {
                    SoilClassifyResult result = calculateSimilarity(request, standard);
                    results.add(result);
                }
                results.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));

                BatchCompareResult batchResult = new BatchCompareResult();
                batchResult.setIndex(i);
                batchResult.setSampleId(request.getSampleId());
                batchResult.setMatches(results.stream().limit(5).collect(Collectors.toList()));
                batchResult.setTopMatch(results.isEmpty() ? null : results.get(0));
                batchResults.add(batchResult);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("results", batchResults);
            data.put("total", batchResults.size());
            return success(data);
        } catch (Exception e) {
            log.error("AI批量比对失败", e);
            return error("批量比对失败: " + e.getMessage());
        }
    }

    @PostMapping("/ai/diagram/{id}")
    public ResponseEntity<Map<String, Object>> generateDiagram(@PathVariable Long id) {
        try {
            GeoStandard standard = geoStandardMapper.selectById(id);
            if (standard == null) {
                return error("标准不存在");
            }

            DiagramDescription diagram = new DiagramDescription();
            diagram.setStandardId(standard.getStandardId());
            diagram.setStandardName(standard.getStandardName());
            diagram.setDiagramType("soil_profile");

            StringBuilder description = new StringBuilder();
            description.append("【").append(standard.getStandardName()).append("】土壤剖面图示：\n\n");
            description.append("1. 土壤类型：").append(standard.getStandardName()).append("\n");
            description.append("2. 分类系统：").append(standard.getClassificationSystem() != null ?
                    standard.getClassificationSystem() : "未指定").append("\n");

            if (standard.getColorDescription() != null && !standard.getColorDescription().isEmpty()) {
                description.append("3. 颜色特征：").append(standard.getColorDescription()).append("\n");
            }
            if (standard.getTextureDescription() != null && !standard.getTextureDescription().isEmpty()) {
                description.append("4. 质地特征：").append(standard.getTextureDescription()).append("\n");
            }
            if (standard.getStructureDescription() != null && !standard.getStructureDescription().isEmpty()) {
                description.append("5. 结构特征：").append(standard.getStructureDescription()).append("\n");
            }
            if (standard.getParentMaterial() != null && !standard.getParentMaterial().isEmpty()) {
                description.append("6. 母质类型：").append(standard.getParentMaterial()).append("\n");
            }
            if (standard.getFormationEnvironment() != null && !standard.getFormationEnvironment().isEmpty()) {
                description.append("7. 形成环境：").append(standard.getFormationEnvironment()).append("\n");
            }
            if (standard.getDistribution() != null && !standard.getDistribution().isEmpty()) {
                description.append("8. 分布区域：").append(standard.getDistribution()).append("\n");
            }

            description.append("\n【土层结构】\n");
            description.append("- 淋溶层(A层)：腐殖质积累，颜色较暗\n");
            description.append("- 淀积层(B层)：物质淀积，结构明显\n");
            description.append("- 母质层(C层)：半风化岩石碎屑\n");
            description.append("- 母岩层(R层)：坚硬基岩\n");

            if (standard.getChemicalComposition() != null && !standard.getChemicalComposition().isEmpty()) {
                description.append("\n【化学性质】\n").append(standard.getChemicalComposition()).append("\n");
            }
            if (standard.getPhysicalProperties() != null && !standard.getPhysicalProperties().isEmpty()) {
                description.append("\n【物理性质】\n").append(standard.getPhysicalProperties()).append("\n");
            }

            diagram.setDescription(description.toString());
            diagram.setLayers(Arrays.asList(
                    createLayer("A层", "淋溶层", "0-30cm", "暗棕色", "粒状结构"),
                    createLayer("B层", "淀积层", "30-80cm", "棕色", "块状结构"),
                    createLayer("C层", "母质层", "80-150cm", "浅棕色", "半风化状"),
                    createLayer("R层", "母岩层", ">150cm", "灰色", "坚硬岩石")
            ));

            Map<String, Object> data = new HashMap<>();
            data.put("diagram", diagram);
            return success(data);
        } catch (Exception e) {
            log.error("生成标准图示描述失败", e);
            return error("生成图示失败: " + e.getMessage());
        }
    }

    private SoilClassifyResult calculateSimilarity(SoilClassifyRequest request, GeoStandard standard) {
        SoilClassifyResult result = new SoilClassifyResult();
        result.setStandardId(standard.getStandardId());
        result.setStandardName(standard.getStandardName());
        result.setStandardCode(standard.getStandardCode());
        result.setCategory(standard.getCategory());

        MatchDetails details = new MatchDetails();
        double totalScore = 0.0;
        double weightSum = 0.0;

        if (request.getPh() != null) {
            double phScore = calculatePhScore(request.getPh(), standard);
            details.setPhScore(phScore);
            details.setPhMatch(phScore >= 70);
            totalScore += phScore * 0.30;
            weightSum += 0.30;
        } else {
            details.setPhScore(0.0);
            details.setPhMatch(false);
        }

        if (request.getOrganicMatter() != null) {
            double omScore = calculateOrganicMatterScore(request.getOrganicMatter(), standard);
            details.setOrganicMatterScore(omScore);
            details.setOrganicMatterMatch(omScore >= 70);
            totalScore += omScore * 0.25;
            weightSum += 0.25;
        } else {
            details.setOrganicMatterScore(0.0);
            details.setOrganicMatterMatch(false);
        }

        if (request.getTexture() != null && !request.getTexture().isEmpty()) {
            double textureScore = calculateTextureScore(request.getTexture(), standard);
            details.setTextureScore(textureScore);
            details.setTextureMatch(textureScore >= 60);
            totalScore += textureScore * 0.25;
            weightSum += 0.25;
        } else {
            details.setTextureScore(0.0);
            details.setTextureMatch(false);
        }

        if (request.getColor() != null && !request.getColor().isEmpty()) {
            double colorScore = calculateColorScore(request.getColor(), standard);
            details.setColorScore(colorScore);
            details.setColorMatch(colorScore >= 80);
            totalScore += colorScore * 0.20;
            weightSum += 0.20;
        } else {
            details.setColorScore(0.0);
            details.setColorMatch(false);
        }

        if (request.getMoisture() != null) {
            double moistureScore = Math.max(0, 100 - Math.abs(request.getMoisture() - 25) * 2);
            details.setMoistureScore(moistureScore);
        } else {
            details.setMoistureScore(0.0);
        }

        double finalScore = weightSum > 0 ? (totalScore / weightSum) : 0.0;
        result.setConfidence(Math.round(finalScore * 100.0) / 100.0);
        result.setMatchDetails(details);

        return result;
    }

    private double calculatePhScore(Double phValue, GeoStandard standard) {
        String chemical = standard.getChemicalComposition();
        if (chemical == null || chemical.isEmpty()) {
            return 60.0;
        }

        double targetPh = 7.0;
        if (chemical.contains("pH")) {
            try {
                String phStr = chemical.replaceAll(".*pH[：: ]*([\\d.]+).*", "$1");
                if (!phStr.equals(chemical)) {
                    targetPh = Double.parseDouble(phStr);
                }
            } catch (Exception e) {
                targetPh = 7.0;
            }
        }

        double diff = Math.abs(phValue - targetPh);
        if (diff <= 0.5) {
            return 90 + (0.5 - diff) * 20;
        } else if (diff <= 1.0) {
            return 70 + (1.0 - diff) * 40;
        } else if (diff <= 2.0) {
            return 50 + (2.0 - diff) * 20;
        } else {
            return Math.max(0, 50 - (diff - 2.0) * 10);
        }
    }

    private double calculateOrganicMatterScore(Double omValue, GeoStandard standard) {
        String chemical = standard.getChemicalComposition();
        if (chemical == null || chemical.isEmpty()) {
            return 60.0;
        }

        double targetOm = 2.5;
        if (chemical.contains("有机质") || chemical.contains("有机碳")) {
            try {
                String omStr = chemical.replaceAll(".*有机质[：: ]*([\\d.]+).*", "$1");
                if (!omStr.equals(chemical)) {
                    targetOm = Double.parseDouble(omStr);
                }
            } catch (Exception e) {
                targetOm = 2.5;
            }
        }

        if (targetOm <= 0) return 60.0;
        double diffPercent = Math.abs(omValue - targetOm) / targetOm;
        if (diffPercent <= 0.1) {
            return 90 + (0.1 - diffPercent) * 100;
        } else if (diffPercent <= 0.3) {
            return 70 + (0.3 - diffPercent) * 100;
        } else if (diffPercent <= 0.5) {
            return 50 + (0.5 - diffPercent) * 100;
        } else {
            return Math.max(0, 50 - (diffPercent - 0.5) * 50);
        }
    }

    private double calculateTextureScore(String texture, GeoStandard standard) {
        String textureDesc = standard.getTextureDescription();
        if (textureDesc == null || textureDesc.isEmpty()) {
            return 50.0;
        }

        String textureLower = texture.toLowerCase();
        String descLower = textureDesc.toLowerCase();

        if (descLower.contains(textureLower) || textureLower.contains(descLower)) {
            return 100.0;
        }

        Map<String, List<String>> similarGroups = new HashMap<>();
        similarGroups.put("壤土", Arrays.asList("粉壤土", "砂壤土", "粘壤土", "壤质"));
        similarGroups.put("粘土", Arrays.asList("粘壤土", "重粘土", "壤质粘土"));
        similarGroups.put("砂土", Arrays.asList("砂壤土", "壤质砂土", "粗砂土"));
        similarGroups.put("粉土", Arrays.asList("粉壤土", "壤质粉土", "粉质"));

        for (Map.Entry<String, List<String>> entry : similarGroups.entrySet()) {
            if (descLower.contains(entry.getKey())) {
                for (String similar : entry.getValue()) {
                    if (textureLower.contains(similar) || similar.contains(textureLower)) {
                        return 60.0;
                    }
                }
            }
        }

        return 30.0;
    }

    private double calculateColorScore(String color, GeoStandard standard) {
        String colorDesc = standard.getColorDescription();
        if (colorDesc == null || colorDesc.isEmpty()) {
            return 50.0;
        }

        String colorLower = color.toLowerCase();
        String descLower = colorDesc.toLowerCase();

        if (descLower.contains(colorLower) || colorLower.contains(descLower)) {
            return 100.0;
        }

        String[] colorFamilies = {"棕", "红", "黄", "黑", "灰", "白"};
        for (String family : colorFamilies) {
            if (descLower.contains(family) && colorLower.contains(family)) {
                return 70.0;
            }
        }

        return 30.0;
    }

    private DiagramDescription.SoilLayer createLayer(String code, String name, String depth,
                                                      String color, String structure) {
        DiagramDescription.SoilLayer layer = new DiagramDescription.SoilLayer();
        layer.setLayerCode(code);
        layer.setLayerName(name);
        layer.setDepthRange(depth);
        layer.setColor(color);
        layer.setStructure(structure);
        return layer;
    }

    @Data
    public static class SoilClassifyRequest {
        private String sampleId;
        private Double ph;
        private Double organicMatter;
        private Double moisture;
        private String texture;
        private String color;
        private Double nitrogen;
        private Double phosphorus;
        private Double potassium;
    }

    @Data
    public static class SoilClassifyResult {
        private Long standardId;
        private String standardName;
        private String standardCode;
        private String category;
        private Double confidence;
        private MatchDetails matchDetails;
    }

    @Data
    public static class MatchDetails {
        private Double phScore;
        private Boolean phMatch;
        private Double organicMatterScore;
        private Boolean organicMatterMatch;
        private Double textureScore;
        private Boolean textureMatch;
        private Double colorScore;
        private Boolean colorMatch;
        private Double moistureScore;
    }

    @Data
    public static class BatchCompareResult {
        private Integer index;
        private String sampleId;
        private List<SoilClassifyResult> matches;
        private SoilClassifyResult topMatch;
    }

    @Data
    public static class DiagramDescription {
        private Long standardId;
        private String standardName;
        private String diagramType;
        private String description;
        private List<SoilLayer> layers;

        @Data
        public static class SoilLayer {
            private String layerCode;
            private String layerName;
            private String depthRange;
            private String color;
            private String structure;
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
