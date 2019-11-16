package com.sg.fsp.controller;

import com.sg.fsp.enums.UserType;
import com.sg.fsp.model.Form;
import com.sg.fsp.model.UserFormCheckpoints;
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
@RequestMapping("/api/forms")
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
                    FormDetail formDetail=(FormDetail)param.get("formDetails");
                    form.addFormDetails(formDetail);
                    Map<String, String> checkPoints=form.getFormCheckpoints().getCheckPoints();
                    UserFormCheckpoints userFormCheckpoints=new UserFormCheckpoints();
                    userFormCheckpoints.setFormDetail(formDetail);
                    Map<String, Boolean> userCheckpointMap=new HashMap<>();
                    Map<String, String> checkpointsTimestamp=new HashMap<>();
                    for(Map.Entry<String,String> entry:checkPoints.entrySet()){
                        userCheckpointMap.put(entry.getKey(),false);
                        checkpointsTimestamp.put(entry.getKey(),"");
                    }
                    userFormCheckpoints.setCheckPoints(userCheckpointMap);
                    userFormCheckpoints.setCheckPoints_Timestamps(checkpointsTimestamp);
                    formDetail.setUserFormCheckpoints(userFormCheckpoints);
                    formRepository.save(form);
                    user.addForm(form);
                    userService.saveUser(user);
                    String entryPoint=null;
                    String entryPoint_email=null;
                    Map<String,String> checkpoints=form.getFormCheckpoints().getCheckPoints();
                  /*  for (Map.Entry<String, String> entry : checkpoints.entrySet()) {
                        if(!entry.getValue()){
                            entryPoint=entry.getKey();
                            break;
                        }
                    }
                    for(Map.Entry<String,String> entry:checkPoints_emails.entrySet()){
                            if(entry.getKey().equals(entryPoint)){
                                entryPoint_email=entry.getValue();
                            }
                    }*/
                    com.sg.fsp.model.User entryPointUser=userService.findByEmail(entryPoint_email);
                    entryPointUser.addForm(form);
                    form.addUser(entryPointUser);
                    userService.saveUser(entryPointUser);
                    formRepository.save(form);
                    return ResponseEntity.ok().build();
                }
                else{
                    return new ResponseEntity<>("No form found!",HttpStatus.NOT_FOUND);
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


    @GetMapping(value = "/get-forms/{userid}")
    public ResponseEntity<?> getFormsofAUser(@PathVariable Long userid){
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User authUser = (User) auth.getPrincipal();
            com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
            if(user.getRole().getUserType()!= UserType.STUDENT){
                com.sg.fsp.model.User targetUser=userService.findUserById(userid);
                List<Form> forms=targetUser.getForms();
                Map<String,Object> res=new HashMap<>();
                res.put("user",targetUser.getEmail());
                res.put("forms",forms);
                return new ResponseEntity<>(res,HttpStatus.OK);
            }
            else {
                if(userid.equals(user.getId())){
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
        if(user.getRole().getUserType()!= UserType.STUDENT){
            Form form=formRepository.findFormByFormCode(formCode);
            if(form!=null) {
                Map<String, Object> res = new HashMap<>();
                res.put("form", form);
                res.put("formDetails",form.getFormDetails());
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
        if(user.getRole().getUserType()!= UserType.STUDENT){
            List<Form> forms=formRepository.findAll();
            Map<String, Object> res=new HashMap<>();
            res.put("forms",forms);
            return new ResponseEntity<>(res,HttpStatus.OK);
        }
        else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }



    @GetMapping(value = "/get-form-users/{formCode}")
    public ResponseEntity<?> getAllUsersofForm(@PathVariable String formCode){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
        if(user.getRole().getUserType()!= UserType.STUDENT){
            Form form=formRepository.findFormByFormCode(formCode);
            if(form!=null) {
                for(com.sg.fsp.model.User u:form.getUsers()){
                    u.setForms(null);
                }
                Map<String, Object> res = new HashMap<>();
                res.put("form", form);
                res.put("formUsers",form.getUsers());
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


    @GetMapping("/get-form-checkpoints/{formCode}")
    public ResponseEntity<?> getFormCheckPoints(@PathVariable String formCode){
        Form form=formRepository.findFormByFormCode(formCode);
        if(form==null){
            return new ResponseEntity<>("Form not found",HttpStatus.NOT_FOUND);
        }
        Map<String,Object> res=new HashMap<>();
        res.put("formCode",form.getFormCode());
        res.put("formCheckPoint",form.getFormCheckpoints().getCheckPoints());
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/get-form-checkpoints/{formCode}/{userid}")
    public ResponseEntity<?> getFormCheckpointsForAUserDetail(@PathVariable String formCode,@PathVariable Long userid){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
        Form form=formRepository.findFormByFormCode(formCode);
        UserFormCheckpoints userFormCheckpoints =null;
        com.sg.fsp.model.User user1=userService.findUserById(userid);
        for(FormDetail formDetail:form.getFormDetails()){
            if(user1.getEmail().equals(formDetail.getEmail())){
                userFormCheckpoints =formDetail.getUserFormCheckpoints();
            }
        }
        if(userFormCheckpoints !=null){
            Map<String,Object> res=new HashMap<>();
            res.put("email",user1.getEmail());
            res.put("formCode",formCode);
            res.put("formCheckPointID", userFormCheckpoints.getId());
            res.put("formCheckPoints", userFormCheckpoints.getCheckPoints());
            if(user.getRole().getUserType()!=UserType.STUDENT){
                return new ResponseEntity<>(res,HttpStatus.OK);
            }
            else{
                if(userid.equals(user1.getId())){
                    return new ResponseEntity<>(res,HttpStatus.OK);
                }
                else{
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
        }
        else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
