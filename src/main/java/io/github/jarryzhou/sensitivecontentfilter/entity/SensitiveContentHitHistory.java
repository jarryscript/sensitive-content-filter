package io.github.jarryzhou.sensitivecontentfilter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import org.springframework.data.annotation.CreatedDate;

import java.sql.Timestamp;

@Entity
public class SensitiveContentHitHistory {
    @Id
    @GeneratedValue
    private long id;

    private String checkedText;

    private String sensitiveContent;

    @CreatedDate
    public Timestamp createTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCheckedText() {
        return checkedText;
    }

    public void setCheckedText(String checkedText) {
        this.checkedText = checkedText;
    }

    public String getSensitiveContent() {
        return sensitiveContent;
    }

    public void setSensitiveContent(String sensitiveContent) {
        this.sensitiveContent = sensitiveContent;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
