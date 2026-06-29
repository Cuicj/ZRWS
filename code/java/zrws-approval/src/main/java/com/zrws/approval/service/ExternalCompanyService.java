package com.zrws.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zrws.approval.domain.entity.ExternalCompany;
import com.zrws.approval.mapper.ExternalCompanyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ExternalCompanyService {

    @Autowired
    private ExternalCompanyMapper externalCompanyMapper;

    public List<ExternalCompany> listAll() {
        QueryWrapper<ExternalCompany> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_time");
        return externalCompanyMapper.selectList(wrapper);
    }

    public ExternalCompany create(ExternalCompany company) {
        company.setCreatedTime(LocalDateTime.now());
        company.setUpdatedTime(LocalDateTime.now());
        if (company.getStatus() == null) {
            company.setStatus(ExternalCompany.Status.ACTIVE.getValue());
        }
        externalCompanyMapper.insert(company);
        return company;
    }

    public void update(ExternalCompany company) {
        company.setUpdatedTime(LocalDateTime.now());
        externalCompanyMapper.updateById(company);
    }

    public void delete(Long companyId) {
        externalCompanyMapper.deleteById(companyId);
    }

    public ExternalCompany getById(Long companyId) {
        return externalCompanyMapper.selectById(companyId);
    }
}
