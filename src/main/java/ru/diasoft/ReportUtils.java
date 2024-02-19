package ru.diasoft;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import ru.diasoft.report.domain.Report;
import ru.diasoft.report.domain.TestCaseLog;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReportUtils {
    //public static void main(String[] args) {
    public static void creatHtmlReport (Report report) throws Exception {

        // Подготовьте выходной путь для сохранения документа
        String documentPath = "reportPage1.html";

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db  = dbf.newDocumentBuilder();
        Document document = db.newDocument();

        Element html = document.createElement("html");
        html.setAttribute("lang", "ru");
        document.appendChild(html);

        Element head = document.createElement("head");
        html.appendChild(head);

        Element body = document.createElement("body");
        html.appendChild(body);


// Инициализировать пустой HTML-документ
        //Document document = new Document();

// Создайте элемент стиля и присвойте значения цвета border-style и border-color для элемента таблицы.
        Element style = document.createElement("style");
        style.setTextContent("TABLE {\n" +
                "    /*width: 300px; /* Ширина таблицы */\n" +
                "    background: #fffff0; /* Цвет фона нечетных строк */\n" +
                "    border: 1px solid #a52a2a; /* Рамка вокруг таблицы */\n" +
                "    border-collapse: collapse; /* Убираем двойные линии между ячейками */\n" +
                "   }\n" +
                "   TD, TH {\n" +
                "    padding: 3px; /* Поля вокруг содержимого ячейки */\n" +
                "    border:1px solid;\n" +
                "   }\n" +
                "   TD {\n" +
                "    text-align: left; /* Выравнивание по левому краю */\n" +
                "    border-bottom: 1px solid #5a1111; /* Линия внизу ячейки */\n" +
                "    \n" +
                "    \n" +
                "   }\n" +
                "   TH {\n" +
                "    text-align: center;\n" +
                "    background: #91c2ed; /* Цвет фона */\n" +
                "    color: white; /* Цвет текста */\n" +
                "   }\n" +
                "   TR {\n" +
                "    background: #fff8dc; \n" +
                "   }\n" +
                "\n" +
                "   TR.even {\n" +
                "    background: #f1f1ee; \n" +
                "   }\n" +
                "\n" +
                "   TR.fail {\n" +
                "    background: #ee5858; \n" +
                "   }\n" +
                "\n" +
                "   TR.success {\n" +
                "    background: #a5da7f; \n" +
                "   }");
        // Найдите элемент заголовка документа и добавьте элемент стиля в заголовок.
        //Element head = (Element)document.getElementsByTagName("head").item(0);
        head.appendChild(style);

// Объявите тело переменной, которая ссылается на<body> элемент


        Element h1 = document.createElement("h1");
        Text tit1 = document.createTextNode("Отчёт о выполнении тест-кейсов");
        h1.appendChild(tit1);

// Создать элемент таблицы
        Element table = document.createElement("table");
        table.setAttribute("style", "background-color:#00FF00;");

// Создать тело таблицы
        Element tbody = document.createElement("tbody");
        table.appendChild(tbody);

        //подсчёт успешных и проваленных тестов
        int cntSuccess = 0;
        int cntFailed = 0;


        for (int i = 0; i < report.getTestCaseLogs().size(); i++){
            if (report.getTestCaseLogs().get(i).isResult()){
                cntSuccess++;
            } else {
                cntFailed++;
            }
        }


// ТАБЛИЦА 1

        addToTitleTable("Дата запуска обработки:", String.valueOf(report.getDateStart()), tbody, document);
        addToTitleTable("Дата окончания обработки:", String.valueOf(report.getDateEnd()), tbody, document);
        addToTitleTable("Наименование файла с тестами:", report.getFileName(), tbody, document);

        addToTitleTable("Общее количество тестов:", String.valueOf(report.getTestCaseLogs().size()), tbody, document);
        addToTitleTable("Количество успешных тестов:", String.valueOf(cntSuccess), tbody, document);
        addToTitleTable("Количество проваленных тестов:", String.valueOf(cntFailed), tbody, document);

        Element hr = document.createElement("hr");
        hr.setAttribute("style", "opacity:0");

        Element caption = document.createElement("caption");
        Text capTxt = document.createTextNode("Детализация");
        caption.appendChild(capTxt);

// ТАБЛИЦА 2

// Создать элемент таблицы
        Element table2 = document.createElement("table");
        table2.setAttribute("style", "background-color:#00FF00;");

// Создать тело таблицы
        Element tbody2 = document.createElement("tbody");
        table2.appendChild(tbody2);

// Названия столбцов
        Element tbTr = document.createElement("tr");
        tbody2.appendChild(tbTr);
        Element th = document.createElement("th");
        Text title = document.createTextNode("Номер кейса");
        th.appendChild(title);
        tbTr.appendChild(th);

        Element th2 = document.createElement("th");
        Text title2 = document.createTextNode("URL запроса");
        th2.appendChild(title2);
        tbTr.appendChild(th2);

        Element th3 = document.createElement("th");
        Text title3 = document.createTextNode("Код ответа");
        th3.appendChild(title3);
        tbTr.appendChild(th3);

        Element th4 = document.createElement("th");
        Text title4 = document.createTextNode("Ошибки");
        th4.appendChild(title4);
        tbTr.appendChild(th4);

        Element th5 = document.createElement("th");
        Text title5 = document.createTextNode("Результат");
        th5.appendChild(title5);
        tbTr.appendChild(th5);


        for (int i=0; i < report.getTestCaseLogs().size(); i++){
            addLog(report.getTestCaseLogs().get(i), tbody2, document);

        }

// Добавить таблицу, h1 в тело
        //Element body = document.getDocumentElement();
        body.appendChild(h1);
        body.appendChild(table);
        body.appendChild(hr);
        body.appendChild(caption);
        body.appendChild(table2);

 // Сохраните документ на диск
        //document.save(documentPath);
        writeDocument(document, documentPath);

    }

    public static void addLog(TestCaseLog testCaseLog, Element tbody, Document document){
        Element clTr = document.createElement("tr");
        if (testCaseLog.isResult()){
            clTr.setAttribute("class", "success");
        } else {
            clTr.setAttribute("class", "fail");
        }

        tbody.appendChild(clTr);
        Element clTd = document.createElement("td");
        Text tdTx1 = document.createTextNode(String.valueOf(testCaseLog.getTestCaseNum()));
        clTd.appendChild(tdTx1);
        clTr.appendChild(clTd);

        Element clTd2 = document.createElement("td");
        Text tdTx2 = document.createTextNode(testCaseLog.getRequestUrl());
        clTd2.appendChild(tdTx2);
        clTr.appendChild(clTd2);

        Element clTd3 = document.createElement("td");
        Text tdTx3 = document.createTextNode(String.valueOf(testCaseLog.getRespCode()));
        clTd3.appendChild(tdTx3);
        clTr.appendChild(clTd3);


        if (testCaseLog.getErrors().isEmpty()) {
            Element clTd4 = document.createElement("td");
            Text tdTx4 = document.createTextNode("Ошибок нет");
            clTd4.appendChild(tdTx4);
            clTr.appendChild(clTd4);

        } else {
            Element clTd4 = document.createElement("td");
            Text tdTx4 = document.createTextNode(String.valueOf(testCaseLog.getErrors()));
            clTd4.appendChild(tdTx4);
            clTr.appendChild(clTd4);
        }

        if (testCaseLog.isResult()){
            Element clTd5 = document.createElement("td");
            Text tdTx5 = document.createTextNode("Успех");
            clTd5.appendChild(tdTx5);
            clTr.appendChild(clTd5);

        } else {
            Element clTd5 = document.createElement("td");
            Text tdTx5 = document.createTextNode("Провал");
            clTd5.appendChild(tdTx5);
            clTr.appendChild(clTd5);
        }
    }

    public static void addToTitleTable(String name, String value, Element tbody, Document document){
        Element tr1 = document.createElement("tr");
        tbody.appendChild(tr1);

        Element td1 = document.createElement("td");
        Text txTd1 = document.createTextNode(name);
        td1.appendChild(txTd1);
        tr1.appendChild(td1);

        Element td2 = document.createElement("td");
        Text txTd2 = document.createTextNode(value);
        td2.appendChild(txTd2);
        tr1.appendChild(td2);
    }

    /**
     * Процедура сохранения DOM в файл
     */
    private static void writeDocument(Document document, String path)
            throws TransformerFactoryConfigurationError
    {
        Transformer trf = null;
        DOMSource src = null;
        FileOutputStream fos = null;
        try {
            trf = TransformerFactory.newInstance()
                    .newTransformer();
            src = new DOMSource(document);
            fos = new FileOutputStream(path);

            StreamResult result = new StreamResult(fos);
            trf.transform(src, result);
        } catch (TransformerException e) {
            e.printStackTrace(System.out);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

}
