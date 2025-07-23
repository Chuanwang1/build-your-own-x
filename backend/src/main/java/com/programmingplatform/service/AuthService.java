package com.programmingplatform.service;

import com.programmingplatform.dto.request.LoginRequest;
import com.programmingplatform.dto.request.RegisterRequest;
import com.programmingplatform.dto.response.JwtAuthenticationResponse;
import com.programmingplatform.entity.User;
import com.programmingplatform.exception.BadRequestException;
import com.programmingplatform.exception.ResourceNotFoundException;
import com.programmingplatform.mapper.primary.UserMapper;
import com.programmingplatform.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务类
 * 处理用户注册、登录、令牌刷新等认证相关业务逻辑
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${app.jwt.refresh-expiration}")
    private long jwtRefreshExpirationInMs;

    /**
     * 用户注册
     */
    @Transactional
    public JwtAuthenticationResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userMapper.existsByUsername(request.getUsername())) {
            throw new BadRequestException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userMapper.existsByEmail(request.getEmail())) {
            throw new BadRequestException("邮箱已存在");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(User.UserRole.STUDENT);
        user.setIsActive(true);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        // 自动登录
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成令牌
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        // 将刷新令牌存储到 Redis
        storeRefreshToken(user.getId(), refreshToken);

        // 更新最后登录时间
        userMapper.updateLastLoginAt(user.getId(), LocalDateTime.now());

        // 构建响应
        JwtAuthenticationResponse.UserInfo userInfo = new JwtAuthenticationResponse.UserInfo(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            user.getAvatarUrl()
        );

        return new JwtAuthenticationResponse(accessToken, refreshToken, jwtExpirationInMs / 1000, userInfo);
    }

    /**
     * 用户登录
     */
    @Transactional
    public JwtAuthenticationResponse login(LoginRequest request) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsernameOrEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 获取用户信息
        User user = userMapper.findByUsername(request.getUsernameOrEmail());
        if (user == null) {
            user = userMapper.findByEmail(request.getUsernameOrEmail());
        }

        if (user == null) {
            throw new ResourceNotFoundException("用户不存在");
        }

        // 检查用户状态
        if (!user.getIsActive()) {
            throw new BadRequestException("账户已被禁用");
        }

        // 生成令牌
        String accessToken = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        // 将刷新令牌存储到 Redis
        storeRefreshToken(user.getId(), refreshToken);

        // 更新最后登录时间
        userMapper.updateLastLoginAt(user.getId(), LocalDateTime.now());

        // 构建响应
        JwtAuthenticationResponse.UserInfo userInfo = new JwtAuthenticationResponse.UserInfo(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            user.getAvatarUrl()
        );

        return new JwtAuthenticationResponse(accessToken, refreshToken, jwtExpirationInMs / 1000, userInfo);
    }

    /**
     * 刷新访问令牌
     */
    public JwtAuthenticationResponse refreshToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new BadRequestException("刷新令牌不能为空");
        }

        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            throw new BadRequestException("无效的刷新令牌");
        }

        Long userId = tokenProvider.getUserIdFromToken(refreshToken);

        // 验证刷新令牌是否存在于 Redis 中
        String storedToken = getStoredRefreshToken(userId);
        if (!refreshToken.equals(storedToken)) {
            throw new BadRequestException("刷新令牌已失效");
        }

        // 获取用户信息
        User user = userMapper.findById(userId);
        if (user == null || !user.getIsActive()) {
            throw new BadRequestException("用户不存在或已被禁用");
        }

        // 生成新的访问令牌
        String newAccessToken = tokenProvider.generateTokenFromRefreshToken(refreshToken);

        // 构建响应
        JwtAuthenticationResponse.UserInfo userInfo = new JwtAuthenticationResponse.UserInfo(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            user.getAvatarUrl()
        );

        return new JwtAuthenticationResponse(newAccessToken, refreshToken, jwtExpirationInMs / 1000, userInfo);
    }

    /**
     * 用户登出
     */
    public void logout(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (tokenProvider.validateToken(token)) {
            Long userId = tokenProvider.getUserIdFromToken(token);
            
            // 从 Redis 中删除刷新令牌
            removeRefreshToken(userId);
            
            // 将访问令牌加入黑名单
            blacklistToken(token);
        }
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (!tokenProvider.validateToken(token)) {
            return false;
        }

        // 检查令牌是否在黑名单中
        return !isTokenBlacklisted(token);
    }

    /**
     * 存储刷新令牌到 Redis
     */
    private void storeRefreshToken(Long userId, String refreshToken) {
        String key = "refresh_token:" + userId;
        redisTemplate.opsForValue().set(key, refreshToken, jwtRefreshExpirationInMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 从 Redis 获取存储的刷新令牌
     */
    private String getStoredRefreshToken(Long userId) {
        String key = "refresh_token:" + userId;
        return (String) redisTemplate.opsForValue().get(key);
    }

    /**
     * 从 Redis 删除刷新令牌
     */
    private void removeRefreshToken(Long userId) {
        String key = "refresh_token:" + userId;
        redisTemplate.delete(key);
    }

    /**
     * 将令牌加入黑名单
     */
    private void blacklistToken(String token) {
        String key = "blacklist_token:" + token;
        long expiration = tokenProvider.getExpirationDateFromToken(token).getTime() - System.currentTimeMillis();
        if (expiration > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", expiration, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 检查令牌是否在黑名单中
     */
    private boolean isTokenBlacklisted(String token) {
        String key = "blacklist_token:" + token;
        return redisTemplate.hasKey(key);
    }
}
