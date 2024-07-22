package io.github.jarryzhou.sensitivecontentfilter.repository;

import io.github.jarryzhou.sensitivecontentfilter.entity.SensitiveContentHitHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensitiveContentHitHistoryRepository extends JpaRepository<SensitiveContentHitHistory, Long> {
}
