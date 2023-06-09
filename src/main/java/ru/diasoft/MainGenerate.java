package ru.diasoft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.swagger.v3.oas.models.OpenAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.diasoft.domain.*;
import ru.diasoft.domain.TestCase;

import java.util.*;

public class MainGenerate {

    //Логирование
    private static Logger logger = LoggerFactory.getLogger(MainGenerate.class);
    // static List<TestCase> testCaseList = new ArrayList<>();


    public static void main(String[] args) throws Exception {

//
//        //Преобразование из JSON в классы
//        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("data.json");
//        Root root = new ObjectMapper().readValue(inputStream, Root.class);
//
//
//        //Преобразование из классов в строку JSON
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();
//        String json = gson.toJson(root, Root.class);
//
//
//
//        System.out.println(json);

        //int k = 0;


/*
        // create a client
        HttpClient client = HttpClient.newHttpClient();

        // create a request
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY"))
                .header("accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
*/

        // parse a swagger description from the petstore and get the result
        //SwaggerParseResult result = new OpenAPIParser().readLocation("http://qmmmsgpackage.msghubtmp.qrun.diasoft.ru/qmmmsgpackage/v3/api-docs", null, null);
        SwaggerParseResult result = new OpenAPIParser().readLocation(MainGenerate.class.getClassLoader().getResource("api-docs.json").toString(), null, null);

        // or from a file
        //   SwaggerParseResult result = new OpenAPIParser().readLocation("./path/to/openapi.yaml", null, null);

        // the parsed POJO
        OpenAPI openAPI = result.getOpenAPI();
        //int i = 0;

//        if (result.getMessages() != null) {
//            result.getMessages().forEach(System.err::println); // validation errors and warnings
//        }

        //List<MethodAPI> methods = new ArrayList<>();

        AllCases allCases = new AllCases();
        List<TestCase> testCaseList = new ArrayList<>();
        if (openAPI != null) {
            Map<String, Schema> schemas = openAPI.getComponents().getSchemas();

            openAPI.getPaths().forEach((pathName, pathItem) -> {
                tryCreateTestCase(testCaseList, pathName, pathItem.getGet(), MethodTypes.GET, schemas);
                tryCreateTestCase(testCaseList, pathName, pathItem.getPut(), MethodTypes.PUT, schemas);
                tryCreateTestCase(testCaseList, pathName, pathItem.getPost(), MethodTypes.POST, schemas);
                tryCreateTestCase(testCaseList, pathName, pathItem.getDelete(), MethodTypes.DELETE, schemas);
            });

            //io.swagger.v3.oas.models.media.Schema schema = openAPI.getComponents().getSchemas().get("ModelAndView");
            // int i = 0;
        }
        allCases.setCases(testCaseList);
        Gson gson1 = new GsonBuilder().setPrettyPrinting().create();
        String json1 = gson1.toJson(allCases, AllCases.class);
        System.out.println(json1);
    }



//// отладочный метод
//    public static void main(String[] args) {
//        SchemaTypesExample schemaTypesExample = new SchemaTypesExample();
//
//        System.out.println(schemaTypesExample.getTypeExample("fghgf"));
//
//    }

    static SortedSet<String> possibleResponseCodes = new TreeSet<>();
    static {
        possibleResponseCodes.add("200");
        possibleResponseCodes.add("201");
        possibleResponseCodes.add("202");
        possibleResponseCodes.add("203");
        possibleResponseCodes.add("204");
        possibleResponseCodes.add("300");
        possibleResponseCodes.add("301");
        possibleResponseCodes.add("302");
        possibleResponseCodes.add("303");
        possibleResponseCodes.add("304");
        possibleResponseCodes.add("307");
        possibleResponseCodes.add("308");
        possibleResponseCodes.add("400");
        possibleResponseCodes.add("401");
        possibleResponseCodes.add("403");
    }

    /**
     * Формирует/не формирует тест-кейс на основе описания из swagger и добавляет его в List
     *
     * @param pathName
     * @param methodDescription
     * @param methodType
     * @param schemas
     * @return если что-то внутри упадет (например, methodDescription вернутся некорректный или схема), возвращается null
     */

    public static void tryCreateTestCase(List<TestCase> testCaseList, String pathName, Operation methodDescription, MethodTypes methodType, Map<String, Schema> schemas) {


        TestCase testCase = null;

        //если у метода есть параметры, то сохраним в мапу имя этого параметра и пример его значения
        if (methodDescription != null) {
            //создаем экземпляр класса в этом месте, так как у данного метода может не быть RequestBody
            testCase = new TestCase();
            testCase.setMethodPath(pathName);
            testCase.setMethodType(methodType);

            //определение как проверять наш ответ
            testCase.setResponseBodyCheckMode(ResponseBodyCheckMode.DECLARED_AND_ANY_TAGS);
            testCase.setResponseBodyValueCheckMode(ResponseBodyValueCheckMode.STRICT);
            testCase.setResponseCheckMode(ResponseCheckMode.RESPONSE_CODE_AND_BODY);


            //заполняем параметры метода
            Map<String, Object> parametersPath = new HashMap<>();
            Map<String, Object> parametersQuery = new HashMap<>();
            if (methodDescription.getParameters() != null) {
                methodDescription.getParameters().forEach(param -> {
                    if ("path".equals(param.getIn())){
                        parametersPath.put(param.getName(), ValueGenerator.generateValueByParameter(param, schemas));
                    } else if ("query".equals(param.getIn())){
                        parametersQuery.put(param.getName(), ValueGenerator.generateValueByParameter(param, schemas));
                    } else {
                        throw new IllegalArgumentException(String.format("В теге in параметра %s содержится некорректное значение: %s", param.getName(), param.getIn()));
                    }

                });
            }
            testCase.setParametersPath(parametersPath);
            testCase.setParametersQuery(parametersQuery);

            //если RequestBody не пустое
            if (methodDescription.getRequestBody() != null) {
                //то получаем content
                Content content = methodDescription.getRequestBody().getContent();
                testCase.setRequestBody(ValueGenerator.generateValueByContent(content, schemas));
            }

            //получение responseBody
            if (methodDescription.getResponses() != null) {
                ApiResponse apiResponse = null;
                for (String possibleResponseCode : possibleResponseCodes){
                    if (methodDescription.getResponses().containsKey(possibleResponseCode)){
                        apiResponse = methodDescription.getResponses().get(possibleResponseCode);
                        testCase.setResponseBody(ValueGenerator.generateValueByContent(apiResponse.getContent(), schemas));
                        testCase.setResponseCode(possibleResponseCode);
                        break;
                    }
                }

                if (apiResponse == null){
                    throw new IllegalArgumentException(String.format("Ни один из кодов ответа не соответствует possibleResponseCodes. Количество найденных кодов ответа: %s",
                            methodDescription.getResponses().size()));
                }
            }
        }
        if (testCase != null) {
            testCaseList.add(testCase);
        }
    }

}
