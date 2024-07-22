package io.github.jarryzhou.sensitivecontentfilter.service;

import java.util.HashMap;
import java.util.Map;

public class WordNode {

    private boolean isEnd;
    private Map<Character, WordNode> children;  // Assuming ASCII

    public WordNode() {
        this.isEnd = false;
    }

    public void putChildIfAbsent(Character character, WordNode wordNode) {
        if (children == null) {
            children = new HashMap<>();
        }
        children.putIfAbsent(character, wordNode);
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public Map<Character, WordNode> getChildren() {
        return children;
    }

}
