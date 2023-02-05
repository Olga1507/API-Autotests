package ru.diasoft.domain;

import java.util.ArrayList;
import java.util.List;

public class AllCases {
    private List<TestCase> cases = new ArrayList<>();

    private String serverUrl;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public List<TestCase> getCases() {
        return cases;
    }

    public void setCases(List<TestCase> cases) {
        this.cases = cases;
    }

    //    public List<Data> getCases() {
//        return Collections.unmodifiableList(cases); //чтобы пользователь не смог испортить
//    }
//
//    public void addCase(Data data) {
//        cases.add(data);
//    }
}
