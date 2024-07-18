package com.github.jarryzhou.sensitivecontentfilter.service;


import com.github.jarryzhou.sensitivecontentfilter.dto.SensitiveContentCheckResult;

interface SensitiveContentCheckService{
    SensitiveContentCheckResult checkText(String text);
}
