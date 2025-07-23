package com.programmingplatform.mapper.primary;

import com.programmingplatform.entity.User;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数据访问层接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查找用户
     */
    @Select("SELECT * FROM users WHERE id = #{id}")
    User findById(Long id);

    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    User findByEmail(String email);

    /**
     * 检查用户名是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE username = #{username}")
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM users WHERE email = #{email}")
    boolean existsByEmail(String email);

    /**
     * 插入新用户
     */
    @Insert("INSERT INTO users (username, email, password_hash, full_name, role, created_at, updated_at) " +
            "VALUES (#{username}, #{email}, #{passwordHash}, #{fullName}, #{role}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    /**
     * 更新用户信息
     */
    @Update("UPDATE users SET " +
            "username = #{username}, " +
            "email = #{email}, " +
            "full_name = #{fullName}, " +
            "avatar_url = #{avatarUrl}, " +
            "bio = #{bio}, " +
            "updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int update(User user);

    /**
     * 更新用户密码
     */
    @Update("UPDATE users SET password_hash = #{passwordHash}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 更新用户状态
     */
    @Update("UPDATE users SET is_active = #{isActive}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("isActive") Boolean isActive, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 更新邮箱验证状态
     */
    @Update("UPDATE users SET email_verified = #{emailVerified}, updated_at = #{updatedAt} WHERE id = #{id}")
    int updateEmailVerified(@Param("id") Long id, @Param("emailVerified") Boolean emailVerified, @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * 更新最后登录时间
     */
    @Update("UPDATE users SET last_login_at = #{lastLoginAt} WHERE id = #{id}")
    int updateLastLoginAt(@Param("id") Long id, @Param("lastLoginAt") LocalDateTime lastLoginAt);

    /**
     * 删除用户
     */
    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 分页查询用户列表
     */
    @Select("<script>" +
            "SELECT * FROM users " +
            "<where>" +
            "<if test='role != null'>AND role = #{role}</if>" +
            "<if test='isActive != null'>AND is_active = #{isActive}</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%') OR full_name LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "</where>" +
            "ORDER BY created_at DESC " +
            "LIMIT #{offset}, #{limit}" +
            "</script>")
    List<User> findByPage(@Param("role") User.UserRole role, 
                         @Param("isActive") Boolean isActive, 
                         @Param("keyword") String keyword,
                         @Param("offset") int offset, 
                         @Param("limit") int limit);

    /**
     * 统计用户总数
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM users " +
            "<where>" +
            "<if test='role != null'>AND role = #{role}</if>" +
            "<if test='isActive != null'>AND is_active = #{isActive}</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (username LIKE CONCAT('%', #{keyword}, '%') OR email LIKE CONCAT('%', #{keyword}, '%') OR full_name LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "</where>" +
            "</script>")
    long countByCondition(@Param("role") User.UserRole role, 
                         @Param("isActive") Boolean isActive, 
                         @Param("keyword") String keyword);

    /**
     * 获取用户统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_users, " +
            "SUM(CASE WHEN role = 'STUDENT' THEN 1 ELSE 0 END) as student_count, " +
            "SUM(CASE WHEN role = 'INSTRUCTOR' THEN 1 ELSE 0 END) as instructor_count, " +
            "SUM(CASE WHEN role = 'ADMIN' THEN 1 ELSE 0 END) as admin_count, " +
            "SUM(CASE WHEN is_active = true THEN 1 ELSE 0 END) as active_count, " +
            "SUM(CASE WHEN email_verified = true THEN 1 ELSE 0 END) as verified_count " +
            "FROM users")
    @Results({
        @Result(property = "totalUsers", column = "total_users"),
        @Result(property = "studentCount", column = "student_count"),
        @Result(property = "instructorCount", column = "instructor_count"),
        @Result(property = "adminCount", column = "admin_count"),
        @Result(property = "activeCount", column = "active_count"),
        @Result(property = "verifiedCount", column = "verified_count")
    })
    UserStatistics getUserStatistics();

    /**
     * 用户统计信息类
     */
    class UserStatistics {
        private long totalUsers;
        private long studentCount;
        private long instructorCount;
        private long adminCount;
        private long activeCount;
        private long verifiedCount;

        // Getters and Setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
        
        public long getStudentCount() { return studentCount; }
        public void setStudentCount(long studentCount) { this.studentCount = studentCount; }
        
        public long getInstructorCount() { return instructorCount; }
        public void setInstructorCount(long instructorCount) { this.instructorCount = instructorCount; }
        
        public long getAdminCount() { return adminCount; }
        public void setAdminCount(long adminCount) { this.adminCount = adminCount; }
        
        public long getActiveCount() { return activeCount; }
        public void setActiveCount(long activeCount) { this.activeCount = activeCount; }
        
        public long getVerifiedCount() { return verifiedCount; }
        public void setVerifiedCount(long verifiedCount) { this.verifiedCount = verifiedCount; }
    }
}
