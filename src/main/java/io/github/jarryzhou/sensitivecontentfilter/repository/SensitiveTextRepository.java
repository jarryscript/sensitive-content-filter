package io.github.jarryzhou.sensitivecontentfilter.repository;

import io.github.jarryzhou.sensitivecontentfilter.entity.SensitiveText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensitiveTextRepository extends JpaRepository<SensitiveText, Long> {
}
