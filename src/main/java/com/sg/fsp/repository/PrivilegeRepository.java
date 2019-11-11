package com.sg.fsp.repository;

import com.sg.fsp.enums.Permission;
import com.sg.fsp.model.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege,Long> {
    Privilege findByPermission(Permission name);
}
