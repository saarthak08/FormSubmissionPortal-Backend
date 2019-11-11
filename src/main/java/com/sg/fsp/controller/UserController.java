package com.sg.fsp.controller;


import com.sg.fsp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user/info")
    public ResponseEntity<?> getUserInfo(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser=(User)auth.getPrincipal();
        com.sg.fsp.model.User user=userService.findByEmail(authUser.getUsername());
        user.setPassword("");
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
