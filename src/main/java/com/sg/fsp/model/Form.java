package com.sg.fsp.model;


import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Form {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    @Column(unique = true)
    private String formCode;

    @NotEmpty
    private String department;

    @OneToMany(mappedBy = "form",cascade = CascadeType.ALL)
    @JsonIgnore
    private List<FormDetail> formDetails;


    @ManyToMany(mappedBy = "forms")
    @JsonIgnore
    private List<User> users;

    @JsonIgnore
    @OneToOne(mappedBy = "form",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private FormCheckpoints formCheckpoints;


    public void addFormDetails(FormDetail formDetail){
        if(formDetails ==null){
            formDetails =new ArrayList<>();
        }
        formDetails.add(formDetail);
    }

    public void updateFormDetails(FormDetail formDetail){
        if(formDetails!=null){
            for(FormDetail f:formDetails){
                if(f.getFacultyNumber().equals(formDetail.getFacultyNumber())){
                    formDetails.remove(f);
                    formDetails.add(formDetail);
                }
            }
        }
    }

    public void addUser(User user){
        if(users==null){
            users=new ArrayList<>();
        }
        users.add(user);
    }


    public void updateUser(User user){
        if(users!=null){
            for(User u:users){
                if(u.getId()==user.getId()){
                    users.remove(u);
                    users.add(user);
                }
            }
        }
    }

}
