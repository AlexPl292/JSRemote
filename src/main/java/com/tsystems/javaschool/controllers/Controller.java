package com.tsystems.javaschool.controllers;

import com.tsystems.javaschool.entities.Backer;
import com.tsystems.javaschool.services.Service;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

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
    @EJB
    private Service service;

    private Backer backer = new Backer();

    // Проверка соединения, запрос тарифов
    public void setUp() {
        service.setUp(backer);
    }

    public void createAndLoadPdf() {
        byte[] pdfData = service.generate(backer);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();


        // Initialize response.
        response.reset(); // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
        response.setContentType("application/pdf"); // Check http://www.iana.org/assignments/media-types for all types. Use if necessary ServletContext#getMimeType() for auto-detection based on filename.
        response.setHeader("Content-disposition", "attachment; filename=\"name.pdf\""); // The Save As popup magic is done here. You can give it any filename you want, this only won't work in MSIE, it will use current request URL as filename instead.

        // Write file to response.
        OutputStream output = null;
        try {
            output = response.getOutputStream();
            output.write(pdfData);
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Inform JSF to not take the response in hands.
        facesContext.responseComplete();
    }

    public Backer getBacker() {
        return backer;
    }

    public void setBacker(Backer backer) {
        this.backer = backer;
    }
}
