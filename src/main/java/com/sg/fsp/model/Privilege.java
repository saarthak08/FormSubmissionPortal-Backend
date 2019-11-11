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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @Enumerated(value = EnumType.STRING)
    private Permission permission;


    @ManyToMany(mappedBy = "privileges")
    private Collection<Role> roles;

    public Privilege(Permission permission) {
        this.permission = permission;
    }

}
