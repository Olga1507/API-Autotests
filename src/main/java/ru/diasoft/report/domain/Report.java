package ru.diasoft.report.domain;

import lombok.Getter;
import lombok.Setter;
import ru.diasoft.report.domain.TestCaseLog;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Report {
    private final List<TestCaseLog> testCaseLogs;
    private String fileName;
    private Instant dateStart;
    private Instant dateEnd;

    public Report() {
        testCaseLogs = new ArrayList<>();
    }
}
