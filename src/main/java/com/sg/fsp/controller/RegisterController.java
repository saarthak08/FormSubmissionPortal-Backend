package com.sg.fsp.controller;


import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
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


    @GetMapping(value = "/hello")
    @ResponseBody
    public String hello() {
        return "hello";
    }


    // Process form input data
    @ResponseBody
    @RequestMapping(value = "/register", method = RequestMethod.POST,consumes = "application/json",produces = "application/json")
    public ResponseEntity<?> processRegistrationForm(@Valid @RequestBody Map<String, String> user, HttpServletRequest request) {

        Map<String, String> res=new HashMap<>();
        // Lookup user in database by e-mail
        User userExists = userService.findByEmail(user.get("email"));


        System.out.println(userExists);

        if (userExists != null&&userExists.isEnabled()) {
            res.put("message","Error! User Exists");
            return new ResponseEntity<>(res,
                    HttpStatus.BAD_REQUEST);
        }
        else if(userExists!=null&& !userExists.isEnabled()){
            sendConfirmationMail(userExists,request);
            res.put("message","Confirmation Link Sent Again!");
            return new ResponseEntity<>(res,HttpStatus.OK);
        }
        else {
            userExists=new User();
            userExists.setLastName(user.get("lastName"));
            userExists.setFirstName(user.get("firstName"));
            userExists.setEmail(user.get("email"));
            userExists.setIdNumber(user.get("idNumber"));
            String role=user.get("role");
            Role role1;
            if(role.equals("STUDENT")){
                role1=roleRepository.findByUserType(UserType.STUDENT);
            } else if (role.equals("DEAN")){
                role1=roleRepository.findByUserType(UserType.DEAN);
            } else{
                role1=roleRepository.findByUserType(UserType.PROVOST);
            }
            userExists.setRole(role1);
            sendConfirmationMail(userExists,request);
        }
        res.put("message","User created & confirmation link sent!");
        return new ResponseEntity<>(res,HttpStatus.OK);
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
                + appUrl + "/api/signup/confirmToken?token=" + user.getConfirmationToken());
        registrationEmail.setFrom("Form Submission Portal <noreply@formsubmissionportal.com>");
        emailService.sendEmail(registrationEmail);
    }


    // Process confirmation link
    @RequestMapping(value="/confirmToken", method = RequestMethod.GET)
    @ResponseBody
    public Object confirmRegistration(@RequestParam("token") String token, ModelAndView modelAndView) {
        User user = userService.findByConfirmationToken(token);

        if (user == null) { // No token found in DB
            return "Invalid Token";
        } else { // Token found
            modelAndView.addObject("confirmationToken", user.getConfirmationToken());
            modelAndView.setViewName("confirm");
            return modelAndView;
        }
    }

    // Process confirmation link
    @RequestMapping(value="/confirmPassword")
    @ResponseBody
    public Object confirmRegistration(ModelAndView modelAndView, BindingResult bindingResult, @RequestParam Map<String, String> requestParams, RedirectAttributes redir) {

        Zxcvbn passwordCheck = new Zxcvbn();

        Strength strength = passwordCheck.measure(requestParams.get("password"));

        if (strength.getScore() < 1) {
            //modelAndView.addObject("errorMessage", "Your password is too weak.  Choose a stronger one.");
            bindingResult.reject("password");

            redir.addFlashAttribute("errorMessage", "Your password is too weak.  Choose a stronger one.");

            modelAndView.setViewName("redirect:/api/signup/confirmToken?token=" + requestParams.get("token"));
            return modelAndView;
        }

        // Find the user associated with the reset token
        User user = userService.findByConfirmationToken(requestParams.get("token"));

        if(user==null){
            redir.addFlashAttribute("errorMessage", "Link expired or Wrong link!");
            modelAndView.setViewName("redirect:/api/signup/confirmToken?token=" + requestParams.get("token"));
            return modelAndView;
        }
        if(user.isEnabled()){
            redir.addFlashAttribute("errorMessage", "Link expired or Wrong link!");
            modelAndView.setViewName("redirect:/api/signup/confirmToken?token=" + requestParams.get("token"));
            return modelAndView;
        }
        // Set new password
        user.setPassword(bCryptPasswordEncoder.encode(requestParams.get("password")));

        // Set user to enabled
        user.setEnabled(true);

        user.setConfirmationToken("");

        // Save user
        userService.saveUser(user);
        return "Password set!";
    }

}
