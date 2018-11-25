package com.emotion.ecm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_ACCOUNT")
@SQLDelete(sql = "UPDATE TB_ACCOUNT SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DELETED")
    private boolean deleted;

    @Column(name = "TPS")
    private short tps;

}
