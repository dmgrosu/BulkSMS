package com.emotion.ecm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import javax.persistence.*;


@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_SMSC_ACCOUNT")
@Where(clause = "DELETED=0")
public class SmscAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "IP_ADDRESS")
    private String ipAddress;

    @Column(name = "PORT")
    private int port;

    @Column(name = "TPS")
    private short tps;

    @Column(name = "DELETED")
    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

}
