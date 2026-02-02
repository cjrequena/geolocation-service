package com.cjrequena.sample.domain.model.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Enhanced Audit Info value object for tracking creation and modification timestamps.
 * Provides additional utility methods for audit trail analysis.
 */
@Getter
@EqualsAndHashCode
public class AuditInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DEFAULT_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String createdBy;
    private final String updatedBy;

    private AuditInfo(
            LocalDateTime createdAt, 
            LocalDateTime updatedAt,
            String createdBy,
            String updatedBy) {
        
        if (createdAt == null) {
            throw new IllegalArgumentException("createdAt cannot be null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("updatedAt cannot be null");
        }
        if (updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("updatedAt cannot be before createdAt");
        }
        
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    /**
     * Create new audit info with current timestamp.
     */
    public static AuditInfo create() {
        LocalDateTime now = LocalDateTime.now();
        return new AuditInfo(now, now, null, null);
    }

    /**
     * Create new audit info with creator.
     */
    public static AuditInfo create(String createdBy) {
        LocalDateTime now = LocalDateTime.now();
        return new AuditInfo(now, now, createdBy, createdBy);
    }

    /**
     * Create audit info from timestamps.
     */
    public static AuditInfo of(LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new AuditInfo(createdAt, updatedAt, null, null);
    }

    /**
     * Create audit info with all fields.
     */
    public static AuditInfo of(
            LocalDateTime createdAt, 
            LocalDateTime updatedAt,
            String createdBy,
            String updatedBy) {
        return new AuditInfo(createdAt, updatedAt, createdBy, updatedBy);
    }

    /**
     * Update the audit info (sets updatedAt to now).
     */
    public AuditInfo update() {
        return new AuditInfo(this.createdAt, LocalDateTime.now(), this.createdBy, this.updatedBy);
    }

    /**
     * Update the audit info with updater.
     */
    public AuditInfo update(String updatedBy) {
        return new AuditInfo(this.createdAt, LocalDateTime.now(), this.createdBy, updatedBy);
    }

    /**
     * Update with specific timestamp.
     */
    public AuditInfo updateAt(LocalDateTime updatedAt) {
        return new AuditInfo(this.createdAt, updatedAt, this.createdBy, this.updatedBy);
    }

    /**
     * Update with specific timestamp and updater.
     */
    public AuditInfo updateAt(LocalDateTime updatedAt, String updatedBy) {
        return new AuditInfo(this.createdAt, updatedAt, this.createdBy, updatedBy);
    }

    /**
     * Set created by (useful when reconstructing from DB).
     */
    public AuditInfo withCreatedBy(String createdBy) {
        return new AuditInfo(this.createdAt, this.updatedAt, createdBy, this.updatedBy);
    }

    /**
     * Set updated by (useful when reconstructing from DB).
     */
    public AuditInfo withUpdatedBy(String updatedBy) {
        return new AuditInfo(this.createdAt, this.updatedAt, this.createdBy, updatedBy);
    }

    // ==================== Query Methods ====================

    /**
     * Check if this entity was ever updated (updatedAt > createdAt).
     */
    public boolean wasUpdated() {
        return !updatedAt.equals(createdAt);
    }

    /**
     * Check if updated within last N minutes.
     */
    public boolean wasUpdatedWithinMinutes(long minutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);
        return updatedAt.isAfter(threshold);
    }

    /**
     * Check if updated within last N hours.
     */
    public boolean wasUpdatedWithinHours(long hours) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        return updatedAt.isAfter(threshold);
    }

    /**
     * Check if updated within last N days.
     */
    public boolean wasUpdatedWithinDays(long days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return updatedAt.isAfter(threshold);
    }

    /**
     * Check if created within last N days.
     */
    public boolean wasCreatedWithinDays(long days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return createdAt.isAfter(threshold);
    }

    /**
     * Check if entity is stale (not updated for N days).
     */
    public boolean isStale(long days) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        return updatedAt.isBefore(threshold);
    }

    /**
     * Check if entity is fresh (created within last hour).
     */
    public boolean isFresh() {
        return wasCreatedWithinHours(1);
    }

    /**
     * Check if created within last N hours.
     */
    public boolean wasCreatedWithinHours(long hours) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(hours);
        return createdAt.isAfter(threshold);
    }

    /**
     * Check if created by a specific user.
     */
    public boolean wasCreatedBy(String user) {
        return createdBy != null && createdBy.equals(user);
    }

    /**
     * Check if updated by a specific user.
     */
    public boolean wasUpdatedBy(String user) {
        return updatedBy != null && updatedBy.equals(user);
    }

    /**
     * Check if same user created and updated.
     */
    public boolean wasMaintainedBySameUser() {
        return createdBy != null && createdBy.equals(updatedBy);
    }

    // ==================== Duration Calculations ====================

    /**
     * Get the age of the entity (time since creation).
     */
    public Duration getAge() {
        return Duration.between(createdAt, LocalDateTime.now());
    }

    /**
     * Get age in days.
     */
    public long getAgeInDays() {
        return ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
    }

    /**
     * Get age in hours.
     */
    public long getAgeInHours() {
        return ChronoUnit.HOURS.between(createdAt, LocalDateTime.now());
    }

    /**
     * Get age in minutes.
     */
    public long getAgeInMinutes() {
        return ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());
    }

    /**
     * Get time since last update.
     */
    public Duration getTimeSinceLastUpdate() {
        return Duration.between(updatedAt, LocalDateTime.now());
    }

    /**
     * Get days since last update.
     */
    public long getDaysSinceLastUpdate() {
        return ChronoUnit.DAYS.between(updatedAt, LocalDateTime.now());
    }

    /**
     * Get hours since last update.
     */
    public long getHoursSinceLastUpdate() {
        return ChronoUnit.HOURS.between(updatedAt, LocalDateTime.now());
    }

    /**
     * Get time between creation and last update.
     */
    public Duration getMaintenanceDuration() {
        return Duration.between(createdAt, updatedAt);
    }

    /**
     * Get days between creation and last update.
     */
    public long getMaintenanceDurationInDays() {
        return ChronoUnit.DAYS.between(createdAt, updatedAt);
    }

    // ==================== Formatting Methods ====================

    /**
     * Format created timestamp.
     */
    public String formatCreatedAt() {
        return createdAt.format(DEFAULT_FORMATTER);
    }

    /**
     * Format updated timestamp.
     */
    public String formatUpdatedAt() {
        return updatedAt.format(DEFAULT_FORMATTER);
    }

    /**
     * Format created timestamp with custom pattern.
     */
    public String formatCreatedAt(String pattern) {
        return createdAt.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Format updated timestamp with custom pattern.
     */
    public String formatUpdatedAt(String pattern) {
        return updatedAt.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Get human-readable age.
     */
    public String getAgeDescription() {
        long days = getAgeInDays();
        if (days == 0) {
            long hours = getAgeInHours();
            if (hours == 0) {
                long minutes = getAgeInMinutes();
                return minutes + " minute" + (minutes != 1 ? "s" : "") + " old";
            }
            return hours + " hour" + (hours != 1 ? "s" : "") + " old";
        }
        if (days < 30) {
            return days + " day" + (days != 1 ? "s" : "") + " old";
        }
        long months = days / 30;
        if (months < 12) {
            return months + " month" + (months != 1 ? "s" : "") + " old";
        }
        long years = days / 365;
        return years + " year" + (years != 1 ? "s" : "") + " old";
    }

    /**
     * Get human-readable time since last update.
     */
    public String getTimeSinceUpdateDescription() {
        if (!wasUpdated()) {
            return "Never updated";
        }
        
        long days = getDaysSinceLastUpdate();
        if (days == 0) {
            long hours = getHoursSinceLastUpdate();
            if (hours == 0) {
                return "Updated less than an hour ago";
            }
            return "Updated " + hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        }
        if (days < 30) {
            return "Updated " + days + " day" + (days != 1 ? "s" : "") + " ago";
        }
        long months = days / 30;
        return "Updated " + months + " month" + (months != 1 ? "s" : "") + " ago";
    }

    /**
     * Get audit trail summary.
     */
    public String getAuditSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Created: ").append(formatCreatedAt());
        if (createdBy != null) {
            sb.append(" by ").append(createdBy);
        }
        sb.append(" | Updated: ").append(formatUpdatedAt());
        if (updatedBy != null) {
            sb.append(" by ").append(updatedBy);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        if (createdBy != null || updatedBy != null) {
            return String.format("AuditInfo[created=%s by %s, updated=%s by %s]",
                formatCreatedAt(), 
                createdBy != null ? createdBy : "unknown",
                formatUpdatedAt(),
                updatedBy != null ? updatedBy : "unknown");
        }
        return String.format("AuditInfo[created=%s, updated=%s]", 
            formatCreatedAt(), formatUpdatedAt());
    }
}
