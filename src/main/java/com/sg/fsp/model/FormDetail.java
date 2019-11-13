package com.sg.fsp.model;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Data
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class FormDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @NotEmpty
    private String facultyNumber;

    @NotEmpty
    private String email;

    @NotEmpty
    private String phoneNumber;

    @NotEmpty(message = "Provide your enrollment number.")
    private String enrollmentNumber;


    @ManyToOne(cascade = {CascadeType.DETACH,CascadeType.MERGE,CascadeType.PERSIST,CascadeType.REFRESH})
    @JoinColumn(name = "form_id")
    private Form form;


    @JsonIgnore
    @OneToOne(mappedBy = "formDetail",cascade = CascadeType.ALL)
    private FormCheckpoints formCheckpoints;


}
