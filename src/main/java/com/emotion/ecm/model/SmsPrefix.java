package com.emotion.ecm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.util.Lazy;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
@Table(name = "SMS_PREFIX")
@SQLDelete(sql = "UPDATE SMS_PREFIX SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class SmsPrefix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "PREFIX")
    private String prefix;

    @Column(name = "DELETED")
    private boolean deleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SMS_PREFIX_GROUP_ID")
    private SmsPrefixGroup prefixGroup;

}
