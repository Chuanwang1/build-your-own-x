package com.programmingplatform.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程实体类
 */
public class Course {
    
    private Long id;
    
    @NotBlank(message = "课程标题不能为空")
    @Size(max = 200, message = "课程标题长度不能超过200个字符")
    private String title;
    
    private String description;
    
    @Size(max = 500, message = "简短描述长度不能超过500个字符")
    private String shortDescription;
    
    @NotNull(message = "讲师ID不能为空")
    private Long instructorId;
    
    private Long categoryId;
    
    private CourseLevel level = CourseLevel.BEGINNER;
    
    @NotBlank(message = "编程语言不能为空")
    private String language;
    
    private String thumbnailUrl;
    
    private BigDecimal price = BigDecimal.ZERO;
    
    private Boolean isFree = true;
    
    private Boolean isPublished = false;
    
    private Integer durationHours = 0;
    
    private Integer totalLessons = 0;
    
    private Integer enrollmentCount = 0;
    
    private BigDecimal rating = BigDecimal.ZERO;
    
    private Integer ratingCount = 0;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    // 课程难度级别枚举
    public enum CourseLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    // 构造函数
    public Course() {}

    public Course(String title, Long instructorId, String language) {
        this.title = title;
        this.instructorId = instructorId;
        this.language = language;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(Long instructorId) {
        this.instructorId = instructorId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public CourseLevel getLevel() {
        return level;
    }

    public void setLevel(CourseLevel level) {
        this.level = level;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getIsFree() {
        return isFree;
    }

    public void setIsFree(Boolean isFree) {
        this.isFree = isFree;
    }

    public Boolean getIsPublished() {
        return isPublished;
    }

    public void setIsPublished(Boolean isPublished) {
        this.isPublished = isPublished;
    }

    public Integer getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(Integer durationHours) {
        this.durationHours = durationHours;
    }

    public Integer getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(Integer totalLessons) {
        this.totalLessons = totalLessons;
    }

    public Integer getEnrollmentCount() {
        return enrollmentCount;
    }

    public void setEnrollmentCount(Integer enrollmentCount) {
        this.enrollmentCount = enrollmentCount;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", instructorId=" + instructorId +
                ", language='" + language + '\'' +
                ", level=" + level +
                ", isPublished=" + isPublished +
                ", enrollmentCount=" + enrollmentCount +
                ", rating=" + rating +
                ", createdAt=" + createdAt +
                '}';
    }
}
