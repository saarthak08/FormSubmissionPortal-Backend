package com.sg.fsp.controller;


import com.sg.fsp.model.User;
import com.sg.fsp.service.EmailService;
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
@RequestMapping("/api/signup")
public class RegisterController {

    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private UserService userService;
    private EmailService emailService;

    @Autowired
    public RegisterController(BCryptPasswordEncoder bCryptPasswordEncoder,UserService userService, EmailService emailService) {
       this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userService = userService;
        this.emailService = emailService;
    }



    // Process form input data
    @RequestMapping(value = "/register", method = RequestMethod.POST,consumes = "application/json")
    public ResponseEntity<String> processRegistrationForm(@Valid @RequestBody User user, HttpServletRequest request) {

        // Lookup user in database by e-mail
        User userExists = userService.findByEmail(user.getEmail());

        System.out.println(userExists);

        if (userExists != null&&userExists.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "User Already Exists");
        }
        else if(userExists!=null&& !userExists.isEnabled()){
            sendConfirmationMail(user,request);
            return new ResponseEntity<>("Confirmation email sent!",HttpStatus.OK);
        }
        else {
            sendConfirmationMail(user,request);
        }
        return new ResponseEntity<>("User created & confirmation email sent!",HttpStatus.OK);
    }


    private void sendConfirmationMail(User user, HttpServletRequest request){
        // new user so we create user and send confirmation e-mail

        // Disable user until they click on confirmation link in email
        user.setEnabled(false);

        // Generate random 36-character string token for confirmation link
        user.setConfirmationToken(UUID.randomUUID().toString());
        userService.saveUser(user);

        String appUrl = request.getScheme() + "://" + request.getServerName()+":8080";

        SimpleMailMessage registrationEmail = new SimpleMailMessage();
        registrationEmail.setTo(user.getEmail());
        registrationEmail.setSubject("Registration Confirmation");
        registrationEmail.setText("To confirm your e-mail address, please click the link below:\n"
                + appUrl + "/confirm?token=" + user.getConfirmationToken());
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

        // Set new password
        user.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));

        // Set user to enabled
        user.setEnabled(true);

        // Save user
        userService.saveUser(user);

        return new ResponseEntity<>("Your password has been set!",HttpStatus.OK);
    }

}
