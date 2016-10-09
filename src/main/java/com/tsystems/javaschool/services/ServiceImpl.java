package com.tsystems.javaschool.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfWriter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.tsystems.javaschool.entities.Backer;

import javax.ejb.Stateless;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
        String output = request(backer, "/contracts?tariff="+backer.getChoosedTariff());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4, 36f, 72f, 108f, 180f);
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph(
                    "Hello World! Hello People! " +
                            "Hello Sky! Hello Sun! Hello Moon! Hello Stars!"));
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
/*        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            Anchor anchorTarget = new Anchor("First page of the document.");
            anchorTarget.setName("BackToTop");
            Paragraph paragraph1 = new Paragraph();

            paragraph1.setSpacingBefore(50);

            paragraph1.add(anchorTarget);

            document.add(paragraph1);
            document.add(new Paragraph("Some more text on the "+

                    "first page with different color and font type.",

                    FontFactory.getFont(FontFactory.COURIER, 14, Font.BOLD, new CMYKColor(0, 255, 0, 0))));
        } catch (DocumentException e) {
            e.printStackTrace();
        }*/
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
}
