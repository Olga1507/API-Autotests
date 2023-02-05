package ru.diasoft.domain;

import ru.diasoft.MethodTypes;
import java.util.Map;

public class TestCase {
    private String caseName;
    private String methodPath;
    private MethodTypes methodType;
    private Object requestBody;
    private Map<String, Object> parametersPath;
    private Map<String, Object> parametersQuery;
    private Object responseBody;
    private int responseCode;


    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public String getMethodPath() {
        return methodPath;
    }

    public void setMethodPath(String methodPath) {
        this.methodPath = methodPath;
    }

    public MethodTypes getMethodType() {
        return methodType;
    }

    public void setMethodType(MethodTypes methodType) {
        this.methodType = methodType;
    }

    public Object getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    public Map<String, Object> getParametersPath() {
        return parametersPath;
    }

    public void setParametersPath(Map<String, Object> parametersPath) {
        this.parametersPath = parametersPath;
    }

    public Map<String, Object> getParametersQuery() {
        return parametersQuery;
    }

    public void setParametersQuery(Map<String, Object> parametersQuery) {
        this.parametersQuery = parametersQuery;
    }

    public Object getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(Object responseBody) {
        this.responseBody = responseBody;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}




