package com.sg.fsp.service;


import com.sg.fsp.enums.Permission;
import com.sg.fsp.enums.UserType;
import com.sg.fsp.model.*;
import com.sg.fsp.repository.PrivilegeRepository;
import com.sg.fsp.repository.RoleRepository;
import com.sg.fsp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Component
public class InitialDataLoader implements
        ApplicationListener<ContextRefreshedEvent> {

    boolean alreadySetup = false;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PrivilegeRepository privilegeRepository;
    private PasswordEncoder passwordEncoder;
    public Role Student;
    public Role Dean;
    public Role Controller;


    @Autowired
    public InitialDataLoader(UserRepository userRepository, RoleRepository roleRepository,PrivilegeRepository privilegeRepository, PasswordEncoder passwordEncoder){
        this.userRepository=userRepository;
        this.roleRepository=roleRepository;
        this.privilegeRepository=privilegeRepository;
        this.passwordEncoder=passwordEncoder;
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


            List<Privilege> adminPrivileges = Arrays.asList(
                    readPrivilege, writePrivilege, enableUser, submitForm, checkForm);

            createRoleIfNotFound(UserType.ADMIN, adminPrivileges);
            Student = createRoleIfNotFound(UserType.STUDENT, Arrays.asList(submitForm));
            Controller = createRoleIfNotFound(UserType.CONTROLLER, Arrays.asList(checkForm, readPrivilege));
            Dean = createRoleIfNotFound(UserType.DEAN, Arrays.asList(checkForm, readPrivilege));

            List<Privilege> sd = Arrays.asList(
                    readPrivilege, writePrivilege);


            Role adminRole = roleRepository.findByUserType(UserType.ADMIN);
            User user = new User();
            user.setFirstName("ADMIN");
            user.setLastName("AMU");
            user.setEnabled(true);
            user.setPassword(passwordEncoder.encode("ZHCET"));
            user.setEmail("fsp_admin@myamu.ac.in");
            user.setRole(adminRole);
            userRepository.save(user);
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