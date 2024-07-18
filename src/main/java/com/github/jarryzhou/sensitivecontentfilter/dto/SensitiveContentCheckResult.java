package com.github.jarryzhou.sensitivecontentfilter.dto;

import java.sql.Date;

public class SensitiveContentCheckResult{
    boolean sensitive;
    String type; // 目前只有文本检查一种，可省略
    String source;
    Date checkTime;
    String originalContent;
    String[] sensitiveContent;
}