package com.zrws.approval.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户/角色/组织管理 API
 * <p>所有接口需要JWT认证，租户隔离由MyBatis-Plus拦截器自动处理
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/management")
public class ManagementController {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private OrganizationMapper organizationMapper;
    @Autowired
    private UserOrgMapper userOrgMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public Map<String, Object> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword)
                    .or().like(SysUser::getPhone, keyword);
        }
        wrapper.orderByDesc(SysUser::getCreatedTime);

        Page<SysUser> result = sysUserMapper.selectPage(new Page<>(page, size), wrapper);
        // 清除密码
        result.getRecords().forEach(u -> u.setPassword(null));

        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("total", result.getTotal());
        response.put("page", page);
        response.put("size", size);
        return response;
    }

    @PostMapping("/users")
    public Map<String, Object> createUser(@RequestBody Map<String, Object> body) {
        SysUser user = new SysUser();
        user.setUsername((String) body.get("username"));
        user.setPassword(passwordEncoder.encode((String) body.get("password")));
        user.setPhone((String) body.get("phone"));
        user.setEmail((String) body.get("email"));
        user.setRealName((String) body.get("realName"));
        user.setStatus(SysUser.Status.ACTIVE.name());
        // 使用当前用户的组织和租户
        user.setCurrentOrgId(body.get("orgId") != null ? Long.valueOf(body.get("orgId").toString()) : null);
        user.setTenantId(body.get("tenantId") != null ? Long.valueOf(body.get("tenantId").toString()) : null);
        sysUserMapper.insert(user);

        // 如果指定了角色，创建用户-组织关联
        if (body.get("orgId") != null && body.get("roleId") != null) {
            UserOrg userOrg = new UserOrg();
            userOrg.setUserId(user.getId());
            userOrg.setOrgId(Long.valueOf(body.get("orgId").toString()));
            userOrg.setRoleId(Long.valueOf(body.get("roleId").toString()));
            userOrg.setStatus(UserOrg.Status.ACTIVE.name());
            userOrg.setJoinTime(LocalDateTime.now());
            userOrg.setTenantId(user.getTenantId());
            userOrgMapper.insert(userOrg);
        }

        user.setPassword(null);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", user);
        return response;
    }

    @PutMapping("/users/{id}")
    public Map<String, Object> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (body.containsKey("realName")) user.setRealName((String) body.get("realName"));
        if (body.containsKey("phone")) user.setPhone((String) body.get("phone"));
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        if (body.containsKey("status")) user.setStatus((String) body.get("status"));
        if (body.containsKey("avatar")) user.setAvatar((String) body.get("avatar"));
        sysUserMapper.updateById(user);

        user.setPassword(null);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", user);
        return response;
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        sysUserMapper.deleteById(id);
        // 删除用户-组织关联
        userOrgMapper.delete(new LambdaQueryWrapper<UserOrg>().eq(UserOrg::getUserId, id));
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    @PutMapping("/users/{id}/password")
    public Map<String, Object> resetPassword(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setPassword(passwordEncoder.encode((String) body.get("password")));
        sysUserMapper.updateById(user);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    @GetMapping("/users/{id}/roles")
    public Map<String, Object> getUserRoles(@PathVariable Long id) {
        List<UserOrg> userOrgs = userOrgMapper.selectList(
                new LambdaQueryWrapper<UserOrg>().eq(UserOrg::getUserId, id));
        List<Map<String, Object>> roles = new ArrayList<>();
        for (UserOrg uo : userOrgs) {
            SysRole role = sysRoleMapper.selectById(uo.getRoleId());
            if (role != null) {
                Map<String, Object> r = new HashMap<>();
                r.put("roleId", role.getId());
                r.put("roleName", role.getRoleName());
                r.put("roleCode", role.getRoleCode());
                r.put("orgId", uo.getOrgId());
                roles.add(r);
            }
        }
        Map<String, Object> response = new HashMap<>();
        response.put("roles", roles);
        return response;
    }

    // ==================== 角色管理 ====================

    @GetMapping("/roles")
    public Map<String, Object> listRoles() {
        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByDesc(SysRole::getCreatedTime));
        Map<String, Object> response = new HashMap<>();
        response.put("list", roles);
        response.put("total", roles.size());
        return response;
    }

    @PostMapping("/roles")
    public Map<String, Object> createRole(@RequestBody Map<String, Object> body) {
        SysRole role = new SysRole();
        role.setRoleName((String) body.get("roleName"));
        role.setRoleCode((String) body.get("roleCode"));
        role.setRoleType(body.get("roleType") != null ? (String) body.get("roleType") : SysRole.RoleType.CUSTOM.name());
        role.setDataScope(body.get("dataScope") != null ? (String) body.get("dataScope") : SysRole.DataScope.ALL.name());
        role.setStatus(SysRole.Status.ACTIVE.name());
        if (body.get("permissions") != null) {
            role.setPermissions(body.get("permissions").toString());
        }
        sysRoleMapper.insert(role);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("role", role);
        return response;
    }

    @PutMapping("/roles/{id}")
    public Map<String, Object> updateRole(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new IllegalArgumentException("角色不存在");
        }
        if (body.containsKey("roleName")) role.setRoleName((String) body.get("roleName"));
        if (body.containsKey("roleCode")) role.setRoleCode((String) body.get("roleCode"));
        if (body.containsKey("dataScope")) role.setDataScope((String) body.get("dataScope"));
        if (body.containsKey("status")) role.setStatus((String) body.get("status"));
        if (body.containsKey("permissions")) role.setPermissions(body.get("permissions").toString());
        sysRoleMapper.updateById(role);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("role", role);
        return response;
    }

    @DeleteMapping("/roles/{id}")
    public Map<String, Object> deleteRole(@PathVariable Long id) {
        sysRoleMapper.deleteById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    // ==================== 组织管理 ====================

    @GetMapping("/orgs")
    public Map<String, Object> listOrgs() {
        List<Organization> orgs = organizationMapper.selectList(null);
        Map<String, Object> response = new HashMap<>();
        response.put("list", orgs);
        response.put("total", orgs.size());
        return response;
    }

    @GetMapping("/orgs/{id}")
    public Map<String, Object> getOrg(@PathVariable Long id) {
        Organization org = organizationMapper.selectById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("org", org);
        return response;
    }

    @PutMapping("/orgs/{id}")
    public Map<String, Object> updateOrg(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Organization org = organizationMapper.selectById(id);
        if (org == null) {
            throw new IllegalArgumentException("组织不存在");
        }
        if (body.containsKey("orgName")) org.setOrgName((String) body.get("orgName"));
        if (body.containsKey("orgType")) org.setOrgType((String) body.get("orgType"));
        if (body.containsKey("subscriptionLevel")) org.setSubscriptionLevel((String) body.get("subscriptionLevel"));
        if (body.containsKey("maxMembers")) org.setMaxMembers((Integer) body.get("maxMembers"));
        if (body.containsKey("status")) org.setStatus((String) body.get("status"));
        organizationMapper.updateById(org);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("org", org);
        return response;
    }

    // ==================== 当前用户信息 ====================

    @GetMapping("/current")
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        // 查询用户组织
        if (user != null && user.getCurrentOrgId() != null) {
            Organization org = organizationMapper.selectById(user.getCurrentOrgId());
            response.put("org", org);
            // 查询用户角色
            List<UserOrg> userOrgs = userOrgMapper.selectList(
                    new LambdaQueryWrapper<UserOrg>().eq(UserOrg::getUserId, userId));
            List<SysRole> roles = new ArrayList<>();
            for (UserOrg uo : userOrgs) {
                SysRole role = sysRoleMapper.selectById(uo.getRoleId());
                if (role != null) roles.add(role);
            }
            response.put("roles", roles);
        }
        return response;
    }
}
