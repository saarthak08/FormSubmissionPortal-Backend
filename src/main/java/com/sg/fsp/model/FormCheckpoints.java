package com.sg.fsp.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Entity(name = "form_checkpoint_id")
@Embeddable
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Data
public class FormCheckpoints implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "form_id")
    private Form form;

    @ElementCollection
    @CollectionTable(name = "form_checkpoints",joinColumns = @JoinColumn(name = "form_checkpoint_id",referencedColumnName = "id"))
    @Column(name="value")
    @MapKeyColumn(name = "name")
    @OrderColumn
    Map<String, String> checkPoints=new HashMap<String,String>();


}
