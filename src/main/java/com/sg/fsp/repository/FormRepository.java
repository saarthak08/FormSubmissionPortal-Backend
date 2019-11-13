package com.sg.fsp.repository;

import com.sg.fsp.model.Form;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FormRepository extends JpaRepository<Form, Integer> {
    Form findFormByFormCode(String formCode);
}
