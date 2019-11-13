package com.sg.fsp.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Form {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty
    private String title;

    @NotEmpty
    @Column(unique = true)
    private String formCode;

    @NotEmpty
    private String department;

    @OneToMany(mappedBy = "form",cascade = CascadeType.ALL)
    @JsonBackReference
    private List<FormDetail> formDetails;


    @ManyToMany(mappedBy = "forms")
    @JsonBackReference
    private List<User> users;


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
