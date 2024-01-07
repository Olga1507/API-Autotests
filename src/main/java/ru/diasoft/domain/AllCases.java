package ru.diasoft.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AllCases {
    private List<TestCase> cases = new ArrayList<>();

    private String serverUrl;
}
