package com.sg.fsp.repository;

import com.sg.fsp.model.Role;
import com.sg.fsp.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Role findByUserType(UserType name);
}
