package com.sg.fsp.model;

import com.sg.fsp.enums.Permission;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Privilege {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Enumerated(value = EnumType.STRING)
    private Permission permission;


    public Privilege(Permission permission) {
        this.permission = permission;
    }

}
