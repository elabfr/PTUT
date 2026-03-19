package com.ptut.backend.service;

import com.ptut.backend.dto.CreateActionRequest;
import com.ptut.backend.dto.InscriptionResponse;
import com.ptut.backend.entity.Action;
import com.ptut.backend.entity.Inscription;
import com.ptut.backend.entity.Role;
import com.ptut.backend.entity.Utilisateur;
import com.ptut.backend.repository.ActionRepository;
import com.ptut.backend.repository.InscriptionRepository;
import com.ptut.backend.repository.UtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class ActionService {

    private final ActionRepository actionRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final InscriptionRepository inscriptionRepository;

    public ActionService(
            ActionRepository actionRepository,
            UtilisateurRepository utilisateurRepository,
            InscriptionRepository inscriptionRepository
    ) {
        this.actionRepository = actionRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.inscriptionRepository = inscriptionRepository;
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
        inscription.setStatutInscription("INSCRIT");

        Inscription saved = inscriptionRepository.save(inscription);

        InscriptionResponse response = new InscriptionResponse();
        response.setIdInscription(saved.getIdInscription());
        response.setIdAction(action.getIdAction());
        response.setNomAmbassadeur(utilisateur.getNom());
        response.setPrenomAmbassadeur(utilisateur.getPrenom());
        response.setDateInscription(saved.getDateInscription());
        response.setStatutInscription(saved.getStatutInscription());
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
}