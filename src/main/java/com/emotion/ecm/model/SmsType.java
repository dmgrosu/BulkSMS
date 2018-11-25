package com.emotion.ecm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Column;
import javax.persistence.GenerationType;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_SMS_TYPE")
@SQLDelete(sql = "UPDATE TB_SMS_TYPE SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class SmsType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "DELETED")
    private boolean deleted;

}
