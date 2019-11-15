package com.sg.fsp.controller;


import com.sg.fsp.enums.UserType;
import com.sg.fsp.model.Role;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UserController {

    private UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService=userService;
    }

    @GetMapping("/user/info")
    public ResponseEntity<?> getUserInfo(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser=(User)auth.getPrincipal();
        com.sg.fsp.model.User user=userService.findByEmail(authUser.getUsername());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() throws java.lang.Exception, NullPointerException{
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser=(User)auth.getPrincipal();
        com.sg.fsp.model.User user=userService.findByEmail(authUser.getUsername());
        Role role=user.getRole();
        if(role.getUserType() == UserType.STUDENT){
            return ResponseEntity.status(401).build();
        }
        else {
            com.sg.fsp.model.User adminUser=null;
            List<com.sg.fsp.model.User> users = userService.getAllUsers();
            Map<String,List<com.sg.fsp.model.User>> map=new HashMap<>();
            for(com.sg.fsp.model.User u:users){
                if(u.getEmail().equals("fsp_admin@myamu.ac.in")){
                    adminUser=u;
                }
            }
         /*   if(adminUser!=null) {
                users.remove(adminUser);
            }*/
            map.put("users",users);
            return new ResponseEntity<>(map,HttpStatus.OK);
        }
    }
}
