package com.zrws.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.SoilSample;
import com.zrws.approval.mapper.SoilSampleMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class SoilSampleService {

    @Autowired
    private SoilSampleMapper soilSampleMapper;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public Page<SoilSample> getPage(int pageNum, int pageSize, Long missionId, String soilType, String status) {
        LambdaQueryWrapper<SoilSample> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SoilSample::getIsDeleted, 0);
        if (missionId != null) {
            wrapper.eq(SoilSample::getMissionId, missionId);
        }
        if (StringUtils.hasText(soilType)) {
            wrapper.eq(SoilSample::getSoilType, soilType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(SoilSample::getStatus, status);
        }
        wrapper.orderByDesc(SoilSample::getCollectTime);
        return soilSampleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public SoilSample getById(Long id) {
        return soilSampleMapper.selectById(id);
    }

    public String generateSampleCode() {
        String dateStr = LocalDate.now().format(DATE_FORMAT);
        long count = soilSampleMapper.selectCount(
                new LambdaQueryWrapper<SoilSample>()
                        .likeRight(SoilSample::getSampleCode, "SS" + dateStr)
        );
        return String.format("SS%s%04d", dateStr, count + 1);
    }

    public void add(SoilSample soilSample) {
        if (!StringUtils.hasText(soilSample.getSampleCode())) {
            soilSample.setSampleCode(generateSampleCode());
        }
        if (soilSample.getCollectTime() == null) {
            soilSample.setCollectTime(LocalDateTime.now());
        }
        soilSampleMapper.insert(soilSample);
    }

    public void update(SoilSample soilSample) {
        soilSampleMapper.updateById(soilSample);
    }

    public void delete(Long id) {
        soilSampleMapper.deleteById(id);
    }
}