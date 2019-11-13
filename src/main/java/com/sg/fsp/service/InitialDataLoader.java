package com.sg.fsp.service;


import com.sg.fsp.enums.Permission;
import com.sg.fsp.enums.UserType;
import com.sg.fsp.model.*;
import com.sg.fsp.repository.FormRepository;
import com.sg.fsp.repository.PrivilegeRepository;
import com.sg.fsp.repository.RoleRepository;
import com.sg.fsp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Query;
import java.util.*;

@Component
public class InitialDataLoader implements
        ApplicationListener<ContextRefreshedEvent> {

    boolean alreadySetup = false;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PrivilegeRepository privilegeRepository;
    private PasswordEncoder passwordEncoder;
    private FormRepository formRepository;


    @Autowired
    public InitialDataLoader(UserRepository userRepository, RoleRepository roleRepository,PrivilegeRepository privilegeRepository, PasswordEncoder passwordEncoder, FormRepository formRepository){
        this.userRepository=userRepository;
        this.roleRepository=roleRepository;
        this.privilegeRepository=privilegeRepository;
        this.passwordEncoder=passwordEncoder;
        this.formRepository=formRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {


        if (alreadySetup){
            return;
        }

        User adminUser=userRepository.findByEmail("fsp_admin@myamu.ac.in");
        if(adminUser==null) {
            Privilege readPrivilege
                    = createPrivilegeIfNotFound(Permission.READ_USERS);
            Privilege writePrivilege
                    = createPrivilegeIfNotFound(Permission.WRITE_USERS);
            Privilege submitForm
                    = createPrivilegeIfNotFound(Permission.SUBMIT_FORM);
            Privilege checkForm
                    = createPrivilegeIfNotFound(Permission.CHECK_FORM);
            Privilege enableUser
                    = createPrivilegeIfNotFound(Permission.ENABLE_USER);
            Privilege addForm
                    =createPrivilegeIfNotFound(Permission.ADD_FORM);


            List<Privilege> adminPrivileges = Arrays.asList(
                    readPrivilege, writePrivilege, enableUser, submitForm, checkForm, addForm);

            createRoleIfNotFound(UserType.ADMIN, adminPrivileges);
            Role student = createRoleIfNotFound(UserType.STUDENT, Arrays.asList(submitForm));
            Role controller = createRoleIfNotFound(UserType.CONTROLLER, Arrays.asList(checkForm, readPrivilege));
            Role dean = createRoleIfNotFound(UserType.DEAN, Arrays.asList(checkForm, readPrivilege));

            Role adminRole = roleRepository.findByUserType(UserType.ADMIN);
            User user = new User();
            user.setFirstName("ADMIN");
            user.setLastName("AMU");
            user.setEnabled(true);
            user.setPassword(passwordEncoder.encode("ZHCET"));
            user.setEmail("fsp_admin@myamu.ac.in");
            user.setRole(adminRole);
            userRepository.save(user);
            adminUser=userRepository.findByEmail("fsp_admin@myamu.ac.in");
            Form form=new Form();
            form.setDepartment("Computer");
            form.setTitle("Continuation-Form");
            form.setFormCode("CONZHCETCE2019");
            form.addUser(adminUser);
            adminUser.addForm(form);
            FormDetail formDetail=new FormDetail();
            formDetail.setEnrollmentNumber("GI0471");
            formDetail.setFirstName("ADMIN");
            formDetail.setLastName("AMU");
            formDetail.setEmail("fsp_admin@myamu.ac.in");
            formDetail.setPhoneNumber("100");
            formDetail.setFacultyNumber("17COB041");
            formDetail.setForm(form);
            form.addFormDetails(formDetail);
            FormCheckpoints formCheckpoints=new FormCheckpoints();
            formCheckpoints.setFormDetail(formDetail);
            formDetail.setFormCheckpoints(formCheckpoints);
            Map<String, Boolean> checkPoint=new HashMap<>();
            checkPoint.put(UserType.CONTROLLER.name(),false);
            checkPoint.put(UserType.DEAN.name(),false);
            formCheckpoints.setCheckPoints(checkPoint);
            form.setFormCheckpoints(formCheckpoints);
            formCheckpoints.setForm(form);
            formRepository.save(form);
            userRepository.save(adminUser);

            Form form2=new Form();
            form2.setDepartment("Electronics");
            form2.setTitle("Second-Form");
            form2.setFormCode("FORM2");
            form2.addUser(adminUser);
            formDetail.setForm(form2);
            form2.addFormDetails(formDetail);
            formCheckpoints=new FormCheckpoints();
            formCheckpoints.setFormDetail(formDetail);
            formDetail.setFormCheckpoints(formCheckpoints);
            formCheckpoints.setCheckPoints(checkPoint);
            form2.setFormCheckpoints(formCheckpoints);
            formCheckpoints.setForm(form2);
            formRepository.save(form2);
            adminUser.addForm(form2);
            userRepository.save(adminUser);
        }
        alreadySetup = true;
    }

    @Transactional
    Privilege createPrivilegeIfNotFound(Permission name) {

        Privilege privilege = privilegeRepository.findByPermission(name);
        if (privilege == null) {
            privilege = new Privilege(name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    Role createRoleIfNotFound(
            UserType name, Collection<Privilege> privileges) {

        Role role = roleRepository.findByUserType(name);
        if (role == null) {
            role = new Role(name);
            role.setPrivileges(privileges);
            roleRepository.save(role);
        }
        return role;
    }
}