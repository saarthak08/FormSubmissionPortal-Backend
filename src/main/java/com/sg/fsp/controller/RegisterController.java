package com.sg.fsp.controller;


import com.sg.fsp.enums.UserType;
import com.sg.fsp.model.Role;
import com.sg.fsp.model.User;
import com.sg.fsp.repository.RoleRepository;
import com.sg.fsp.service.EmailService;
import com.sg.fsp.service.InitialDataLoader;
import com.sg.fsp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api/signup")
public class RegisterController {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserService userService;
    private EmailService emailService;
    private RoleRepository roleRepository;


    @Autowired
    public RegisterController(BCryptPasswordEncoder bCryptPasswordEncoder, UserService userService, EmailService emailService, RoleRepository roleRepository) {
       this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userService = userService;
        this.emailService = emailService;
        this.roleRepository=roleRepository;
    }



    // Process form input data
    @RequestMapping(value = "/register", method = RequestMethod.POST,consumes = "application/json")
    public ResponseEntity<String> processRegistrationForm(@Valid @RequestBody Map<String, String> user, HttpServletRequest request) {

        // Lookup user in database by e-mail
        User userExists = userService.findByEmail(user.get("email"));


        System.out.println(userExists);

        if (userExists != null&&userExists.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User Already Exists");
        }
        else if(userExists!=null&& !userExists.isEnabled()){
            sendConfirmationMail(userExists,request,false);
            return new ResponseEntity<>("Confirmation email sent!",HttpStatus.OK);
        }
        else {
            userExists=new User();
            userExists.setLastName(user.get("lastName"));
            userExists.setFirstName(user.get("firstName"));
            userExists.setEmail(user.get("email"));
            String role=user.get("role");
            Role role1;
            if(role.equals("STUDENT")){
                role1=roleRepository.findByUserType(UserType.STUDENT);
            } else if (role.equals("DEAN")){
                role1=roleRepository.findByUserType(UserType.DEAN);
            } else{
                role1=roleRepository.findByUserType(UserType.CONTROLLER);
            }
            userExists.setRole(role1);
            sendConfirmationMail(userExists,request,true);
        }
        return new ResponseEntity<>("User created & confirmation email sent!",HttpStatus.OK);
    }


    private void sendConfirmationMail(User user, HttpServletRequest request,boolean x){
        // new user so we create user and send confirmation e-mail

        // Disable user until they click on confirmation link in email
        user.setEnabled(false);

        // Generate random 36-character string token for confirmation link
        user.setConfirmationToken(UUID.randomUUID().toString());
        if(x) {
            userService.saveUser(user);
        }

        String appUrl = request.getScheme() + "://" + request.getServerName()+":3000";

        SimpleMailMessage registrationEmail = new SimpleMailMessage();
        registrationEmail.setTo(user.getEmail());
        registrationEmail.setSubject("Registration Confirmation");
        registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
                + appUrl + "/confirm/" + user.getConfirmationToken());
        registrationEmail.setFrom("Form Submission Portal <noreply@formsubmissionportal.com>");
        emailService.sendEmail(registrationEmail);
    }


    // Process confirmation link
    @RequestMapping(value="/confirm", method = RequestMethod.GET)
    public ResponseEntity<String> confirmRegistration(@RequestParam("token") String token) {

        User user = userService.findByConfirmationToken(token);

        if (user == null) { // No token found in DB
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid confirmation token.");

        } else { // Token found
            return new ResponseEntity<>("Valid confirmation token.",HttpStatus.ACCEPTED);
        }
    }

    // Process confirmation link
    @RequestMapping(value="/confirm", method = RequestMethod.POST,consumes = "application/json")
    public ResponseEntity<String> confirmRegistration(@RequestBody Map<String, String> requestParams) {

        // Find the user associated with the reset token
        User user = userService.findByConfirmationToken(requestParams.get("token"));

        if(user==null){
            return new ResponseEntity<>("User not found",HttpStatus.NOT_FOUND);
        }
        // Set new password
        user.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));

        // Set user to enabled
        user.setEnabled(true);

        // Save user
        userService.saveUser(user);

        return new ResponseEntity<>("Your password has been set!",HttpStatus.OK);
    }

}
