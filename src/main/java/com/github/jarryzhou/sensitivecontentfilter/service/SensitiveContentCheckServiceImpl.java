package com.github.jarryzhou.sensitivecontentfilter.service;


import com.github.jarryzhou.sensitivecontentfilter.entity.SensitiveText;
import com.github.jarryzhou.sensitivecontentfilter.repository.SensitiveContentHitHistoryRepository;
import com.github.jarryzhou.sensitivecontentfilter.repository.SensitiveTextRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

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
public class SensitiveContentCheckServiceImpl implements InitializingBean {

    /**
     * ASCII表中除空格外的可见字符与对应的全角字符的相对偏移
     */
    public static final int CONVERT_STEP = 65248; // 全角半角转换间隔
    /**
     * 半角空格的值，在ASCII中为32(Decimal)
     */
    public static final char DBC_SPACE = ' '; // 半角空格
    /**
     * 全角对应于ASCII表的可见字符到～结束，偏移值为65374
     */
    public static final char SBC_CHAR_END = 65374; // 全角～
    /**
     * 全角对应于ASCII表的可见字符从！开始，偏移值为65281
     */
    public static final char SBC_CHAR_START = 65281; // 全角！
    /**
     * 全角空格的值，它没有遵从与ASCII的相对偏移，必须单独处理
     */
    public static final char SBC_SPACE = 12288; // 全角空格 12288
    private static final char SIGN = '*'; // 敏感词过滤替换
    @Autowired
    SensitiveTextRepository sensitiveTextRepository;
    @Autowired
    SensitiveContentHitHistoryRepository sensitiveContentHitHistoryRepository;
    private Map<Integer, WordNode> nodes; // 存储敏感词节点
    private Set<Integer> stopWordSet; // 停顿词
    //    private SensitiveWordsProvider sensitiveWordsProvider;
//    private SensitiveWordsProvider stopWordsProvider;
    private boolean enabled = true;
    private Set<String> stopWords;

    /**
     * 过滤判断 将敏感词转化为成屏蔽词
     */
    public final String checkText(final String textContent) {
        if (!isEnabled()) {
            return textContent;
        }
        if (nodes == null || nodes.isEmpty()) {
            return textContent;
        }
        if (textContent == null || textContent.isEmpty()) {
            return textContent;
        }
        String src = internalReplacement(textContent);
        // 把文本拆分成字符
        char[] chs = src.toCharArray();
        int length = chs.length;
        int currc; // 当前检查的字符
        int cpcurrc; // 当前检查字符的备份
        int k;
        WordNode node;
        // 遍历字符
        for (int i = 0; i < length; i++) {
            // 获得当前检查字符
            currc = charConvert(chs[i]);
            // 如果敏感首字中不包含当前字符，直接跳过
            if (!nodes.containsKey(currc)) {
                continue;
            }
            // 如果命中敏感首字，查询首字对应的敏感词树
            node = nodes.get(currc);// 日 2
            // 如果敏感词数为空，跳过，一般不会有这种情况
            if (node == null) {
                continue;
            }
            boolean couldMark = false;
            int markNum = -1;
            // 单字匹配（日）
            if (node.isLast()) {
                couldMark = true;
                markNum = 0;
            }
            // 继续匹配（日你/日你妹），以长的优先
            // 你-3 妹-4 夫-5
            k = i;
            cpcurrc = currc; // 当前字符的拷贝
            while (++k < length) {
                int temp = charConvert(chs[k]);
                if (temp == cpcurrc) {
                    continue;
                }
                //如果后面是停顿词，表示句子完了，跳过
                if (stopWordSet != null && stopWordSet.contains(temp)) {
                    continue;
                }
                //在子节点中找字符对应的节点
                node = node.querySub(temp);
                if (node == null) {
                    break;
                }
                // 如果找到的子节点是敏感词结束，那么就匹配了
                if (node.isLast()) {
                    couldMark = true;
                    markNum = k - i;// 3-2
                }
                cpcurrc = temp;
            }

            // 如果找到了，提取出来
            if (couldMark) {
                for (k = 0; k <= markNum; k++) {
                    chs[k + i] = SIGN;
                }
                i = i + markNum;
            }
        }
        return new String(chs);

    }

