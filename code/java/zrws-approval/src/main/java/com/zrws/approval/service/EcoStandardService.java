package com.zrws.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zrws.approval.domain.entity.EcoStandard;
import com.zrws.approval.mapper.EcoStandardMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class EcoStandardService {

    @Autowired
    private EcoStandardMapper ecoStandardMapper;

    public Page<EcoStandard> getPage(int pageNum, int pageSize, String category, String subcategory, String gradeLevel, String keyword) {
        LambdaQueryWrapper<EcoStandard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcoStandard::getIsDeleted, 0);
        if (StringUtils.hasText(category)) {
            wrapper.eq(EcoStandard::getCategory, category);
        }
        if (StringUtils.hasText(subcategory)) {
            wrapper.eq(EcoStandard::getSubcategory, subcategory);
        }
        if (StringUtils.hasText(gradeLevel)) {
            wrapper.eq(EcoStandard::getGradeLevel, gradeLevel);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(EcoStandard::getStandardName, keyword)
                    .or().like(EcoStandard::getStandardCode, keyword);
        }
        wrapper.orderByAsc(EcoStandard::getSortOrder);
        return ecoStandardMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public List<EcoStandard> getByCategory(String category) {
        return ecoStandardMapper.selectByCategory(category);
    }

    public List<EcoStandard> getByCategoryAndGrade(String category, String gradeLevel) {
        return ecoStandardMapper.selectByCategoryAndGrade(category, gradeLevel);
    }

    public EcoStandard getById(Long id) {
        return ecoStandardMapper.selectById(id);
    }

    public EcoStandard getByCode(String code) {
        LambdaQueryWrapper<EcoStandard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EcoStandard::getStandardCode, code)
                .eq(EcoStandard::getIsDeleted, 0);
        return ecoStandardMapper.selectOne(wrapper);
    }

    public boolean existsByCode(String code) {
        return ecoStandardMapper.countByCode(code) > 0;
    }

    public void add(EcoStandard ecoStandard) {
        ecoStandardMapper.insert(ecoStandard);
    }

    public void update(EcoStandard ecoStandard) {
        ecoStandardMapper.updateById(ecoStandard);
    }

    public void delete(Long id) {
        ecoStandardMapper.deleteById(id);
    }
}
