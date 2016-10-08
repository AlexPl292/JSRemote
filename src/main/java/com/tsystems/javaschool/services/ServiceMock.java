package com.tsystems.javaschool.services;

import com.tsystems.javaschool.entities.Backer;

import javax.ejb.Stateless;

/**
 * Created by alex on 08.10.16.
 */
@Stateless(name = "mockService")
public class ServiceMock implements Service {
    /*
    Комментарии на русском языке для ясности
    TODO перевести комментариии на английский
    TODO и вообще удалить этот класс
     */

    // Заглушка без реальных rest запросов
    public Boolean setUp(Backer backer) {
        // Записываем "полученные" тарифы
        backer.addName("Tariff1");
        backer.addName("Tariff2");
        return true;
    }
}
