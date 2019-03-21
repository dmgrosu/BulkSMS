package com.emotion.ecm.model;

import com.emotion.ecm.enums.RoleName;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "TB_ROLE")
public class AppRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "NAME")
    @Enumerated(EnumType.STRING)
    private RoleName name;

    public AppRole(RoleName name) {
        this.name = name;
    }
}
