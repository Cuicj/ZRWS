package com.zrws.approval.security;

import com.zrws.approval.domain.entity.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 安全用户包装类
 * <p>包装 {@link SysUser} 实体并实现 {@link UserDetails}，供 Spring Security 使用
 */
public class SecurityUser implements UserDetails {

    private final SysUser sysUser;

    public SecurityUser(SysUser sysUser) {
        this.sysUser = sysUser;
    }

    public SysUser getSysUser() {
        return sysUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return sysUser.getPassword();
    }

    @Override
    public String getUsername() {
        return sysUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return SysUser.Status.ACTIVE.name().equalsIgnoreCase(sysUser.getStatus());
    }
}
