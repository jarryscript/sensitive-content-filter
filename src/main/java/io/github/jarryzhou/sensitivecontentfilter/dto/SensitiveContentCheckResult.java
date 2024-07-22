package io.github.jarryzhou.sensitivecontentfilter.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class SensitiveContentCheckResult {
    private boolean sensitive;
    private LocalDateTime checkTime;
    private String originalContent;
    private Set<String> sensitiveContent;

    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public LocalDateTime getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(LocalDateTime checkTime) {
        this.checkTime = checkTime;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String originalContent) {
        this.originalContent = originalContent;
    }

    public Set<String> getSensitiveContent() {
        return sensitiveContent;
    }

    public void setSensitiveContent(Set<String> sensitiveContent) {
        this.sensitiveContent = sensitiveContent;
    }
}