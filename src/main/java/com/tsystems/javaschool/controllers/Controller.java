package com.tsystems.javaschool.controllers;

import com.tsystems.javaschool.entities.Backer;
import com.tsystems.javaschool.services.Service;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 * Created by alex on 08.10.16.
 */
@ManagedBean
@SessionScoped
public class Controller {

    /*
    Комментарии на русском языке для ясности
    TODO перевести комментариии на английский
     */
    @EJB(beanName = "mockService")  // "realService" for real rest requests
    private Service service;

    private Backer backer = new Backer();

    // Проверка соединения, запрос тарифов
    public void setUp() {
        service.setUp(backer);
    }

    public void createAndLoadPdf() {
        // STUB
        System.out.println("X");  // Create and download pdf
    }

    public Backer getBacker() {
        return backer;
    }

    public void setBacker(Backer backer) {
        this.backer = backer;
    }
}
