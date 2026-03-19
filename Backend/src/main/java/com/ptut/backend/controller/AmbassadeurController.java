package com.ptut.backend.controller;

import com.ptut.backend.dto.UserSummaryResponse;
import com.ptut.backend.entity.Role;
import com.ptut.backend.repository.UtilisateurRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ambassadeurs")
public class AmbassadeurController {

    private final UtilisateurRepository utilisateurRepository;

    public AmbassadeurController(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    @Operation(summary = "Lister les ambassadeurs")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> listAmbassadeurs() {
        List<UserSummaryResponse> ambassadeurs = utilisateurRepository.findAllByRole(Role.AMBASSADEUR).stream()
                .map(user -> new UserSummaryResponse(
                        user.getIdUtilisateur(),
                        user.getNom(),
                        user.getPrenom(),
                        user.getEmail(),
                        user.getRole() != null ? user.getRole().name() : null
                ))
                .toList();

        return ResponseEntity.ok(ambassadeurs);
    }
}