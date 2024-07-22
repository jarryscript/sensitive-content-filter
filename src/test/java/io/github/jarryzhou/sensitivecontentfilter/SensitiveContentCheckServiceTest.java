package io.github.jarryzhou.sensitivecontentfilter;

import io.github.jarryzhou.sensitivecontentfilter.dto.SensitiveContentCheckResult;
import io.github.jarryzhou.sensitivecontentfilter.entity.SensitiveText;
import io.github.jarryzhou.sensitivecontentfilter.repository.SensitiveContentHitHistoryRepository;
import io.github.jarryzhou.sensitivecontentfilter.repository.SensitiveTextRepository;
import io.github.jarryzhou.sensitivecontentfilter.service.SensitiveContentCheckService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootTest
class SensitiveContentCheckServiceTest {
    private static final List<SensitiveText> SENSITIVE_TEXTS = Stream.of("你妹", "傻逼").map(text -> {
        SensitiveText sensitiveText = new SensitiveText();
        sensitiveText.setContent(text);
        return sensitiveText;
    }).collect(Collectors.toList());
    @MockBean
    private SensitiveContentHitHistoryRepository sensitiveContentHitHistoryRepository;
    @MockBean
    private SensitiveTextRepository sensitiveTextRepository;
    @Autowired
    private SensitiveContentCheckService sensitiveContentCheckService;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void shouldReturnNonSensitiveResult_whenNoSensitiveTextConfigured() {
        given(sensitiveTextRepository.findAll()).willReturn(Collections.emptyList());
        var text = "我去你妹的";

        SensitiveContentCheckResult checkResult = sensitiveContentCheckService.check(text);

        assertThat(checkResult).isNotNull();
        assertFalse(checkResult.isSensitive());
        assertEquals(text, checkResult.getOriginalContent());
        assertThat(checkResult.getSensitiveContent()).isNullOrEmpty();
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void shouldReturnNull_whenNoSensitiveContent() {
        given(sensitiveTextRepository.findAll()).willReturn(SENSITIVE_TEXTS);
        var text = "你真可爱";

        SensitiveContentCheckResult checkResult = sensitiveContentCheckService.check(text);

        assertThat(checkResult).isNotNull();
        assertFalse(checkResult.isSensitive());
        assertEquals(text, checkResult.getOriginalContent());
        assertThat(checkResult.getSensitiveContent()).isNullOrEmpty();
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void shouldNotCreateHitHistory_whenSensitiveContentFound_andDisabledHistoryGeneration() {
        given(sensitiveTextRepository.findAll()).willReturn(SENSITIVE_TEXTS);
        var text = "你TM傻逼";

        SensitiveContentCheckResult checkResult = sensitiveContentCheckService.check(text, false);

        assertThat(checkResult).isNotNull();
        assertTrue(checkResult.isSensitive());
        assertEquals(text, checkResult.getOriginalContent());
        assertIterableEquals(Set.of("傻逼"), checkResult.getSensitiveContent());
        verify(sensitiveContentHitHistoryRepository, times(0)).save(any());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void shouldCreateHitHistory_whenSensitiveContentFound_andEnabledHistoryGeneration() {
        given(sensitiveTextRepository.findAll()).willReturn(SENSITIVE_TEXTS);
        var text = "你TM傻逼";

        SensitiveContentCheckResult checkResult = sensitiveContentCheckService.check(text, true);

        assertThat(checkResult).isNotNull();
        assertTrue(checkResult.isSensitive());
        assertEquals(text, checkResult.getOriginalContent());
        assertIterableEquals(Set.of("傻逼"), checkResult.getSensitiveContent());
        verify(sensitiveContentHitHistoryRepository).save(any());
    }

}
