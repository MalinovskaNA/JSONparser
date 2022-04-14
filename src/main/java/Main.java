import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileNameCSV = "data.csv";
        String fileNameXML = "data.xml";
        String fileNameCsvToJson = "data.json";
        String fileNameXmlToJson = "data2.json";

        List<Employee> listCSV = parseCSV(columnMapping, fileNameCSV);
        String json = listToJson(listCSV);
        writeString(json, fileNameCsvToJson);

        List<Employee> listXML = parseXML(fileNameXML);
        String jsonXML = listToJson(listXML);
        writeString(jsonXML, fileNameXmlToJson);

        String jsonJSON = readString(fileNameCsvToJson);
        List<Employee> employeeList = jsonToList(jsonJSON);
        for (Employee employee : employeeList) {
            System.out.println(employee);
        }
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> list = null;
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            list = csv.parse();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Employee> parseXML(String fileName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        List<Employee> empList = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            empList = new ArrayList<>();
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    Employee emp = new Employee();
                    NodeList childNodes = elem.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node cNode = childNodes.item(j);
                        if (cNode.getNodeType() == Node.ELEMENT_NODE) {
                            String content = cNode.getLastChild().getTextContent().trim();

                            switch (cNode.getNodeName()) {
                                case "id":
                                    emp.id = Long.parseLong(content);
                                    break;
                                case "firstName":
                                    emp.firstName = content;
                                    break;
                                case "lastName":
                                    emp.lastName = content;
                                    break;
                                case "country":
                                    emp.country = content;
                                    break;
                                case "age":
                                    emp.age = Integer.parseInt(content);
                                    break;
                            }
                        }

                    }
                    empList.add(emp);
                }
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return empList;
    }

    public static <Employee> String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        String json = gson.toJson(list, listType);
        return json;
    }

    public static void writeString(String json, String fileName) {
        try (FileWriter file = new
                FileWriter(fileName)) {
            file.write(json);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readString(String fileName) {
        StringBuilder sb = new StringBuilder();
        String s;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((s = br.readLine()) != null) {
                sb = sb.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static List<Employee> jsonToList(String json) {
        List<Employee> employees = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray jsonArray = (JSONArray) parser.parse(json);
            for (Object el : jsonArray) {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                Employee em = gson.fromJson(String.valueOf(el), Employee.class);
                employees.add(em);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return employees;
    }
}
