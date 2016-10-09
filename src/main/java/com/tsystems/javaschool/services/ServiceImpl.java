package com.tsystems.javaschool.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.tsystems.javaschool.entities.Backer;

import javax.ejb.Stateless;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 08.10.16.
 */
@Stateless(name = "realService")
public class ServiceImpl implements Service {

    /*
    Комментарии на русском языке для ясности
    TODO перевести комментариии на английский
     */
    public Boolean setUp(Backer backer) {
        String output = request(backer, "/tariffs");

        // Записываем список тарифов в backer
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readTree(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (JsonNode node : rootNode) {
            backer.addName(node.get("name").asText());
        }

        return true;
    }

    public byte[] generate(Backer backer) {
        String output = request(backer, "/contracts?tariff="+backer.getChosenTariff());
        Map<JsonNode, List<JsonNode>> nodes = prepareOutput(output);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4, 36f, 72f, 108f, 180f);
        Font fontBig = new Font(Font.getFamily("Arial"), 30.0f, Font.BOLD);
        Font fontName = new Font(Font.getFamily("Arial"), 24.0f, Font.BOLD);
        Font fontMedium = new Font(Font.getFamily("Arial"), 12.0f, Font.NORMAL);
        LineSeparator ls = new LineSeparator();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Tariff report", fontBig));
            document.add(new Paragraph(Chunk.NEWLINE));
            document.add(new Paragraph("Tariff: " + backer.getChosenTariff(), fontMedium));
            document.add(new Chunk(ls));
            document.add(new Paragraph(Chunk.NEWLINE));
            for (Map.Entry<JsonNode, List<JsonNode>> node : nodes.entrySet()) {
                document.add(new Paragraph(node.getKey().get("surname").asText() +
                        " " +node.getKey().get("name").asText() +
                        " ("+ node.getValue().size()+" contract(s))", fontName));
                document.add(new Paragraph("Id: "+node.getKey().get("id").asText()));
                document.add(new Paragraph("Email: "+node.getKey().get("email").asText()));
                document.add(new Paragraph("Address: "+node.getKey().get("address").asText()));
                document.add(new Paragraph(Chunk.NEWLINE));
                document.add(new Paragraph("Contracts: "));
                for (JsonNode contract : node.getValue()) {
                    String SMALL_LEADING = "    ";
                    String blocked = "";
                    if (contract.get("isBlocked").asInt() == 0)
                        blocked = "non blocked";
                    else if (contract.get("isBlocked").asInt() == 1)
                        blocked = "blocked by customer";
                    else if (contract.get("isBlocked").asInt() == 2)
                        blocked = "blocked by eCare";

                    document.add(new Paragraph(SMALL_LEADING+contract.get("number").asText()+
                        " - " + blocked));
                    document.add(new Paragraph(SMALL_LEADING+"Balance: "+String.format( "%.2f", contract.get("balance").asDouble())+" \u20BD"));
                    document.add(new Paragraph(SMALL_LEADING+Chunk.NEWLINE));
                }
                document.add(new Chunk(ls));
                document.add(new Paragraph(Chunk.NEWLINE));
            }
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    private String request(Backer backer, String resource) {
        Client client = Client.create();

        // Аутентификация с помощью Basic auth
        client.addFilter(new HTTPBasicAuthFilter(backer.getEmail(), backer.getPassword()));

        // Запрашиваем список тарифов по адресу "restUrl + /tariffs"
        WebResource webResource = client
                .resource(backer.getUrl()+resource);
        ClientResponse response = webResource.accept("application/json")
                .get(ClientResponse.class);

        // Проверка статуса
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }

        // Получение данных
        return response.getEntity(String.class);
    }

    private Map<JsonNode, List<JsonNode>> prepareOutput(String output) {
        ObjectMapper mapper = new ObjectMapper();
        Map<JsonNode, List<JsonNode>> res = new HashMap<>();
        try {
            JsonNode rootNode = mapper.readTree(output);
            for (JsonNode node : rootNode) {
                if (res.containsKey(node.get("customer")))
                    res.get(node.get("customer")).add(node);
                else {
                    List<JsonNode> tmp = new ArrayList<>();
                    tmp.add(node);
                    res.put(node.get("customer"), tmp);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