    private String internalReplacement(String src) {
        if (src == null || src.isEmpty()) {
            return "";
        }

        // 以下为例子
        return src.replaceAll("傻逼", "笨笨").replaceAll("傻B", "笨笨").replaceAll("傻缺", "笨笨").replaceAll("煞笔", "笨笨").replaceAll("煞逼", "笨笨").replaceAll("傻笔", "笨笨").replaceAll("傻x", "笨笨").replaceAll("傻\\*", "笨笨");
    }

    /**
     * 大写转化为小写 全角转化为半角
     */
    private int charConvert(char src) {
        int r = src;
        if (src >= SBC_CHAR_START && src <= SBC_CHAR_END) { // 如果位于全角！到全角～区间内
            r = src - CONVERT_STEP;
        } else if (src == SBC_SPACE) { // 如果是全角空格
            r = DBC_SPACE;
        }
        return (r >= 'A' && r <= 'Z') ? r + 32 : r;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 是否包含敏感词
     */
    public final boolean contains(final String src) {
        if (nodes != null) {
            char[] chs = src.toCharArray();
            int length = chs.length;
            int currc; // 当前检查的字符
            int cpcurrc; // 当前检查字符的备份
            int k;
            WordNode node;
            for (int i = 0; i < length; i++) {
                currc = charConvert(chs[i]);
                if (!nodes.containsKey(currc)) {
                    continue;
                }
                node = nodes.get(currc);// 日 2
                if (node == null)// 其实不会发生，习惯性写上了
                    continue;
                boolean couldMark = node.isLast();
                // 单字匹配（日）
                // 继续匹配（日你/日你妹），以长的优先
                // 你-3 妹-4 夫-5
                k = i;
                cpcurrc = currc;
                while (++k < length) {
                    int temp = charConvert(chs[k]);
                    if (temp == cpcurrc) continue;
                    if (stopWordSet != null && stopWordSet.contains(temp)) continue;
                    node = node.querySub(temp);
                    if (node == null) {
                        break;
                    }// 没有了

                    if (node.isLast()) {
                        couldMark = true;
                    }
                    cpcurrc = temp;
                }
                if (couldMark) {
                    return true;
                }
            }
        }

        return false;
    }

    //初始化后开始加载词库到redis
    @Override
    public void afterPropertiesSet() throws Exception {
        addSensitiveWord(getSensitiveText());
        addStopWord(stopWords);
    }

    /**
     * 增加停顿词
     */
    private void addStopWord(final Set<String> words) {
        if (words != null && !words.isEmpty()) {
            char[] chs;
            for (String curr : words) {
                chs = curr.toCharArray();
                for (char c : chs) {
                    stopWordSet.add(charConvert(c));
                }
            }
        }
    }

    /**
     * 将敏感词汇拆分为DFA节点
     */
    protected void addSensitiveWord(final List<String> words) {
        if (words == null || words.isEmpty()) {
            return;
        }
        char[] chs;
        int fchar;
        int lastIndex;
        WordNode fnode; // 首字母节点
        for (String curr : words) {
            if (curr == null || curr.isEmpty()) {
                continue;
            }
            chs = curr.toCharArray();
            fchar = charConvert(chs[0]);
            fnode = nodes.get(fchar);
            if (fnode == null) {
                fnode = new WordNode(fchar, chs.length == 1);
                nodes.put(fchar, fnode);
            } else {
                // 如果已包含该首字
                if (!fnode.isLast() && chs.length == 1) fnode.setLast(true);
            }
            lastIndex = chs.length - 1;
            for (int i = 1; i < chs.length; i++) {
                fnode = fnode.addIfNoExist(charConvert(chs[i]), i == lastIndex);
            }
        }
    }

    private List<String> getSensitiveText() {
        return sensitiveTextRepository.findAll().stream().map(SensitiveText::getContent).collect(Collectors.toList());
    }

}