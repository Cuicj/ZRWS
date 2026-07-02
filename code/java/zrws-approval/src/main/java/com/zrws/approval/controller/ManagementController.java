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
import com.zrws.common.core.domain.R;
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
    public R<Map<String, Object>> listUsers(
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
        result.getRecords().forEach(u -> u.setPassword(null));

        Map<String, Object> response = new HashMap<>();
        response.put("list", result.getRecords());
        response.put("total", result.getTotal());
        response.put("page", page);
        response.put("size", size);
        return R.ok(response);
    }

    @PostMapping("/users")
    public R<SysUser> createUser(@RequestBody Map<String, Object> body) {
        SysUser user = new SysUser();
        user.setUsername((String) body.get("username"));
        user.setPassword(passwordEncoder.encode((String) body.get("password")));
        user.setPhone((String) body.get("phone"));
        user.setEmail((String) body.get("email"));
        user.setRealName((String) body.get("realName"));
        user.setStatus(SysUser.Status.ACTIVE.name());
        user.setCurrentOrgId(body.get("orgId") != null ? Long.valueOf(body.get("orgId").toString()) : null);
        user.setTenantId(body.get("tenantId") != null ? Long.valueOf(body.get("tenantId").toString()) : null);
        sysUserMapper.insert(user);

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
        return R.ok(user);
    }

    @PutMapping("/users/{id}")
    public R<SysUser> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return R.fail("用户不存在");
        }
        if (body.containsKey("realName")) user.setRealName((String) body.get("realName"));
        if (body.containsKey("phone")) user.setPhone((String) body.get("phone"));
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        if (body.containsKey("status")) user.setStatus((String) body.get("status"));
        if (body.containsKey("avatar")) user.setAvatar((String) body.get("avatar"));
        sysUserMapper.updateById(user);

        user.setPassword(null);
        return R.ok(user);
    }

    @DeleteMapping("/users/{id}")
    public R<String> deleteUser(@PathVariable Long id) {
        sysUserMapper.deleteById(id);
        userOrgMapper.delete(new LambdaQueryWrapper<UserOrg>().eq(UserOrg::getUserId, id));
        return R.ok("删除成功");
    }

    @PutMapping("/users/{id}/password")
    public R<String> resetPassword(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return R.fail("用户不存在");
        }
        user.setPassword(passwordEncoder.encode((String) body.get("password")));
        sysUserMapper.updateById(user);
        return R.ok("重置密码成功");
    }

    @GetMapping("/users/{id}/roles")
    public R<List<Map<String, Object>>> getUserRoles(@PathVariable Long id) {
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
        return R.ok(roles);
    }

    // ==================== 角色管理 ====================

    @GetMapping("/roles")
    public R<List<SysRole>> listRoles() {
        List<SysRole> roles = sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>().orderByDesc(SysRole::getCreatedTime));
        return R.ok(roles);
    }

    @PostMapping("/roles")
    public R<SysRole> createRole(@RequestBody Map<String, Object> body) {
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

        return R.ok(role);
    }

    @PutMapping("/roles/{id}")
    public R<SysRole> updateRole(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            return R.fail("角色不存在");
        }
        if (body.containsKey("roleName")) role.setRoleName((String) body.get("roleName"));
        if (body.containsKey("roleCode")) role.setRoleCode((String) body.get("roleCode"));
        if (body.containsKey("dataScope")) role.setDataScope((String) body.get("dataScope"));
        if (body.containsKey("status")) role.setStatus((String) body.get("status"));
        if (body.containsKey("permissions")) role.setPermissions(body.get("permissions").toString());
        sysRoleMapper.updateById(role);

        return R.ok(role);
    }

    @DeleteMapping("/roles/{id}")
    public R<String> deleteRole(@PathVariable Long id) {
        sysRoleMapper.deleteById(id);
        return R.ok("删除成功");
    }

    // ==================== 组织管理 ====================

    @GetMapping("/orgs")
    public R<List<Organization>> listOrgs() {
        List<Organization> orgs = organizationMapper.selectList(null);
        return R.ok(orgs);
    }

    @GetMapping("/orgs/{id}")
    public R<Organization> getOrg(@PathVariable Long id) {
        Organization org = organizationMapper.selectById(id);
        return R.ok(org);
    }

    @PutMapping("/orgs/{id}")
    public R<Organization> updateOrg(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Organization org = organizationMapper.selectById(id);
        if (org == null) {
            return R.fail("组织不存在");
        }
        if (body.containsKey("orgName")) org.setOrgName((String) body.get("orgName"));
        if (body.containsKey("orgType")) org.setOrgType((String) body.get("orgType"));
        if (body.containsKey("subscriptionLevel")) org.setSubscriptionLevel((String) body.get("subscriptionLevel"));
        if (body.containsKey("maxMembers")) org.setMaxMembers((Integer) body.get("maxMembers"));
        if (body.containsKey("status")) org.setStatus((String) body.get("status"));
        organizationMapper.updateById(org);

        return R.ok(org);
    }

    // ==================== 当前用户信息 ====================

    @GetMapping("/current")
    public R<Map<String, Object>> getCurrentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        SysUser user = sysUserMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        if (user != null && user.getCurrentOrgId() != null) {
            Organization org = organizationMapper.selectById(user.getCurrentOrgId());
            response.put("org", org);
            List<UserOrg> userOrgs = userOrgMapper.selectList(
                    new LambdaQueryWrapper<UserOrg>().eq(UserOrg::getUserId, userId));
            List<SysRole> roles = new ArrayList<>();
            for (UserOrg uo : userOrgs) {
                SysRole role = sysRoleMapper.selectById(uo.getRoleId());
                if (role != null) roles.add(role);
            }
            response.put("roles", roles);
        }
        return R.ok(response);
    }
}
