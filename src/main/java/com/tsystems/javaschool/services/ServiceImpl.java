package com.tsystems.javaschool.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.tsystems.javaschool.entities.Backer;

import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by alex on 08.10.16.
 */
@Stateless
public class ServiceImpl implements Service {

    /*
    Комментарии на русском языке для ясности
    TODO перевести комментариии на английский
     */
    public Boolean logIn(Backer backer) {
        String output = request(backer, "/tariffs");

        if (output == null)
            return false;

        // Записываем список тарифов в backer
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(output);
        } catch (IOException e) {
            return false;
        }
        backer.setTariffNames(new ArrayList<>());
        for (JsonNode node : rootNode) {
            backer.addName(node.get("name").asText());
        }

        return true;
    }

    public byte[] generate(Backer backer) {
        String output = request(backer, "/contracts?tariff="+backer.getChosenTariff());
        if (output == null)
            return null;

        Map<JsonNode, List<JsonNode>> nodes = prepareOutput(output);
        if (nodes == null)
            return null;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4, 36f, 72f, 40f, 30f);
        Font fontBig = new Font(Font.getFamily("Arial"), 30.0f, Font.BOLD);
        Font fontName = new Font(Font.getFamily("Arial"), 24.0f, Font.BOLD);
        Font fontMedium = new Font(Font.getFamily("Arial"), 12.0f, Font.NORMAL);
        Font fontMediumBold = new Font(Font.getFamily("Arial"), 12.0f, Font.BOLD);
        LineSeparator ls = new LineSeparator();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Tariff report", fontBig));
            document.add(new Paragraph(Chunk.NEWLINE));
            document.add(new Paragraph("Tariff: " + backer.getChosenTariff(), fontMedium));
            document.add(new Chunk(ls));
            document.add(new Paragraph(Chunk.NEWLINE));

            if (nodes.isEmpty()) {
                document.add(new Paragraph("No contracts with chosen tariff"));
                document.close();
                return out.toByteArray();
            }

            for (Map.Entry<JsonNode, List<JsonNode>> node : nodes.entrySet()) {
                document.add(new Paragraph(node.getKey().get("surname").asText() +
                        " " +node.getKey().get("name").asText(), fontName));
                document.add(new Paragraph(Chunk.NEWLINE));
                PdfPTable head = new PdfPTable(4);
                head.setWidthPercentage(100);
                head.setSpacingBefore(5);
                head.setSpacingAfter(5);
                float[] columnWidths = new float[]{5f, 20f, 15f, 10f};
                head.setWidths(columnWidths);

                PdfPCell cellId = new PdfPCell(new Paragraph("Id", fontMediumBold));
                cellId.setPadding(5);
                head.addCell(cellId);

                PdfPCell cellEmail = new PdfPCell(new Paragraph("Email", fontMediumBold));
                cellEmail.setPadding(5);
                head.addCell(cellEmail);

                PdfPCell cellAddress = new PdfPCell(new Paragraph("Address", fontMediumBold));
                cellAddress.setPadding(5);
                head.addCell(cellAddress);

                PdfPCell cellContsCount = new PdfPCell(new Paragraph("Contracts count", fontMediumBold));
                cellContsCount.setPadding(5);
                head.addCell(cellContsCount);

                PdfPCell cellIdVal = new PdfPCell(new Paragraph(node.getKey().get("id").asText()));
                cellIdVal.setPadding(5);
                head.addCell(cellIdVal);

                PdfPCell cellEmailVal = new PdfPCell(new Paragraph(node.getKey().get("email").asText()));
                cellEmailVal.setPadding(5);
                head.addCell(cellEmailVal);

                PdfPCell cellAddressVal = new PdfPCell(new Paragraph(node.getKey().get("address").asText()));
                cellAddressVal.setPadding(5);
                head.addCell(cellAddressVal);

                PdfPCell cellContsVal = new PdfPCell(new Paragraph(node.getValue().size()+""));
                cellContsVal.setPadding(5);
                head.addCell(cellContsVal);

                document.add(head);

/*                document.add(new Paragraph("Id: "+node.getKey().get("id").asText()));
                document.add(new Paragraph("Email: "+node.getKey().get("email").asText()));
                document.add(new Paragraph("Address: "+node.getKey().get("address").asText()));*/
                document.add(new Paragraph(Chunk.NEWLINE));
                document.add(new Paragraph("Contracts: ", fontMediumBold));
                document.add(new Paragraph(Chunk.NEWLINE));

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setSpacingBefore(5);
                table.setSpacingAfter(5);
                PdfPCell cellNumber = new PdfPCell(new Paragraph("Number", fontMediumBold));
                cellNumber.setPadding(5);
                table.addCell(cellNumber);

                PdfPCell cellBlock = new PdfPCell(new Paragraph("Blocked", fontMediumBold));
                cellBlock.setPadding(5);
                table.addCell(cellBlock);

                PdfPCell cellBalance = new PdfPCell(new Paragraph("Balance", fontMediumBold));
                cellBalance.setPadding(5);
                table.addCell(cellBalance);

                PdfPCell cellOptions = new PdfPCell(new Paragraph("Options", fontMediumBold));
                cellOptions.setPadding(5);
                table.addCell(cellOptions);

                for (JsonNode contract : node.getValue()) {
                    PdfPCell cellNumberVal = new PdfPCell(new Paragraph(contract.get("number").asText()));
                    cellNumberVal.setPadding(5);
                    table.addCell(cellNumberVal);

                    String blocked = "";
                    if (contract.get("isBlocked").asInt() == 0)
                        blocked = "non blocked";
                    else if (contract.get("isBlocked").asInt() == 1)
                        blocked = "blocked by customer";
                    else if (contract.get("isBlocked").asInt() == 2)
                        blocked = "blocked by eCare";

                    PdfPCell cellBlockVal = new PdfPCell(new Paragraph(blocked));
                    cellBlockVal.setPadding(5);
                    table.addCell(cellBlockVal);

                    PdfPCell cellBalanceVal = new PdfPCell(new Paragraph(String.format( "%.2f", contract.get("balance").asDouble()) + " \u20BD"));
                    cellBalanceVal.setPadding(5);
                    table.addCell(cellBalanceVal);

                    Iterator<JsonNode> optIterator = contract.get("usedOptions").elements();
                    com.itextpdf.text.List usedOptions = new com.itextpdf.text.List(10);
                    usedOptions.setListSymbol("\u2022");
                    if (!optIterator.hasNext()) {
                        document.add(new Paragraph("Options aren't used"));
                    }
                    while (optIterator.hasNext()) {
                        JsonNode option = optIterator.next();
                        usedOptions.add(new ListItem(option.get("name").asText(), fontMedium));
                    }

                    PdfPCell optionCell = new PdfPCell();
                    optionCell.addElement(usedOptions);
                    optionCell.setPadding(5);
                    table.addCell(optionCell);
                }
                document.add(table);
                document.add(new Paragraph(Chunk.NEWLINE));
                document.add(new Chunk(ls));
                document.newPage();
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

        String restUrl;
        try {
            ResourceBundle rb = ResourceBundle.getBundle("bundle");
            restUrl = rb.getString("restUrl");
        } catch (MissingResourceException e) {
            restUrl = "http://localhost:8080/JavaSchool/rest";
        }

        // Запрашиваем список тарифов по адресу "restUrl + /tariffs"
        WebResource webResource = client
                .resource(restUrl+resource);
        ClientResponse response = webResource.accept("application/json")
                .get(ClientResponse.class);

        // Проверка статуса
        if (response.getStatus() != 200) {
            return null;
        }

        // Получение данных
        return response.getEntity(String.class);
    }

    private Map<JsonNode, java.util.List<JsonNode>> prepareOutput(String output) {
        ObjectMapper mapper = new ObjectMapper();
        Map<JsonNode, java.util.List<JsonNode>> res = new HashMap<>();
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
            return null;
        }
        return res;
    }
}
