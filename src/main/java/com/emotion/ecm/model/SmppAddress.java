package com.emotion.ecm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.TypeOfNumber;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_SMPP_ADDRESS")
@SQLDelete(sql = "UPDATE TB_SMPP_ADDRESS SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class SmppAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "TON")
    @Enumerated(EnumType.ORDINAL)
    private TypeOfNumber ton;

    @Column(name = "NPI")
    @Enumerated(EnumType.STRING)
    private NumberingPlanIndicator npi;

    @Column(name = "DELETED")
    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;
}
