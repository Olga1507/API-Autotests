package ru.diasoft;


import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.diasoft.domain.TestCase;
import ru.diasoft.domain.AllCases;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainExecute {
    //Логирование
    private static Logger logger = LoggerFactory.getLogger(MainExecute.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        //Преобразование из JSON в классы
        InputStream inputStream = MainExecute.class.getClassLoader().getResourceAsStream("firstTest.json");
        AllCases allCases = new ObjectMapper().readValue(inputStream, AllCases.class);
        logger.info("Считан файл с исходными данными. Тест-кейсов: {}", allCases.getCases().size());

        int correctStatusCode = 200;

        for (int i = 0; i < allCases.getCases().size(); i++) {
            logger.info("Начали обработку тест-кейса {}", i);
            try {
                TestCase case0 = allCases.getCases().get(i);
                String correctUrl = getUrl(case0);

                //System.out.println(correctUrl);

                String resultUrl = allCases.getServerUrl().concat(correctUrl);
                HttpResponse<String> response = sendRequest(resultUrl, case0.getMethodType());

                //Для кpacивoго oфopмлeния JSON cтpoки
                ObjectMapper mapper = new ObjectMapper();
                Object jsonObject = mapper.readValue(response.body(), Object.class);


                System.out.println(resultUrl);
                System.out.println("http-code: " + response.statusCode());
                System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject));

                if (response.statusCode() != correctStatusCode) {
                    logger.error("Ошибка валидации ответа. Получен некорректный статус код: {}", response.statusCode());
                }
                else {
                    logger.info("Получен корректный статус - код: {}", response.statusCode());
                }
            } catch (Exception e) {
                logger.error("Ошибка: {}", e.getMessage());
            }

            logger.info("Закончили обработку тест-кейса {}", i);


        }
        logger.info("Обработка завершена.");

    }

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

    public static HttpResponse<String> sendRequest(String url, MethodTypes type) throws IOException, InterruptedException {
        // create a client
        HttpClient client = HttpClient.newHttpClient();

        // create a request
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response;
    }

}
