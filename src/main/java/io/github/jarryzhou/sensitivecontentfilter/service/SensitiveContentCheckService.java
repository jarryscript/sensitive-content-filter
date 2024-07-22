package io.github.jarryzhou.sensitivecontentfilter.service;

import io.github.jarryzhou.sensitivecontentfilter.dto.SensitiveContentCheckResult;
import io.github.jarryzhou.sensitivecontentfilter.entity.SensitiveContentHitHistory;
import io.github.jarryzhou.sensitivecontentfilter.entity.SensitiveText;
import io.github.jarryzhou.sensitivecontentfilter.repository.SensitiveContentHitHistoryRepository;
import io.github.jarryzhou.sensitivecontentfilter.repository.SensitiveTextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SensitiveContentCheckService
 * <p>
 * Author: Jarry Zhou
 * Date: 2021/9/29
 * Description: 敏感内容检测服务
 **/
@Service
public class SensitiveContentCheckService {
    public static final String SENSITIVE_TEXT_DELIMITER = ",";

    @Autowired
    private SensitiveTextRepository sensitiveTextRepository;
    @Autowired
    private SensitiveContentHitHistoryRepository sensitiveContentHitHistoryRepository;

    private WordNode sensitiveWordNodeTree;

    public SensitiveContentCheckResult check(String text) {
        return check(text, true);
    }

    public SensitiveContentCheckResult check(String text, boolean generateRecord) {
        if (sensitiveWordNodeTree == null) {
            initSensitiveWordNodeTree();
        }
        Set<String> hits = findSensitiveWords(text);
        if (hits != null && !hits.isEmpty() && generateRecord) {
            generateSensitiveContentCheckHistory(text, hits);
        }

        return buildCheckResult(text, hits);
    }

    private void initSensitiveWordNodeTree() {
        List<SensitiveText> all = sensitiveTextRepository.findAll();
        buildDFATree(all.stream().map(SensitiveText::getContent).collect(Collectors.toList()));
    }

    private void buildDFATree(List<String> strings) {
        WordNode root = new WordNode();
        strings.forEach(word -> {
            WordNode current = root;
            for (int i = 0; i < word.length(); i++) {
                char ch = word.charAt(i);
                current.putChildIfAbsent(ch, new WordNode());
                current = current.getChildren().get(ch);
                if (i == word.length() - 1) {
                    current.setEnd(true);
                }
            }
        });
        sensitiveWordNodeTree = root;
    }

    private SensitiveContentCheckResult buildCheckResult(String text, Set<String> hits) {
        SensitiveContentCheckResult result = new SensitiveContentCheckResult();
        result.setCheckTime(LocalDateTime.now());
        result.setSensitive(hits != null && !hits.isEmpty());
        result.setSensitiveContent(hits);
        result.setOriginalContent(text);
        return result;

    }

    private Set<String> findSensitiveWords(String text) {
        Set<String> hits = new HashSet<>();
        for (int i = 0; i < text.length(); i++) {
            int matchedLength = doCheck(sensitiveWordNodeTree.getChildren(), text, i);
            if (matchedLength > 0) {
                hits.add(text.substring(i, i + matchedLength));
                i += matchedLength - 1;
            }
        }
        return hits;
    }

    private int doCheck(Map<Character, WordNode> sensitiveWords, String txt, int beginIndex) {
        if (sensitiveWords == null || sensitiveWords.isEmpty()) {
            return 0;
        }
        boolean allMatches = false;
        int matchedIndex = 0;
        for (int i = beginIndex; i < txt.length(); i++) {
            WordNode wordNode = sensitiveWords.get(txt.charAt(i));
            if (wordNode == null) {
                break;
            }
            matchedIndex++;
            sensitiveWords = wordNode.getChildren();
            if (wordNode.isEnd()) {
                allMatches = true;
                break;
            }
        }
        return allMatches ? matchedIndex : 0;
    }

    private void generateSensitiveContentCheckHistory(String text, Set<String> sensitiveWordList) {
        SensitiveContentHitHistory sensitiveContentHitHistory = new SensitiveContentHitHistory();
        sensitiveContentHitHistory.setCheckedText(text);
        sensitiveContentHitHistory.setSensitiveContent(String.join(SENSITIVE_TEXT_DELIMITER, sensitiveWordList));
        sensitiveContentHitHistoryRepository.save(sensitiveContentHitHistory);
    }

}