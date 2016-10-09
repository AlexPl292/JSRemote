package com.tsystems.javaschool.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex on 08.10.16.
 */
public class Backer {
    private String email;
    private String password;
    private String url;
    private List<String> tariffNames = new ArrayList<String>();
    private String chosenTariff;

    public void addName(String name) {
        tariffNames.add(name);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getTariffNames() {
        return tariffNames;
    }

    public void setTariffNames(List<String> tariffNames) {
        this.tariffNames = tariffNames;
    }

    public String getChosenTariff() {
        return chosenTariff;
    }

    public void setChosenTariff(String chosenTariff) {
        this.chosenTariff = chosenTariff;
    }
}
