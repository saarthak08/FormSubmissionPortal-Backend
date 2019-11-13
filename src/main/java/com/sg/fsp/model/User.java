package com.sg.fsp.model;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Email;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Entity
@Data
@Table(name = "user")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    @Email(message =  "Please provide a valid e-mail")
    @NotEmpty(message = "Please provide an e-mail")
    private String email;

    @Column(name = "password")
    @Transient
    @JsonIgnore
    private String password;

    @Column(name = "first_name")
    @NotEmpty(message = "Please provide your first name")
    private String firstName;

    @Column(name = "last_name")
    @NotEmpty(message = "Please provide your last name")
    private String lastName;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "confirmation_token")
    @JsonIgnore
    private String confirmationToken;

    @Column(name="faculty_number")
    private String facultyNumber;


    @Column(name = "employee_number")
    private String employeeNumber;

    @ManyToOne
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(
                    name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(
                    name = "role_id", referencedColumnName = "id"))
    private Role role;


    @ManyToMany
    @JoinTable(
            name = "users_forms",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "form_id")
    )
    private List<Form> forms;


    public void addForm(Form form){
        if(forms==null){
            forms=new ArrayList<>();
        }
        forms.add(form);
    }

    public void deleteForm(Form form){
        if(forms!=null) {
            forms.remove(form);
        }
    }

    public void updateForm(Form form){
        if(forms!=null) {
            for (Form temp : forms) {
                if (temp.getId() == form.getId()) {
                    forms.remove(temp);
                    forms.add(form);
                }
            }
        }
    }

}
