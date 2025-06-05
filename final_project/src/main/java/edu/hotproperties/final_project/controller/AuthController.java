package edu.hotproperties.final_project.controller;

import edu.hotproperties.final_project.entities.Message;
import edu.hotproperties.final_project.entities.Property;
import edu.hotproperties.final_project.services.AdminService;
import edu.hotproperties.final_project.services.AuthService;
import edu.hotproperties.final_project.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import edu.hotproperties.final_project.entities.User;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final AdminService adminService;

    public AuthController( AuthService authService, UserService userService, AdminService adminService) {
        this.authService = authService;
        this.userService = userService;
        this.adminService= adminService;
    }

    //admin
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PostMapping("/create-agent")
    public ResponseEntity<String> createAgent(@RequestBody User user) {
        adminService.createAgent(user);
        return ResponseEntity.ok("Agent created successfully.");
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully.");
    }

    @GetMapping({"/", ""})
    public String loginRedirect() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute("user") User user,
                               HttpServletResponse response,
                               Model model) {
        try {
            Cookie jwtCookie = authService.loginAndCreateJwtCookie(user);
            response.addCookie(jwtCookie);
            return "redirect:/dashboard";
        } catch (BadCredentialsException e) {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
    }

    @GetMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public String logout(HttpServletResponse response) {
        authService.clearJwtCookie(response);
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                               @RequestParam("selectedRoles") List<String> roleNames,
                               @RequestParam(value = "file", required = false) MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        try {
            // First, register the user (this will assign them an ID)
            User savedUser = userService.registerNewUser(user, roleNames);

            redirectAttributes.addFlashAttribute("successMessage", "Registration successful.");
            return "redirect:/login";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        userService.prepareProfileModel(model);
        return "profile";
    }

    @GetMapping("/profile/edit")
    @PreAuthorize("isAuthenticated()")
    public String editProfile(Model model){
        userService.prepareEditProfileModel(model);
        return "edit_profile";
    }

    @PostMapping("/profile/edit")
    @PreAuthorize("isAuthenticated()")
    public String editProfile(
                              @RequestParam(required = false) String firstName,
                              @RequestParam(required = false) String lastName,
                              @RequestParam(required = false) String email
                              ) {

        userService.postEditProfile(firstName, lastName, email);
        return "redirect:/profile";
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('BUYER', 'AGENT', 'ADMIN')")
    public String dashboard(Model model) {
        userService.prepareDashboardModel(model);
        return "dashboard";
    }

    //BUYER FUNCTIONALITY
    @GetMapping("/properties/list")
    @PreAuthorize("hasRole('BUYER')")
    public String properties(Model model) {
        model.addAttribute("properties", userService.getProperties());
        return "browse_properties";
    }

    @GetMapping("/properties/view")
    @PreAuthorize("hasRole('BUYER')")
    public String viewing(@RequestParam String title, Model model) {
        Property priority = userService.getProperty(title);
        model.addAttribute("property",priority);
        User current = authService.getCurrentUser();
        boolean button = userService.isFavorite(current, priority);
        model.addAttribute("button",button);
        return "property_details";
    }

    @GetMapping("/favorites/favorites")
    @PreAuthorize("hasRole('BUYER')")
    public String favorites(Model model) {
        User current = authService.getCurrentUser();
        List<Property> favorites = userService.getFavorites(current);
        model.addAttribute("properties", favorites);
        return "favorites";
    }

    @DeleteMapping("/favorites/remove")
    @PreAuthorize("hasRole('BUYER')")
    public String removeFavorite(@RequestParam Long propertyId) {
        User current = authService.getCurrentUser();
        Property property = userService.getPropertyById(propertyId);
        userService.removeFavorite(current, property);
        return "redirect:/favorites/favorites";
    }

    //AGENT FUNCTIONALITY

    //Create new property -- get form/screen
    @GetMapping("/agent/new_property")
    public String addPropertyForm(Model model) {
        userService.prepareNewPropertyModel(model);
        return "new_property";
    }

    //Post new property
    @PostMapping("/agent/new_property")
    public String createProperty(@RequestParam(required = true) String title,
                                 @RequestParam(required = true) int price,
                                 @RequestParam(required = true) String location,
                                 @RequestParam(required = true) String description,
                                 @RequestParam(required = true) int size
                                )
    {
        userService.createProperty(new Property(title, price, location, description, size));
        return "redirect:/agent/manage_listings";
    }

    //Get existing property by id and update it

    @PostMapping("/properties/edit")
    @PreAuthorize("hasRole('AGENT')")
    public void editProperty(@RequestParam(name="id") Long id, @ModelAttribute("property") Property property) {
        userService.updateProperty(id, property); //returns updated property
    }

    @GetMapping("/properties/edit")
    @PreAuthorize("hasRole('AGENT')")
    public void prepareEditPropertyModel(@RequestParam(name="id") Long id, Model model) {
        userService.prepareEditPropertyModel(id, model); //returns updated property
    }

    //Returns a list of properties managed by the agent (current user)
    @GetMapping("/agent/manage_listings")
    public String getManagedProperties(Model model) {
        userService.prepareManagedListingsModel(model);
        return "manage_listings";
    }

    //Get all messages for Agent
    @GetMapping("/agent/messages")
    @PreAuthorize("hasRole('AGENT')")
    public String getMessages(Model model) {
        //User service adds Agent's list of messages to the model
        userService.prepareMessagesModel(model);
        return "messages";
    }

    //Agent replies to buyer messages
    @GetMapping("/agent/message")
    @PreAuthorize("hasRole('AGENT')")
    public String viewMessage(@RequestParam(name="id") Long id, Model model) {
        //User adds sender info and message to screen
        userService.prepareViewMessageModel(id, model);
        return "view_message";
    }

    //Get all messages for Agent


    //Agent replies to buyer messages
    @PostMapping("/agent/message")
    @PreAuthorize("hasRole('AGENT')")
    public String viewMessage(@RequestParam(name="id") Long id, @RequestBody String reply) {
        //User adds sender info and message to screen
        userService.postMessageReply(id, reply);
        return "view_message";
    }
}



