package com.zrws.approval.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zrws.approval.domain.entity.SysUser;
import com.zrws.approval.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 用户详情服务实现
 * <p>根据用户名加载用户，返回 {@link SecurityUser}（权限暂设为 ROLE_USER）
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return new SecurityUser(user);
    }
}
