package com.emotion.ecm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_SMS_TEXT")
@SQLDelete(sql = "UPDATE TB_SMS_TEXT SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class SmsText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "PART")
    private short part;

    @Column(name = "TEXT")
    private String text;

    @Column(name = "DELETED")
    private boolean deleted;

}
