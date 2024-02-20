package ru.diasoft;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.diasoft.domain.TestCase;
import ru.diasoft.domain.AllCases;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.diasoft.report.ReportUtils;
import ru.diasoft.report.domain.Report;
import ru.diasoft.report.domain.TestCaseLog;


public class MainExecute {
    //Логирование
    private static Logger logger = LoggerFactory.getLogger(MainExecute.class);
    private static final String fileName = "firstTest.json";

    public static void main(String[] args) throws Exception {
        //Создали переменную для отчёта
        Report report = new Report();
        //Сохранили в отчёт имя файла с кейсами
        report.setFileName(fileName);
        //Записали текущее время
        report.setDateStart(Instant.now());

        //Преобразование из JSON в классы
        InputStream inputStream = MainExecute.class.getClassLoader().getResourceAsStream(fileName);
        AllCases allCases = new ObjectMapper().readValue(inputStream, AllCases.class);
        logger.info("Считан файл с исходными данными. Тест-кейсов: {}", allCases.getCases().size());

        //int correctStatusCode = 200;

        for (int i = 0; i < allCases.getCases().size(); i++) {
            //Заполнение отчёта
            TestCaseLog testCaseLog = new TestCaseLog();
            report.getTestCaseLogs().add(testCaseLog);
            testCaseLog.setTestCaseNum(i);

            logger.info("Начали обработку тест-кейса {}", i);
            boolean result = true;
            try {
                TestCase case0 = allCases.getCases().get(i);
                String correctUrl = getUrl(case0);


                //System.out.println(correctUrl);

                String resultUrl = allCases.getServerUrl().concat(correctUrl);
                logger.info("URL для запроса: {}", resultUrl);
                //Записали URL для отчёта
                testCaseLog.setRequestUrl(resultUrl);

                //Формируем body (маршаллинг)
                ObjectMapper mapper = new ObjectMapper();

                //Java Object -> JSON
                String requestBodyString = mapper.writeValueAsString(case0.getRequestBody());
                logger.info("Request Body: {}", requestBodyString);


                HttpResponse<String> response = sendRequest(resultUrl, case0.getMethodType(), requestBodyString);

                logger.info("Получен ответ. Код: {}. Тело ответа: {}", response.statusCode(), response.body());

                //Для отчёта
                testCaseLog.setResponseBody(response.body());

                //Записали код для отчёта

                testCaseLog.setRespCode(response.statusCode());

                if (response.statusCode() != case0.getResponseCode()) {
                    logger.error("Ошибка валидации ответа. Получен некорректный статус код: {}", response.statusCode());
                    result = false;
                    testCaseLog.getErrors().add("Ошибка валидации ответа. Получен некорректный статус код: " + response.statusCode());
                } else {
                    logger.info("Получен корректный статус - код: {}", response.statusCode());
                }

                logger.info("Начали валидацию тела ответа из expectedObjects");

                //Для кpacивoго oфopмлeния JSON cтpoки
                //ObjectMapper mapper = new ObjectMapper(); - создали выше!
                JsonNode responseJson = mapper.readValue(response.body(), JsonNode.class);
                //JsonNode node1 = mapper.valueToTree(jsonObject);


                //Валидация

                try {
                    validateResponse(responseJson, case0);
                } catch (RuntimeException e) {
                    logger.error("Ошибка валидации: " + e.getMessage());
                    result = false;
                    testCaseLog.getErrors().add("Ошибка валидации: " + e.getMessage());
                }


            } catch (Exception e) {
                logger.error("Ошибка: {}", e.getMessage());
                result = false;
                testCaseLog.getErrors().add("Ошибка: " + e.getMessage());

            }


            logger.info("Закончили обработку тест-кейса {}", i);
            testCaseLog.setResult(result);

        }
        logger.info("Обработка завершена.");
        report.setDateEnd(Instant.now());

        logger.info("Начали формирование отчёта");

        ReportUtils.creatHtmlReport(report);

        logger.info("Отчёт готов!");


    }


    // ВАЛИДАЦИЯ
    // create object mapper instance
    static ObjectMapper mapper = new ObjectMapper();

    public static void validateResponse(JsonNode responseJson, TestCase case0) {
        validateObjects(responseJson, mapper.valueToTree(case0.getExpectedObjects()), true);
        validateObjects(responseJson, mapper.valueToTree(case0.getUnexpectedObjects()), false);
        logger.info("Валидация прошла успешно!");
    }

