package com.zrws.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.SoilClassification;
import com.zrws.approval.domain.entity.SoilSample;
import com.zrws.approval.mapper.SoilClassificationMapper;
import com.zrws.approval.mapper.SoilSampleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SoilClassificationService {

    @Autowired
    private SoilClassificationMapper soilClassificationMapper;

    @Autowired
    private SoilSampleMapper soilSampleMapper;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public Page<SoilClassification> getPage(int pageNum, int pageSize, Long missionId, String soilType, String status) {
        LambdaQueryWrapper<SoilClassification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SoilClassification::getIsDeleted, 0);
        if (missionId != null) {
            wrapper.eq(SoilClassification::getMissionId, missionId);
        }
        if (StringUtils.hasText(soilType)) {
            wrapper.eq(SoilClassification::getSoilType, soilType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SoilClassification::getStatus, status);
        }
        wrapper.orderByDesc(SoilClassification::getAnalysisTime);
        return soilClassificationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public SoilClassification getById(Long id) {
        return soilClassificationMapper.selectById(id);
    }

    public List<SoilClassification> getHistory(Long missionId) {
        LambdaQueryWrapper<SoilClassification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SoilClassification::getIsDeleted, 0);
        if (missionId != null) {
            wrapper.eq(SoilClassification::getMissionId, missionId);
        }
        wrapper.orderByDesc(SoilClassification::getAnalysisTime);
        return soilClassificationMapper.selectList(wrapper);
    }

    public String generateAnalysisCode() {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        long count = soilClassificationMapper.selectCount(
                new LambdaQueryWrapper<SoilClassification>()
                        .likeRight(SoilClassification::getAnalysisCode, "SC" + dateStr)
        );
        return String.format("SC%s%04d", dateStr, count + 1);
    }

    public SoilClassification analyzeBySampleId(Long sampleId) {
        SoilSample sample = soilSampleMapper.selectById(sampleId);
        if (sample == null) {
            throw new RuntimeException("采样记录不存在: " + sampleId);
        }
        return performAnalysis(sample);
    }

    public SoilClassification analyzeByParams(Map<String, Object> params) {
        SoilClassification classification = new SoilClassification();
        classification.setAnalysisCode(generateAnalysisCode());
        classification.setAnalysisName("土壤分类分析");
        classification.setAnalysisTime(LocalDateTime.now());
        classification.setAnalyst("系统自动分析");
        classification.setIsDeleted(0);
        classification.setStatus("COMPLETED");

        if (params.containsKey("missionId")) {
            classification.setMissionId(Long.valueOf(params.get("missionId").toString()));
        }
        if (params.containsKey("phValue")) {
            classification.setPhValue(Double.valueOf(params.get("phValue").toString()));
        }
        if (params.containsKey("organicMatter")) {
            classification.setOrganicMatter(Double.valueOf(params.get("organicMatter").toString()));
        }
        if (params.containsKey("moisture")) {
            classification.setMoisture(Double.valueOf(params.get("moisture").toString()));
        }
        if (params.containsKey("nitrogen")) {
            classification.setNitrogen(Double.valueOf(params.get("nitrogen").toString()));
        }
        if (params.containsKey("phosphorus")) {
            classification.setPhosphorus(Double.valueOf(params.get("phosphorus").toString()));
        }
        if (params.containsKey("potassium")) {
            classification.setPotassium(Double.valueOf(params.get("potassium").toString()));
        }
        if (params.containsKey("texture")) {
            classification.setTexture(params.get("texture").toString());
        }
        if (params.containsKey("color")) {
            classification.setColor(params.get("color").toString());
        }
        if (params.containsKey("depth")) {
            classification.setDepth(Double.valueOf(params.get("depth").toString()));
        }

        performClassification(classification);
        soilClassificationMapper.insert(classification);
        return classification;
    }

    private SoilClassification performAnalysis(SoilSample sample) {
        SoilClassification classification = new SoilClassification();
        classification.setAnalysisCode(generateAnalysisCode());
        classification.setAnalysisName("土壤分类分析 - 样本" + sample.getSampleCode());
        classification.setMissionId(sample.getMissionId());
        classification.setMissionCode(sample.getMissionCode());
        classification.setAnalysisTime(LocalDateTime.now());
        classification.setAnalyst("系统自动分析");
        classification.setIsDeleted(0);
        classification.setStatus("COMPLETED");

        classification.setPhValue(sample.getPhValue());
        classification.setOrganicMatter(sample.getOrganicMatter());
        classification.setMoisture(sample.getMoisture());
        classification.setNitrogen(sample.getNitrogen());
        classification.setPhosphorus(sample.getPhosphorus());
        classification.setPotassium(sample.getPotassium());
        if (sample.getSoilTexture() != null) {
            classification.setTexture(sample.getSoilTexture());
        }

        performClassification(classification);

        classification.setSampleCount(1);
        soilClassificationMapper.insert(classification);
        return classification;
    }

    private void performClassification(SoilClassification classification) {
        String soilType = determineSoilType(classification);
        classification.setSoilType(soilType);

        String soilSubtype = determineSoilSubtype(classification);
        classification.setSoilSubtype(soilSubtype);

        double confidence = calculateConfidence(classification);
        classification.setConfidence(confidence);

        String description = generateDescription(classification);
        classification.setDescription(description);

        String aiSuggestion = generateAiSuggestion(classification);
        classification.setAiSuggestion(aiSuggestion);
    }

    private String determineSoilType(SoilClassification classification) {
        Double ph = classification.getPhValue();
        Double organic = classification.getOrganicMatter();
        String texture = classification.getTexture();

        if (ph != null && ph < 5.5) {
            if (organic != null && organic > 2.0) {
                return "红壤";
            }
            return "红壤";
        } else if (ph != null && ph >= 5.5 && ph < 6.5) {
            return "黄棕壤";
        } else if (ph != null && ph >= 6.5 && ph < 7.5) {
            if (texture != null && texture.contains("黏")) {
                return "水稻土";
            }
            return "菜园土";
        } else {
            return "冲积土";
        }
    }

    private String determineSoilSubtype(SoilClassification classification) {
        String mainType = classification.getSoilType();
        Double nitrogen = classification.getNitrogen();
        Double phosphorus = classification.getPhosphorus();
        Double potassium = classification.getPotassium();

        if (nitrogen != null && nitrogen > 1.5 && phosphorus != null && phosphorus > 20) {
            return mainType + "（高肥力型）";
        } else if (nitrogen != null && nitrogen < 0.5) {
            return mainType + "（低氮型）";
        }
        return mainType + "（普通型）";
    }

    private double calculateConfidence(SoilClassification classification) {
        double confidence = 0.5;
        if (classification.getPhValue() != null) confidence += 0.15;
        if (classification.getOrganicMatter() != null) confidence += 0.1;
        if (classification.getMoisture() != null) confidence += 0.05;
        if (classification.getNitrogen() != null) confidence += 0.05;
        if (classification.getPhosphorus() != null) confidence += 0.05;
        if (classification.getPotassium() != null) confidence += 0.05;
        if (classification.getTexture() != null) confidence += 0.05;
        return Math.min(confidence, 1.0);
    }

    private String generateDescription(SoilClassification classification) {
        StringBuilder sb = new StringBuilder();
        sb.append("该土壤类型为").append(classification.getSoilType()).append("。");
        if (classification.getPhValue() != null) {
            sb.append("pH值为").append(String.format("%.2f", classification.getPhValue())).append("，");
            if (classification.getPhValue() < 5.5) {
                sb.append("呈酸性；");
            } else if (classification.getPhValue() > 7.5) {
                sb.append("呈碱性；");
            } else {
                sb.append("呈中性；");
            }
        }
        if (classification.getOrganicMatter() != null) {
            sb.append("有机质含量").append(String.format("%.2f", classification.getOrganicMatter())).append("%；");
        }
        return sb.toString();
    }

    private String generateAiSuggestion(SoilClassification classification) {
        StringBuilder sb = new StringBuilder();
        sb.append("根据分析结果，建议：");

        Double ph = classification.getPhValue();
        if (ph != null) {
            if (ph < 5.5) {
                sb.append("土壤偏酸，建议施用石灰或碱性肥料调节pH值；");
            } else if (ph > 7.5) {
                sb.append("土壤偏碱，建议施用硫磺或酸性肥料调节pH值；");
            }
        }

        Double organic = classification.getOrganicMatter();
        if (organic != null && organic < 1.5) {
            sb.append("有机质含量偏低，建议增施有机肥；");
        }

        Double nitrogen = classification.getNitrogen();
        if (nitrogen != null && nitrogen < 0.5) {
            sb.append("氮含量不足，建议适当追施氮肥；");
        }

        return sb.toString();
    }

    public void delete(Long id) {
        soilClassificationMapper.deleteById(id);
    }
}