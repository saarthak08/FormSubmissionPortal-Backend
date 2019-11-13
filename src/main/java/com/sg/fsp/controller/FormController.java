package com.sg.fsp.controller;

import com.sg.fsp.enums.UserType;
import com.sg.fsp.model.Form;
import com.sg.fsp.model.FormDetail;
import com.sg.fsp.repository.FormRepository;
import com.sg.fsp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class FormController {

    private UserService userService;
    private FormRepository formRepository;

    @Autowired
    public FormController(UserService userService, FormRepository formRepository){
        this.userService=userService;
        this.formRepository=formRepository;
    }


    @GetMapping(value = "/submit-form/{userid}")
    public ResponseEntity<?> addFormToAUser(@RequestBody Map<String,Object> param,@PathVariable int userid){
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User authUser = (User) auth.getPrincipal();
            com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
            if(user.getId()==userid){
                String formCode = (String)param.get("formCode");
                Form form = formRepository.findFormByFormCode(formCode);
                if(form!=null) {
                    form.addFormDetails((FormDetail)param.get("formDetails"));
                    formRepository.save(form);
                    user.addForm(form);
                    userService.saveUser(user);
                    return ResponseEntity.ok().build();
                }
                else{
                    return new ResponseEntity<>("No form found!",HttpStatus.BAD_REQUEST);
                }
            }
            else{
                return new ResponseEntity(HttpStatus.UNAUTHORIZED);
            }

        }
        catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage().trim());
        }
    }


    @GetMapping(value = "/{email}/get-forms")
    public ResponseEntity<?> getFormsofAUser(@PathVariable String email){
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User authUser = (User) auth.getPrincipal();
            com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
            if(user.getRole().getUserType()== UserType.STUDENT){
                com.sg.fsp.model.User targetUser=userService.findByEmail(email);
                List<Form> forms=targetUser.getForms();
                Map<String,Object> res=new HashMap<>();
                res.put("user",targetUser.getEmail());
                res.put("forms",forms);
                return new ResponseEntity<>(res,HttpStatus.OK);
            }
            else {
                if(email.equals(user.getEmail())){
                    List<Form> forms=user.getForms();
                    Map<String,Object> res=new HashMap<>();
                    res.put("user",user.getEmail());
                    res.put("forms",forms);
                    return new ResponseEntity<>(res,HttpStatus.OK);
                }
                else {
                    return new ResponseEntity(HttpStatus.UNAUTHORIZED);
                }
            }
        }
        catch(Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage().trim());
        }
    }

    @GetMapping(value = "/get-formDetails/{formCode}")
    public ResponseEntity<?> getAllFormsDetailsofForm(@PathVariable String formCode){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
        if(user.getRole().getUserType()== UserType.STUDENT){
            Form form=formRepository.findFormByFormCode(formCode);
            if(form!=null) {
                Map<String, Object> res = new HashMap<>();
                res.put("form", form);
                return new ResponseEntity<>(res, HttpStatus.OK);
            }
            else{
                return ResponseEntity.status(405).build();
            }
        }
        else{
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping(value = "/get-all-forms")
    public ResponseEntity<?> getAllForms(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
        if(user.getRole().getUserType()== UserType.STUDENT){
            List<Form> forms=formRepository.findAll();
            Map<String, Object> res=new HashMap<>();
            res.put("forms",forms);
            return new ResponseEntity<>(res,HttpStatus.OK);
        }
        else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }
}