    private static void validateObjects(JsonNode responseJson, JsonNode objects, boolean expected) {
        if (objects != null) {
            objects.fields().forEachRemaining(stringJsonNodeEntry -> {
                String key = stringJsonNodeEntry.getKey();
                JsonNode expectedValue = stringJsonNodeEntry.getValue();
                JsonNode founded = responseJson.get(key);

                if (expected) {
                    validateExpectedObject(key, expectedValue, founded);
                } else {
                    validateUnexpectedObject(key, expectedValue, founded);
                }

                // Рекурсивный вызов для вложенных объектов
                if (founded != null && founded.isObject()) {
                    validateObjects(founded, expectedValue, expected);
                }
            });
        }
    }

    private static void validateExpectedObject(String key, JsonNode expectedValue, JsonNode founded) {
        if (founded == null) {
            throw new RuntimeException("Не найдено обязательное поле в теле ответа: " + key);
        }
        if (!expectedValue.equals(founded)) {
            throw new RuntimeException(String.format(
                    "Получили ошибку при сравнении тега '%s': полученное значение '%s' не совпало с ожидаемым '%s'",
                    key, founded, expectedValue));
        }
    }

    private static void validateUnexpectedObject(String key, JsonNode expectedValue, JsonNode founded) {
        // Проверка отсутствия неожиданного поля
        if (founded != null && expectedValue.equals(founded)) {
            throw new RuntimeException(String.format(
                    "В теле ответа не должно быть тега '%s' с значением '%s'",
                    key, founded));
        }
    }

    //

    public static String getUrl(TestCase testCase) throws Exception {
        String regex = "\\{([^}]*)\\}";
        //скомпиллировали регулярное выражение
        Pattern pattern = Pattern.compile(regex);


        //Подготовка поиска в testStr
        Matcher matcher = pattern.matcher(testCase.getMethodPath());
        List<String> errors = new ArrayList<>();
        String res = matcher.replaceAll(matchResult -> {
            //return "hello" + " " + matchResult.group(1);
            if (testCase.getParametersPath().get(matchResult.group(1)) != null) {
                return String.valueOf(testCase.getParametersPath().get(matchResult.group(1)));
            } else {
                errors.add("Не найдено значение параметра: " + matcher.group());
                logger.error("Не найдено значение параметра: " + matcher.group());
                //throw new Exception("Не найдено значение параметра: " + matcher.group());
                return matcher.group();
            }

        });
        if (errors.size() > 0) {
            throw new Exception("Ошибка при формировании URL");
        }
        return res;
    }


    public static HttpResponse<String> sendRequest(String url, MethodTypes type, String requestBody) throws IOException, InterruptedException {
        // create a client
        HttpClient client = HttpClient.newHttpClient();

        // create a request
        HttpRequest.Builder tmp = HttpRequest.newBuilder(URI.create(url))
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + getAuthToken());

        HttpRequest request;

        switch (type) {

            case GET:
                request = tmp.GET().build();
                break;
            //case POST: request = tmp.POST(HttpRequest.BodyPublishers.noBody()).build(); break;
            case POST:
                request = tmp.POST(HttpRequest.BodyPublishers.ofString(requestBody)).build();
                break;
            case PUT:
                request = tmp.PUT(HttpRequest.BodyPublishers.ofString(requestBody)).build();
                break;
            case DELETE:
                request = tmp.DELETE().build();
                break;

            default:
                throw new IllegalArgumentException("Некорректное значение MethodType: " + String.valueOf(type));
        }


        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

    public static String getAuthToken() throws IOException, InterruptedException {
        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", "password");
        formData.put("scope", "openid");
        formData.put("username", "dqtech");
        formData.put("password", "12345678");

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create("http://mdpauth.msghubtmp.qrun.diasoft.ru/mdpauth/oauth/token"))
                .POST(HttpRequest.BodyPublishers.ofString(getFormDataAsString(formData)))
                .header("Authorization", "Basic " +
                        Base64.getEncoder().encodeToString(("client:secret").getBytes()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String authToken = new JSONObject(response.body()).getString("access_token");

        return authToken;
    }

    private static String getFormDataAsString(Map<String, String> formData) {
        StringBuilder formBodyBuilder = new StringBuilder();
        for (Map.Entry<String, String> singleEntry : formData.entrySet()) {
            if (formBodyBuilder.length() > 0) {
                formBodyBuilder.append("&");
            }
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getKey(), StandardCharsets.UTF_8));
            formBodyBuilder.append("=");
            formBodyBuilder.append(URLEncoder.encode(singleEntry.getValue(), StandardCharsets.UTF_8));
        }
        return formBodyBuilder.toString();
    }


}
