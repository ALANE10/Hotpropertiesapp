package edu.hotproperties.final_project.services;

import edu.hotproperties.final_project.entities.User;

import java.util.List;

public interface AdminService {
    List<User> getAllUsers();
    void createAgent(User user);
    void deleteUser(Long id);
}
