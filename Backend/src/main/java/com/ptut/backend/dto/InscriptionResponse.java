package com.ptut.backend.dto;

import java.time.Instant;

public class InscriptionResponse {

    private Long idInscription;
    private Long idAction;
    private String nomAmbassadeur;
    private String prenomAmbassadeur;
    private Instant dateInscription;
    private String statutInscription;

    public InscriptionResponse() {
    }

    public InscriptionResponse(Long idInscription, Long idAction, String nomAmbassadeur, String prenomAmbassadeur, Instant dateInscription, String statutInscription) {
        this.idInscription = idInscription;
        this.idAction = idAction;
        this.nomAmbassadeur = nomAmbassadeur;
        this.prenomAmbassadeur = prenomAmbassadeur;
        this.dateInscription = dateInscription;
        this.statutInscription = statutInscription;
    }

    public Long getIdInscription() {
        return idInscription;
    }

    public void setIdInscription(Long idInscription) {
        this.idInscription = idInscription;
    }

    public Long getIdAction() {
        return idAction;
    }

    public void setIdAction(Long idAction) {
        this.idAction = idAction;
    }

    public String getNomAmbassadeur() {
        return nomAmbassadeur;
    }

    public void setNomAmbassadeur(String nomAmbassadeur) {
        this.nomAmbassadeur = nomAmbassadeur;
    }

    public String getPrenomAmbassadeur() {
        return prenomAmbassadeur;
    }

    public void setPrenomAmbassadeur(String prenomAmbassadeur) {
        this.prenomAmbassadeur = prenomAmbassadeur;
    }

    public Instant getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(Instant dateInscription) {
        this.dateInscription = dateInscription;
    }

    public String getStatutInscription() {
        return statutInscription;
    }

    public void setStatutInscription(String statutInscription) {
        this.statutInscription = statutInscription;
    }
}