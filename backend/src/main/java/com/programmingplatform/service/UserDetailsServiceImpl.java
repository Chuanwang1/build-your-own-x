package com.programmingplatform.service;

import com.programmingplatform.entity.User;
import com.programmingplatform.mapper.primary.UserMapper;
import com.programmingplatform.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security UserDetailsService 实现
 * 用于加载用户认证信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            user = userMapper.findByEmail(username);
        }
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userMapper.findById(id);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + id);
        }

        return UserPrincipal.create(user);
    }
}
