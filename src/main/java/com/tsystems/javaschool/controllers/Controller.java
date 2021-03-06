package com.tsystems.javaschool.controllers;

import com.tsystems.javaschool.Utils;
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

    @EJB
    private Service service;

    private Backer backer = new Backer();

    // Check connection, load tariffs list
    public void login() {
        Boolean res = service.logIn(backer);
        if (res)
            Utils.addMessage("Success login");
        else
            Utils.addMessage("Fail login");
    }

    public void createAndLoadPdf() {
        if (backer.getChosenTariff() == null || backer.getChosenTariff().equals(""))
            return;

        byte[] pdfData = service.generate(backer);
        if (pdfData == null)
            return;

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();


        // Initialize response.
        response.reset();
        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "attachment; filename=\"report.pdf\"");

        // Write file to response.
        try (OutputStream output = response.getOutputStream()) {
            output.write(pdfData);
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
