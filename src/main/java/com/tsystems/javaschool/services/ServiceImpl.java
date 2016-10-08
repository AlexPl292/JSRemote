package com.tsystems.javaschool.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.tsystems.javaschool.entities.Backer;

import javax.ejb.Stateless;
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
        Client client = Client.create();

        // Аутентификация с помощью Basic auth
        client.addFilter(new HTTPBasicAuthFilter(backer.getEmail(), backer.getPassword()));

        // Запрашиваем список тарифов по адресу "restUrl + /tariffs"
        WebResource webResource = client
                .resource(backer.getUrl()+"/tariffs");
        ClientResponse response = webResource.accept("application/json")
                .get(ClientResponse.class);

        // Проверка статуса
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }

        // Получение данных
        String output = response.getEntity(String.class);

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
}
