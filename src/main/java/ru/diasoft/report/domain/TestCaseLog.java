package ru.diasoft.report.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TestCaseLog {
    private int testCaseNum;
    private boolean result;
    private String requestUrl;
    private int respCode;
    private final List<String> errors;
    private String requestBody;
    private String responseBody;

    public TestCaseLog() {
        errors = new ArrayList<>();
        result = false;
    }
}
