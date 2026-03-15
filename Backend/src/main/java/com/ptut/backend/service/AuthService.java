package com.ptut.backend.service;

import com.ptut.backend.dto.AuthResponse;
import com.ptut.backend.dto.LoginRequest;
import com.ptut.backend.dto.RegisterRequest;
import com.ptut.backend.entity.Role;
import com.ptut.backend.entity.Utilisateur;
import com.ptut.backend.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {

        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email déjà utilisé");
        }

        Utilisateur user = new Utilisateur();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(resolveDefaultStudentRole());

        Utilisateur savedUser = utilisateurRepository.save(user);

        String token = jwtService.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        return new AuthResponse(
                token,
                "Bearer",
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "Utilisateur enregistré"
        );
    }

    public AuthResponse login(LoginRequest request) {

        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou mot de passe incorrect");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                token,
                "Bearer",
                user.getEmail(),
                user.getRole().name(),
                "Connexion réussie"
        );
    }

    public Utilisateur getCurrentUser(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
    }

    private Role resolveDefaultStudentRole() {
        for (Role role : Role.values()) {
            if ("USER".equals(role.name())) {
                return role;
            }
        }

        for (Role role : Role.values()) {
            if ("AMBASSADEUR".equals(role.name())) {
                return role;
            }
        }

        return Role.values()[0];
    }

}
