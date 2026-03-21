package com.ptut.backend.service;

import com.ptut.backend.dto.CreateActionRequest;
import com.ptut.backend.dto.InscriptionResponse;
import com.ptut.backend.entity.Action;
import com.ptut.backend.entity.DocumentResource;
import com.ptut.backend.entity.Inscription;
import com.ptut.backend.entity.Role;
import com.ptut.backend.entity.Utilisateur;
import com.ptut.backend.repository.ActionRepository;
import com.ptut.backend.repository.DocumentResourceRepository;
import com.ptut.backend.repository.InscriptionRepository;
import com.ptut.backend.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class ActionService {

    private static final String STATUT_INSCRIT = "INSCRIT";
    private static final String STATUT_DOSSIER_EN_COURS = "DOSSIER_EN_COURS_DE_TRAITEMENT";
    private static final String STATUT_VALIDE = "VALIDE";
    private static final String STATUT_REFUSE = "REFUSE";

    private final ActionRepository actionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final InscriptionRepository inscriptionRepository;
    private final DocumentResourceService documentResourceService;
    private final DocumentResourceRepository documentResourceRepository;

    public ActionService(
            ActionRepository actionRepository,
            UtilisateurRepository utilisateurRepository,
            InscriptionRepository inscriptionRepository,
            DocumentResourceService documentResourceService,
            DocumentResourceRepository documentResourceRepository
    ) {
        this.actionRepository = actionRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.inscriptionRepository = inscriptionRepository;
        this.documentResourceService = documentResourceService;
        this.documentResourceRepository = documentResourceRepository;
    }

    public Action createAction(CreateActionRequest request) {
        Action action = new Action();
        action.setTitre(request.getTitre());
        action.setDescription(request.getDescription());
        action.setLieu(request.getLieu());
        action.setTypeEtablissement(request.getTypeEtablissement());
        action.setDateAction(request.getDateAction());
        action.setTypeAction(request.getTypeAction());
        action.setCapaciteMax(request.getCapaciteMax());
        action.setStatut(request.getStatut());

        return actionRepository.save(action);
    }

    public List<Action> listAllActions() {
        return actionRepository.findAll();
    }

    public Action updateAction(Long idAction, CreateActionRequest request) {
        Action existingAction = actionRepository.findById(idAction)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Action introuvable"));

        existingAction.setTitre(request.getTitre());
        existingAction.setDescription(request.getDescription());
        existingAction.setLieu(request.getLieu());
        existingAction.setTypeEtablissement(request.getTypeEtablissement());
        existingAction.setDateAction(request.getDateAction());
        existingAction.setTypeAction(request.getTypeAction());
        existingAction.setCapaciteMax(request.getCapaciteMax());
        existingAction.setStatut(request.getStatut());

        return actionRepository.save(existingAction);
    }

    public void deleteAction(Long idAction) {
        if (!actionRepository.existsById(idAction)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Action introuvable");
        }

        actionRepository.deleteById(idAction);
    }

    public InscriptionResponse inscrireAmbassadeur(Long idAction, String emailAmbassadeur) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(emailAmbassadeur)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (utilisateur.getRole() != Role.AMBASSADEUR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seuls les ambassadeurs peuvent s'inscrire");
        }

        Action action = actionRepository.findById(idAction)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Action introuvable"));

        if (inscriptionRepository.existsByActionAndUtilisateur(action, utilisateur)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Déjà inscrit à cette action");
        }

        if (action.getCapaciteMax() != null) {
            long nombreInscrits = inscriptionRepository.countByAction(action);
            if (nombreInscrits >= action.getCapaciteMax()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Capacité maximale atteinte");
            }
        }

        Inscription inscription = new Inscription();
        inscription.setAction(action);
        inscription.setUtilisateur(utilisateur);
        inscription.setDateInscription(Instant.now());
        inscription.setStatutInscription(STATUT_INSCRIT);

        Inscription saved = inscriptionRepository.save(inscription);

        InscriptionResponse response = new InscriptionResponse();
        response.setIdInscription(saved.getIdInscription());
        response.setIdAction(action.getIdAction());
        response.setTitreAction(action.getTitre());
        response.setNomAmbassadeur(utilisateur.getNom());
        response.setPrenomAmbassadeur(utilisateur.getPrenom());
        response.setDateInscription(saved.getDateInscription());
        response.setStatutInscription(saved.getStatutInscription());
        response.setJustificatifId(saved.getJustificatifPresence() != null ? saved.getJustificatifPresence().getId() : null);
        return response;
    }

    public List<InscriptionResponse> listInscriptionsByAction(Long idAction) {
        if (!actionRepository.existsById(idAction)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Action introuvable");
        }

        return inscriptionRepository.findAllResponsesByActionId(idAction);
    }

    public void desinscrireAmbassadeur(Long idAction, String emailAmbassadeur) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(emailAmbassadeur)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (utilisateur.getRole() != Role.AMBASSADEUR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seuls les ambassadeurs peuvent se désinscrire");
        }

        Action action = actionRepository.findById(idAction)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Action introuvable"));

        Inscription inscription = inscriptionRepository.findByActionAndUtilisateur(action, utilisateur)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscription introuvable"));

        inscriptionRepository.delete(inscription);
    }

    public void deposerJustificatifPresence(Long idAction, String emailAmbassadeur, MultipartFile file) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(emailAmbassadeur)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (utilisateur.getRole() != Role.AMBASSADEUR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seuls les ambassadeurs peuvent déposer un justificatif");
        }

        Action action = actionRepository.findById(idAction)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Action introuvable"));

        Inscription inscription = inscriptionRepository.findByActionAndUtilisateur(action, utilisateur)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inscription introuvable : vous devez être inscrit à cette action"
                ));

        Long ancienJustificatifId = inscription.getJustificatifPresence() != null
                ? inscription.getJustificatifPresence().getId()
                : null;

        Long nouveauJustificatifId = documentResourceService.upload(file, emailAmbassadeur).getId();
        DocumentResource nouveauJustificatif = documentResourceRepository.findById(nouveauJustificatifId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource introuvable"));

        inscription.setJustificatifPresence(nouveauJustificatif);
        inscription.setStatutInscription(STATUT_DOSSIER_EN_COURS);
        inscriptionRepository.save(inscription);

        if (ancienJustificatifId != null && !ancienJustificatifId.equals(nouveauJustificatifId)) {
            documentResourceRepository.deleteById(ancienJustificatifId);
        }
    }

    @Transactional(readOnly = true)
    public List<InscriptionResponse> listMesInscriptions(String emailAmbassadeur) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(emailAmbassadeur)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));

        if (utilisateur.getRole() != Role.AMBASSADEUR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seuls les ambassadeurs peuvent consulter leurs inscriptions");
        }

        return inscriptionRepository.findByUtilisateur_EmailOrderByDateInscriptionDesc(emailAmbassadeur)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InscriptionResponse> listDossiersEnCours() {
        return inscriptionRepository.findByStatutInscriptionAndJustificatifPresenceIsNotNullOrderByDateInscriptionDesc(STATUT_DOSSIER_EN_COURS)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public InscriptionResponse validerDossier(Long idAction, Long idInscription) {
        return traiterDossier(idAction, idInscription, STATUT_VALIDE);
    }

    @Transactional
    public InscriptionResponse refuserDossier(Long idAction, Long idInscription) {
        return traiterDossier(idAction, idInscription, STATUT_REFUSE);
    }

    private InscriptionResponse traiterDossier(Long idAction, Long idInscription, String nouveauStatut) {
        Inscription inscription = inscriptionRepository.findByIdInscriptionAndAction_IdAction(idInscription, idAction)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inscription introuvable"));

        if (inscription.getJustificatifPresence() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucun justificatif déposé pour cette inscription");
        }

        if (!STATUT_DOSSIER_EN_COURS.equals(inscription.getStatutInscription())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce dossier a déjà été traité");
        }

        inscription.setStatutInscription(nouveauStatut);
        Inscription saved = inscriptionRepository.save(inscription);
        return toResponse(saved);
    }

    private InscriptionResponse toResponse(Inscription inscription) {
        InscriptionResponse response = new InscriptionResponse();
        response.setIdInscription(inscription.getIdInscription());
        response.setIdAction(inscription.getAction().getIdAction());
        response.setTitreAction(inscription.getAction().getTitre());
        response.setNomAmbassadeur(inscription.getUtilisateur().getNom());
        response.setPrenomAmbassadeur(inscription.getUtilisateur().getPrenom());
        response.setDateInscription(inscription.getDateInscription());
        response.setStatutInscription(inscription.getStatutInscription());
        response.setJustificatifId(inscription.getJustificatifPresence() != null ? inscription.getJustificatifPresence().getId() : null);
        return response;
    }
}