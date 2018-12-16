package com.emotion.ecm.model;

import com.emotion.ecm.enums.PreviewStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_SMS_PREVIEW")
@SQLDelete(sql = "UPDATE TB_SMS_PREVIEW SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class SmsPreview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "CREATE_DATE")
    private LocalDateTime createDate;

    @Column(name = "UPDATE_DATE")
    private LocalDateTime updateDate;

    @Column(name = "SEND_DATE")
    private LocalDateTime sendDate;

    @Column(name = "TEXT")
    private String text;

    @Column(name = "TPS")
    private short tps;

    @Column(name = "RECIPIENTS_COUNT")
    private int recipientsCount;

    @Column(name = "TOTAL_PARTS")
    private int totalParts;

    @Column(name = "SENT_PARTS")
    private int sentParts;

    @Column(name = "PREVIEW_STATUS")
    @Enumerated(EnumType.ORDINAL)
    private PreviewStatus previewStatus;

    @Column(name = "DLR")
    private boolean dlr;

    @Column(name = "DELETED")
    private boolean deleted;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID")
    private AppUser user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SMS_PRIORITY_ID")
    private SmsPriority smsPriority;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SMS_TYPE_ID")
    private SmsType smsType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "EXPIRATION_TIME_ID")
    private ExpirationTime expirationTime;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SMPP_ADDRESS_ID")
    private SmppAddress smppAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACCOUNT_DATA_ID")
    private AccountData accountData;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_PREVIEW_GROUP",
            joinColumns = @JoinColumn(name = "PREVIEW_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "GROUP_ID", referencedColumnName = "ID"))
    private Set<Group> groups;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_PREVIEW_CONTACT",
            joinColumns = @JoinColumn(name = "PREVIEW_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "CONTACT_ID", referencedColumnName = "ID"))
    private Set<Contact> contacts;
}
