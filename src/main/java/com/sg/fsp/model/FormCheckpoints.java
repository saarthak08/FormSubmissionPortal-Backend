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
public class FormCheckpoints implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_detail_id")
    private FormDetail formDetail;

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "form_id")
    private Form form;

    @ElementCollection
    @CollectionTable(name = "form_checkpoint_map",joinColumns = @JoinColumn(name = "form_checkpoint_id",referencedColumnName = "id"))
    @Column(name="value")
    @MapKeyColumn(name = "name")
    @OrderColumn
    Map<String, Boolean> checkPoints=new HashMap<String,Boolean>();

}
