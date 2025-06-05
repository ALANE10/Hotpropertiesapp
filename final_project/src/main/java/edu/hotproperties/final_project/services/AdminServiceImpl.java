package edu.hotproperties.final_project.services;
import edu.hotproperties.final_project.emuns.Role;
//import edu.hotproperties.final_project.entities.Role;
import edu.hotproperties.final_project.entities.User;
import edu.hotproperties.final_project.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;




import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository,
                            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void createAgent(User user) {
        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + user.getEmail());
        }

        // Set the agent role
        Set<Role> roles = new HashSet<>();
        roles.add(Role.AGENT);
        user.setRoles(roles);

        // Encrypt password and set createdAt timestamp
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        // Save the agent
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + id + " not found."));
        userRepository.delete(existingUser);
    }
}
