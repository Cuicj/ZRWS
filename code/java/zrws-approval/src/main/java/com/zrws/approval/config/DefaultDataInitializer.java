package com.zrws.approval.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.Organization;
import com.zrws.approval.domain.entity.SysRole;
import com.zrws.approval.domain.entity.SysUser;
import com.zrws.approval.domain.entity.UserOrg;
import com.zrws.approval.mapper.OrganizationMapper;
import com.zrws.approval.mapper.SysRoleMapper;
import com.zrws.approval.mapper.SysUserMapper;
import com.zrws.approval.mapper.UserOrgMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 默认数据初始化器
 * <p>启动时创建默认组织、管理员用户和系统角色
 */
@Slf4j
@Component
@Order(1)
public class DefaultDataInitializer implements ApplicationRunner {

    @Autowired
    private OrganizationMapper organizationMapper;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private UserOrgMapper userOrgMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        initDefaultOrgAndAdmin();
    }

    private void initDefaultOrgAndAdmin() {
        // 检查是否已存在admin用户
        Long userCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin"));
        if (userCount != null && userCount > 0) {
            log.info("默认管理员已存在，跳过初始化");
            return;
        }

        log.info("开始初始化默认组织和管理员...");

        // 1. 创建默认组织
        Organization org = new Organization();
        org.setOrgName("智壤卫士默认组织");
        org.setOrgType("ENTERPRISE");
        org.setSubscriptionLevel("PRO");
        org.setMaxMembers(100);
        org.setStatus("ACTIVE");
        organizationMapper.insert(org);
        org.setTenantId(org.getId());
        organizationMapper.updateById(org);

        // 2. 创建管理员用户
        SysUser admin = new SysUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setPhone("13800000000");
        admin.setRealName("系统管理员");
        admin.setStatus(SysUser.Status.ACTIVE.name());
        admin.setCurrentOrgId(org.getId());
        admin.setTenantId(org.getId());
        sysUserMapper.insert(admin);

        // 3. 创建系统管理员角色
        SysRole role = new SysRole();
        role.setRoleName("系统管理员");
        role.setRoleCode("SYS_ADMIN");
        role.setRoleType(SysRole.RoleType.SYSTEM.name());
        role.setDataScope(SysRole.DataScope.ALL.name());
        role.setStatus(SysRole.Status.ACTIVE.name());
        role.setTenantId(org.getId());
        sysRoleMapper.insert(role);

        // 4. 创建用户-组织关联
        UserOrg userOrg = new UserOrg();
        userOrg.setUserId(admin.getId());
        userOrg.setOrgId(org.getId());
        userOrg.setRoleId(role.getId());
        userOrg.setStatus(UserOrg.Status.ACTIVE.name());
        userOrg.setJoinTime(LocalDateTime.now());
        userOrg.setTenantId(org.getId());
        userOrgMapper.insert(userOrg);

        log.info("默认组织和管理员初始化完成: orgId={}, adminId={}, roleId={}",
                org.getId(), admin.getId(), role.getId());
    }
}
