package com.emotion.ecm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_BLACK_LIST_MSISDN")
@SQLDelete(sql = "UPDATE TB_BLACK_LIST_MSISDN SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class BlackListMsisdn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "DELETED")
    private boolean deleted;

    @Column(name = "MSISDN")
    private String msisdn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BLACK_LIST_ID")
    private BlackList blackList;

}
