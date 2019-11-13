package com.sg.fsp.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Entity
@Data
public class FormCheckpoints implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "form_detail_id")
    private FormDetail formDetail;

    @OneToOne
    @JoinColumn(name = "form_id")
    private Form form;

    @ElementCollection
    @CollectionTable(name = "form_checkpoint_map",joinColumns = @JoinColumn(name = "form_checkpoint_id",referencedColumnName = "id"))
    @Column(name="value")
    @MapKeyColumn(name = "name")
    Map<String, Boolean> checkPoints=new HashMap<String,Boolean>();

}
