package com.emotion.ecm.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_SMS_TEXT")
public class SmsText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "PART")
    private short part;

    @Column(name = "TEXT")
    private String text;

}
