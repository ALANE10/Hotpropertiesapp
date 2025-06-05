package edu.hotproperties.final_project.config;

import edu.hotproperties.final_project.emuns.Role;
import edu.hotproperties.final_project.entities.User;
import edu.hotproperties.final_project.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminEmail = "admin@gmail.com";

        Optional<User> existing = userRepository.findByEmail(adminEmail);
        if (existing.isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("adminpass123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setCreatedAt(LocalDateTime.now());

            Set<Role> roles = new HashSet<>();
            roles.add(Role.ADMIN);
            admin.setRoles(roles);

            userRepository.save(admin);
            System.out.println("Admin user created: " + adminEmail);
        } else {
            System.out.println("Admin user already exists: " + adminEmail);
        }
    }
}
