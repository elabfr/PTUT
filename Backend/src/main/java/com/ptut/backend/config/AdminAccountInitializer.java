package com.ptut.backend.config;

import com.ptut.backend.entity.Role;
import com.ptut.backend.entity.Utilisateur;
import com.ptut.backend.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "manon.fleuranceau@univ-jfc.fr";
    private static final String ADMIN_PASSWORD = "admin0000";

    private static final String ADMIN2_EMAIL = "a";
    private static final String ADMIN2_PASSWORD = "a";

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public AdminAccountInitializer(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        normalizeRoleValues();
        ensureRoleConstraint();
        ensureAdminAccount();
        ensureAdmin2Account();
    }

    private void normalizeRoleValues() {
        safeExecute("UPDATE utilisateur SET role = 'AMBASSADEUR' WHERE role = 'USER'");
    }

    private void ensureRoleConstraint() {
        safeExecute("ALTER TABLE utilisateur DROP CONSTRAINT IF EXISTS utilisateur_role_check");
        safeExecute("ALTER TABLE utilisateur ADD CONSTRAINT utilisateur_role_check CHECK (role IN ('ADMIN','AMBASSADEUR'))");
    }

    private void ensureAdminAccount() {
        Utilisateur admin = utilisateurRepository.findByEmail(ADMIN_EMAIL).orElseGet(() -> {
            Utilisateur created = new Utilisateur();
            created.setEmail(ADMIN_EMAIL);
            return created;
        });

        admin.setNom("Admin");
        admin.setPrenom("Manon");
        admin.setRole(Role.ADMIN);

        if (admin.getPassword() == null || !passwordEncoder.matches(ADMIN_PASSWORD, admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        }

        utilisateurRepository.save(admin);
    }

    private void ensureAdmin2Account() {
        Utilisateur admin = utilisateurRepository.findByEmail(ADMIN2_EMAIL).orElseGet(() -> {
            Utilisateur created = new Utilisateur();
            created.setEmail(ADMIN2_EMAIL);
            return created;
        });

        admin.setNom("Admin");
        admin.setPrenom("Test");
        admin.setRole(Role.ADMIN);

        if (admin.getPassword() == null || !passwordEncoder.matches(ADMIN2_PASSWORD, admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(ADMIN2_PASSWORD));
        }

        utilisateurRepository.save(admin);
    }

    private void safeExecute(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (RuntimeException exception) {
        }
    }
}
