package com.github.jarryzhou.sensitivecontentfilter.repository;

import com.github.jarryzhou.sensitivecontentfilter.entity.SensitiveContentHitHistory;
import com.github.jarryzhou.sensitivecontentfilter.entity.SensitiveText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public
interface SensitiveContentHitHistoryRepository extends JpaRepository<SensitiveContentHitHistory, Long> {
}