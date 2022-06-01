/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author maximegavens
 */
public class AmenagementHydrauliqueView {

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type;

    @JsonProperty("superficie")
    private String superficie;

    @JsonProperty("capaciteStockage")
    private String capaciteStockage;

    @JsonProperty("profondeurMaximum")
    private String profondeurMaximum;

    @JsonProperty("designation")
    private String designation;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getSuperficie() {
        return superficie;
    }

    public String getCapaciteStockage() {
        return capaciteStockage;
    }

    public String getProfondeurMaximum() {
        return profondeurMaximum;
    }

    public String getDesignation() {
        return designation;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSuperficie(String superficie) {
        this.superficie = superficie;
    }

    public void setCapaciteStockage(String capaciteStockage) {
        this.capaciteStockage = capaciteStockage;
    }

    public void setProfondeurMaximum(String profondeurMaximum) {
        this.profondeurMaximum = profondeurMaximum;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

}
