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

import java.util.ArrayList;
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
    public FormController(UserService userService, FormRepository formRepository) {
        this.userService = userService;
        this.formRepository = formRepository;
    }


    @PostMapping(value = "/submit-form")
    public ResponseEntity<?> addFormToAUser(@RequestBody Map<String, Object> param) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
        String formCode = (String) param.get("formCode");
        Form form = formRepository.findFormByFormCode(formCode);
        if (form != null) {
            Map<String, String> params = (HashMap<String, String>) param.get("formDetails");
            FormDetail formDetail = new FormDetail();
            formDetail.setPhoneNumber(params.get("phoneNumber"));
            formDetail.setEmail(params.get("email"));
            com.sg.fsp.model.User reqUser=userService.findByEmail(params.get("email"));
            if(reqUser==null){
                return new ResponseEntity<>("Invalid form data",HttpStatus.BAD_REQUEST);
            }
            else if(!(reqUser.getEmail().equals(user.getEmail())&&reqUser.getFirstName().equals(user.getFirstName())&&reqUser.getLastName().equals(user.getLastName()))) {
                return new ResponseEntity<>("Invalid form data || Not Equal",HttpStatus.BAD_REQUEST);
            }
            formDetail.setFacultyNumber(params.get("facultyNumber"));
            formDetail.setFirstName(params.get("firstName"));
            formDetail.setLastName(params.get("lastName"));
            formDetail.setEnrollmentNumber(params.get("enrollmentNumber"));
            for (FormDetail f : form.getFormDetails()) {
                if (f.getEmail().equals(formDetail.getEmail())) {
                    return new ResponseEntity<>("Form Already Submitted", HttpStatus.ALREADY_REPORTED);
                }
            }
            form.addFormDetails(formDetail);
            formDetail.setForm(form);
            Map<String, String> checkPoints = form.getFormCheckpoints().getCheckPoints();
            UserFormCheckpoints userFormCheckpoints = new UserFormCheckpoints();
            userFormCheckpoints.setFormDetail(formDetail);
            Map<String, Boolean> userCheckpointMap = new HashMap<>();
            Map<String, String> checkpointsTimestamp = new HashMap<>();
            for (Map.Entry<String, String> entry : checkPoints.entrySet()) {
                userCheckpointMap.put(entry.getKey(), false);
                checkpointsTimestamp.put(entry.getKey(), "");
            }
            userFormCheckpoints.setCheckPoints(userCheckpointMap);
            userFormCheckpoints.setCheckPoints_Timestamps(checkpointsTimestamp);
            formDetail.setUserFormCheckpoints(userFormCheckpoints);
            formRepository.save(form);
            user.addForm(form);
            userService.saveUser(user);
            String entryPoint = null;
            String entryPoint_email = null;
            for (Map.Entry<String, Boolean> entry : userCheckpointMap.entrySet()) {
                if (!entry.getValue()) {
                    entryPoint = entry.getKey();
                    break;
                }
            }
            for (Map.Entry<String, String> entry : checkPoints.entrySet()) {
                if (entryPoint.equals(entry.getKey())) {
                    entryPoint_email = entry.getValue();
                    break;
                }
            }
            boolean flag=false;
            com.sg.fsp.model.User entryPointUser = userService.findByEmail(entryPoint_email);
            for(Form f:user.getForms()){
                if(f==form){
                    flag=true;
                }
            }
            if(!flag) {
                entryPointUser.addForm(form);
                form.addUser(entryPointUser);
                userService.saveUser(entryPointUser);
                formRepository.save(form);
            }
            return ResponseEntity.ok().body("Form Submitted");
        } else {
            return new ResponseEntity(HttpStatus.UNAUTHORIZED);
        }
    }


    @GetMapping(value = "/get-forms/{userid}")
    public ResponseEntity<?> getFormsofAUser(@PathVariable Long userid) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User authUser = (User) auth.getPrincipal();
            com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
            if (user.getRole().getUserType() != UserType.STUDENT) {
                com.sg.fsp.model.User targetUser = userService.findUserById(userid);
                List<Form> forms = targetUser.getForms();
                Map<String, Object> res = new HashMap<>();
                res.put("user", targetUser.getEmail());
                res.put("forms", forms);
                return new ResponseEntity<>(res, HttpStatus.OK);
            } else {
                if (userid.equals(user.getId())) {
                    List<Form> forms = user.getForms();
                    Map<String, Object> res = new HashMap<>();
                    res.put("user", user.getEmail());
                    res.put("forms", forms);
                    return new ResponseEntity<>(res, HttpStatus.OK);
                } else {
                    return new ResponseEntity(HttpStatus.UNAUTHORIZED);
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage().trim());
        }
    }

    @GetMapping(value = "/get-all-formDetails/{formCode}")
    public ResponseEntity<?> getAllFormsDetailsofForm(@PathVariable String formCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
        if (user.getRole().getUserType() != UserType.STUDENT) {
            Form form = formRepository.findFormByFormCode(formCode);
            if (form != null) {
                Map<String, Object> res = new HashMap<>();
                res.put("form", form);
                res.put("formDetails", form.getFormDetails());
                return new ResponseEntity<>(res, HttpStatus.OK);
            } else {
                return ResponseEntity.status(405).build();
            }
        } else {
            return ResponseEntity.status(401).build();
        }
    }


    @GetMapping(value = "/get-formDetails/{formCode}")
    public ResponseEntity<?> getFormsDetailsofForm(@PathVariable String formCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
        Form form = formRepository.findFormByFormCode(formCode);
        FormDetail formDetail=null;
        if (form != null) {
            for (FormDetail detail : form.getFormDetails()) {
                if (detail.getEmail().equals(user.getEmail())) {
                    formDetail = detail;
                    break;
                }
            }
            Map<String, Object> res = new HashMap<>();
            formDetail.setForm(null);
            if(formDetail==null){
                return ResponseEntity.status(405).build();
            }
            res.put("formDetail", formDetail);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
        else{
            return ResponseEntity.status(405).build();
        }
    }

    @GetMapping(value = "/get-all-forms")
    public ResponseEntity<?> getAllForms() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
        List<Form> forms = formRepository.findAll();
        Map<String, Object> res = new HashMap<>();
        res.put("forms", forms);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping(value="/checkFormStatus/{formCode}/{userid}")
    public ResponseEntity<?> getFormStatus(@PathVariable String formCode,@PathVariable Long userid){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
        com.sg.fsp.model.User reqUser=userService.findUserById(userid);
        Form form=formRepository.findFormByFormCode(formCode);
        if(form==null){
            return new ResponseEntity<>("No form found!",HttpStatus.NOT_FOUND);
        }
        FormDetail res=null;
        for(FormDetail temp:form.getFormDetails()){
            if(temp.getEmail().equals(reqUser.getEmail())){
                res=temp;
            }
        }
        if(res==null){
            return new ResponseEntity<>("No form detail found!",HttpStatus.NOT_FOUND);
        }
        if(reqUser.getEmail().equals(user.getEmail())){
            Map<String,Object> response=new HashMap<>();
            response.put("formCode",form.getFormCode());
            response.put("userEmail",res.getEmail());
            response.put("formDetailStatus",res.isStatus());
            return new ResponseEntity<>(response,HttpStatus.OK);
        }
        else{
            if(user.getRole().getUserType()!=UserType.STUDENT){
                Map<String,Object> response=new HashMap<>();
                response.put("formCode",form.getFormCode());
                response.put("userEmail",res.getEmail());
                response.put("formDetailStatus",res.isStatus());
                return new ResponseEntity<>(response,HttpStatus.OK);
            }
            else{
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        }
    }


    @GetMapping(value = "/get-form-users/{formCode}")
    public ResponseEntity<?> getAllUsersofForm(@PathVariable String formCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
        if (user.getRole().getUserType() != UserType.STUDENT) {
            Form form = formRepository.findFormByFormCode(formCode);
            if (form != null) {
                ArrayList<com.sg.fsp.model.User> resUser=new ArrayList<>();
                for (com.sg.fsp.model.User u : form.getUsers()) {
                    if(u.getRole().getUserType()==UserType.STUDENT){
                        u.setForms(null);
                        resUser.add(u);
                    }
                }
                Map<String, Object> res = new HashMap<>();
                res.put("form", form);
                res.put("formUsers", resUser);
                return new ResponseEntity<>(res, HttpStatus.OK);
            } else {
                return ResponseEntity.status(405).build();
            }
        } else {
            return ResponseEntity.status(401).build();
        }
    }


    @GetMapping("/get-form-checkpoints/{formCode}")
    public ResponseEntity<?> getFormCheckPoints(@PathVariable String formCode) {
        Form form = formRepository.findFormByFormCode(formCode);
        if (form == null) {
            return new ResponseEntity<>("Form not found", HttpStatus.NOT_FOUND);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("formCode", form.getFormCode());
        res.put("formCheckPoint", form.getFormCheckpoints().getCheckPoints());
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/get-form-checkpoints/{formCode}/{userid}")
    public ResponseEntity<?> getFormCheckpointsForAUserDetail(@PathVariable String formCode, @PathVariable Long userid) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
        Form form = formRepository.findFormByFormCode(formCode);
        UserFormCheckpoints userFormCheckpoints = null;
        com.sg.fsp.model.User user1 = userService.findUserById(userid);
        for (FormDetail formDetail : form.getFormDetails()) {
            if (user1.getEmail().equals(formDetail.getEmail())) {
                userFormCheckpoints = formDetail.getUserFormCheckpoints();
            }
        }
        if (userFormCheckpoints != null) {
            Map<String, Object> res = new HashMap<>();
            res.put("email", user1.getEmail());
            res.put("formCode", formCode);
            res.put("formCheckPointID", userFormCheckpoints.getId());
            res.put("formCheckPoints", userFormCheckpoints.getCheckPoints());
            if (user.getRole().getUserType() != UserType.STUDENT) {
                return new ResponseEntity<>(res, HttpStatus.OK);
            } else {
                if (userid.equals(user1.getId())) {
                    return new ResponseEntity<>(res, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @GetMapping("/get-form-timestamps/{formCode}/{userid}")
    public ResponseEntity<?> getFormTimestampsForAUserDetail(@PathVariable String formCode, @PathVariable Long userid) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        com.sg.fsp.model.User user = userService.findByEmail(authUser.getUsername());
        Form form = formRepository.findFormByFormCode(formCode);
        UserFormCheckpoints userFormCheckpoints = null;
        com.sg.fsp.model.User user1 = userService.findUserById(userid);
        for (FormDetail formDetail : form.getFormDetails()) {
            if (user1.getEmail().equals(formDetail.getEmail())) {
                userFormCheckpoints = formDetail.getUserFormCheckpoints();
            }
        }
        if (userFormCheckpoints != null) {
            Map<String, Object> res = new HashMap<>();
            res.put("email", user1.getEmail());
            res.put("formCode", formCode);
            res.put("formCheckPointID", userFormCheckpoints.getId());
            res.put("formTimestamps", userFormCheckpoints.getCheckPoints_Timestamps());
            if (user.getRole().getUserType() != UserType.STUDENT) {
                return new ResponseEntity<>(res, HttpStatus.OK);
            } else {
                if (userid.equals(user1.getId())) {
                    return new ResponseEntity<>(res, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/checkForm")
    public ResponseEntity<?> checkAndSubmitForm(@RequestBody Map<String, String> params) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User authUser = (User) auth.getPrincipal();
        com.sg.fsp.model.User reqUser = userService.findByEmail(authUser.getUsername());
        if (reqUser.getRole().getUserType() != UserType.STUDENT) {
            String formCode = params.get("formCode");
            boolean status = Boolean.parseBoolean(params.get("formStatus"));
            String email = params.get("email").toString();
            String timeStamp = params.get("timestamp");
            Form form = formRepository.findFormByFormCode(formCode);
            com.sg.fsp.model.User user = userService.findByEmail(email);
            FormDetail res = null;
            for (FormDetail f : form.getFormDetails()) {
                if (f.getEmail().equals(user.getEmail())) {
                    res = f;
                    break;
                }
            }
            if (res == null) {
                return new ResponseEntity<>("Form Detail not found!", HttpStatus.NOT_FOUND);
            }
            if(!res.isStatus()){
                return new ResponseEntity<>("Form status is rejected!", HttpStatus.BAD_REQUEST);
            }
            if(!status){
                res.setStatus(false);
                formRepository.save(form);
                return new ResponseEntity<>("Form rejected!", HttpStatus.OK);
            }
            Map<String, Boolean> userCheckpointMap = new HashMap<>();
            Map<String, String> checkpointsTimestamp = new HashMap<>();
            Map<String, String> emailCheckpoints = new HashMap<>();
            String entryRole = null, nextRole = null;
            String entryEmail = null, nextEmail = null;
            emailCheckpoints = form.getFormCheckpoints().getCheckPoints();
            userCheckpointMap = res.getUserFormCheckpoints().getCheckPoints();
            checkpointsTimestamp = res.getUserFormCheckpoints().getCheckPoints_Timestamps();
            UserFormCheckpoints userFormCheckpoints = res.getUserFormCheckpoints();
            for (Map.Entry<String, String> entry : emailCheckpoints.entrySet()) {
                if (entry.getValue().equals(reqUser.getEmail())) {
                    entryRole = entry.getKey();
                    entryEmail = entry.getValue();
                }
            }
            for (Map.Entry<String, Boolean> entry : userCheckpointMap.entrySet()) {
                if (entryRole.equals(entry.getKey())) {
                    entry.setValue(true);
                }
                if (!entry.getValue()) {
                    nextRole = entry.getKey();
                }
            }
            for (Map.Entry<String, String> entry : checkpointsTimestamp.entrySet()) {
                if (entry.getKey().equals(entryRole)) {
                    entry.setValue(timeStamp);
                }
            }
            if (nextRole != null) {
                for (Map.Entry<String, String> entry : emailCheckpoints.entrySet()) {
                    if (entry.getKey().equals(nextRole)) {
                        nextEmail = entry.getValue();
                    }
                }
                com.sg.fsp.model.User nextUser = userService.findByEmail(nextEmail);
                for (com.sg.fsp.model.User user1 : form.getUsers()) {
                    if (user1.getEmail().equals(nextUser.getEmail())) {
                        return new ResponseEntity<>("Form Already Submitted!", HttpStatus.ALREADY_REPORTED);
                    }
                }
                boolean flag=false;
                for(Form f:nextUser.getForms()){
                    if(f==form){
                        flag=true;
                    }
                }
                if(!flag) {
                    form.addUser(nextUser);
                    nextUser.addForm(form);
                }
                userFormCheckpoints.setCheckPoints_Timestamps(checkpointsTimestamp);
                userFormCheckpoints.setCheckPoints(userCheckpointMap);
                res.setUserFormCheckpoints(userFormCheckpoints);
                userService.saveUser(user);
                formRepository.save(form);
            } else {
                userFormCheckpoints.setCheckPoints_Timestamps(checkpointsTimestamp);
                userFormCheckpoints.setCheckPoints(userCheckpointMap);
                res.setUserFormCheckpoints(userFormCheckpoints);
                userService.saveUser(user);
                formRepository.save(form);
                return new ResponseEntity<>("Form Submitted!", HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok().body("Form checked & forwarded!");
    }

}

