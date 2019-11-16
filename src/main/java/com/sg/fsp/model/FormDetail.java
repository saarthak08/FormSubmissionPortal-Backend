package com.sg.fsp.model;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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


    @ManyToOne()
    @JoinColumn(name = "form_id")
    private Form form;


    @JsonIgnore
    @OneToOne(mappedBy = "formDetail",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private UserFormCheckpoints userFormCheckpoints;


}
