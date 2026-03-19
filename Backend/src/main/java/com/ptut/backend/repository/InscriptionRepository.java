package com.ptut.backend.repository;

import com.ptut.backend.entity.Action;
import com.ptut.backend.entity.Inscription;
import com.ptut.backend.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    boolean existsByActionAndUtilisateur(Action action, Utilisateur utilisateur);

    Optional<Inscription> findByActionAndUtilisateur(Action action, Utilisateur utilisateur);

    long countByAction(Action action);

    @Query("""
            SELECT new com.ptut.backend.dto.InscriptionResponse(
                i.idInscription,
                i.action.idAction,
                i.utilisateur.nom,
                i.utilisateur.prenom,
                i.dateInscription,
                i.statutInscription
            )
            FROM Inscription i
            WHERE i.action.idAction = :idAction
            ORDER BY i.dateInscription DESC
            """)
    List<com.ptut.backend.dto.InscriptionResponse> findAllResponsesByActionId(Long idAction);
}