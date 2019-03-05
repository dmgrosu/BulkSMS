package com.emotion.ecm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_SMSC_ACCOUNT")
@SQLDelete(sql = "UPDATE TB_SMSC_ACCOUNT SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class SmscAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "SYSTEM_ID")
    private String systemId;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "IP_ADDRESS")
    private String ipAddress;

    @Column(name = "PORT")
    private int port;

    @Column(name = "SYSTEM_TYPE")
    private String systemType;

    @Column(name = "TPS")
    private short tps;

    @Column(name = "ASYNCHRONOUS")
    private boolean asynchronous;

    @Column(name = "MAX_CONNECTIONS")
    private byte maxConnections;

    @Column(name = "DELETED")
    private boolean deleted;

}
