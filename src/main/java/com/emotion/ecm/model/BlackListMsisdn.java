package com.emotion.ecm.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "TB_BLACK_LIST_MSISDN")
public class BlackListMsisdn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "MSISDN")
    private String msisdn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BLACK_LIST_ID")
    private BlackList blackList;

    public BlackListMsisdn() {
    }

    public BlackListMsisdn(String msisdn, BlackList blackList) {
        this.msisdn = msisdn;
        this.blackList = blackList;
    }
}
