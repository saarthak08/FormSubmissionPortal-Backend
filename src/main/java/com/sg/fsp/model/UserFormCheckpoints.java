package com.sg.fsp.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@Embeddable
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class UserFormCheckpoints implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_detail_id")
    private FormDetail formDetail;


    @ElementCollection
    @CollectionTable(name = "user_form_checkpoint_map",joinColumns = @JoinColumn(name = "user_form_checkpoint_id",referencedColumnName = "id"))
    @Column(name="value")
    @MapKeyColumn(name = "name")
    @OrderColumn
    Map<String, Boolean> checkPoints=new HashMap<String,Boolean>();


    @ElementCollection
    @CollectionTable(name = "user_form_checkpoint_timestamp",joinColumns = @JoinColumn(name = "user_form_checkpoint_id",referencedColumnName = "id"))
    @Column(name="value")
    @MapKeyColumn(name = "name")
    @OrderColumn
    Map<String, String> checkPoints_Timestamps=new HashMap<>();

}
