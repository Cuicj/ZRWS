package com.zrws.approval.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.dto.LoginRequest;
import com.zrws.approval.domain.dto.LoginResponse;
import com.zrws.approval.domain.dto.RegisterRequest;
import com.zrws.approval.domain.entity.Organization;
import com.zrws.approval.domain.entity.SysRole;
import com.zrws.approval.domain.entity.SysUser;
import com.zrws.approval.domain.entity.UserOrg;
import com.zrws.approval.mapper.OrganizationMapper;
import com.zrws.approval.mapper.SysRoleMapper;
import com.zrws.approval.mapper.SysUserMapper;
import com.zrws.approval.mapper.UserOrgMapper;
import com.zrws.approval.security.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 */
@Slf4j
@Service
public class AuthService {

    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String TOKEN_CACHE_PREFIX = "auth:token:";
    private static final long SMS_CODE_EXPIRE_MINUTES = 5;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private UserOrgMapper userOrgMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RedisService redisService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 登录
     */
    public LoginResponse login(LoginRequest request) {
        // 查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername())
        );
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        // 校验密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        // 校验状态
        if (!SysUser.Status.ACTIVE.name().equals(user.getStatus())) {
            throw new IllegalStateException("账号已被禁用");
        }
        // 生成JWT
        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), user.getCurrentOrgId(), user.getTenantId());
        // 缓存token
        redisService.set(TOKEN_CACHE_PREFIX + user.getId(), token);
        return buildLoginResponse(user, token);
    }

    /**
     * 注册
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request) {
        // 1. 验证验证码
        String cacheKey = SMS_CODE_PREFIX + request.getPhone();
        Object cachedCode = redisService.get(cacheKey);
        if (cachedCode == null) {
            throw new IllegalArgumentException("验证码已过期，请重新获取");
        }
        if (!cachedCode.toString().equals(request.getVerifyCode())) {
            throw new IllegalArgumentException("验证码错误");
        }
        // 2. 检查手机号是否已注册
        Long existCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getPhone, request.getPhone())
        );
        if (existCount != null && existCount > 0) {
            throw new IllegalStateException("该手机号已注册");
        }
        // 3. 创建组织（PERSONAL类型，FREE计划，maxMembers=3）
        Organization org = new Organization();
        org.setOrgName(request.getOrgName());
        org.setOrgType("PERSONAL");
        org.setSubscriptionLevel("FREE");
        org.setMaxMembers(3);
        org.setStatus("ACTIVE");
        organizationMapper.insert(org);
        // 租户ID为组织自身ID
        org.setTenantId(org.getId());
        organizationMapper.updateById(org);
        // 4. 创建用户
        SysUser user = new SysUser();
        user.setUsername(request.getPhone());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setAlipayUserId(request.getAlipayUserId());
        user.setWechatOpenid(request.getWechatOpenid());
        user.setStatus(SysUser.Status.ACTIVE.name());
        user.setCurrentOrgId(org.getId());
        user.setTenantId(org.getId());
        sysUserMapper.insert(user);
        // 5. 创建默认角色（ORG_ADMIN，roleType=CUSTOM，dataScope=ALL）
        SysRole role = new SysRole();
        role.setRoleName("组织管理员");
        role.setRoleCode("ORG_ADMIN");
        role.setRoleType(SysRole.RoleType.CUSTOM.name());
        role.setDataScope(SysRole.DataScope.ALL.name());
        role.setStatus(SysRole.Status.ACTIVE.name());
        role.setTenantId(org.getId());
        sysRoleMapper.insert(role);
        // 6. 创建用户-组织关联
        UserOrg userOrg = new UserOrg();
        userOrg.setUserId(user.getId());
        userOrg.setOrgId(org.getId());
        userOrg.setRoleId(role.getId());
        userOrg.setStatus(UserOrg.Status.ACTIVE.name());
        userOrg.setJoinTime(LocalDateTime.now());
        userOrg.setTenantId(org.getId());
        userOrgMapper.insert(userOrg);
        // 7. 生成JWT
        String token = jwtTokenProvider.generateToken(
                user.getId(), user.getUsername(), org.getId(), org.getId());
        // 缓存token
        redisService.set(TOKEN_CACHE_PREFIX + user.getId(), token);
        // 清除验证码
        redisService.delete(cacheKey);
        return buildLoginResponse(user, token);
    }

    /**
     * 发送验证码（开发阶段直接返回，不实际发短信）
     */
    public String sendVerifyCode(String phone) {
        // 生成6位随机验证码
        String code = String.format("%06d", new Random().nextInt(1000000));
        // 存入Redis，5分钟过期
        redisService.set(SMS_CODE_PREFIX + phone, code, SMS_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.info("发送验证码: phone={}, code={}", phone, code);
        return code;
    }

    /**
     * 退出登录
     */
    public void logout(String token) {
        try {
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            if (userId != null) {
                redisService.delete(TOKEN_CACHE_PREFIX + userId);
            }
        } catch (Exception e) {
            log.warn("退出登录解析token失败: {}", e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    public LoginResponse getCurrentUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return buildLoginResponse(user, null);
    }

    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(SysUser user, String token) {
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setOrgId(user.getCurrentOrgId());
        response.setTenantId(user.getTenantId());
        response.setAvatar(user.getAvatar());
        // 查询组织信息
        if (user.getCurrentOrgId() != null) {
            Organization org = organizationMapper.selectById(user.getCurrentOrgId());
            if (org != null) {
                response.setOrgName(org.getOrgName());
                response.setSubscriptionLevel(org.getSubscriptionLevel());
            }
        }
        return response;
    }
}
