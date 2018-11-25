package com.emotion.ecm.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "TB_CONTACT")
@SQLDelete(sql = "UPDATE TB_CONTACT SET deleted=true WHERE id=?")
@Where(clause = "deleted=false")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "PHONE_NUMBER")
    private String mobilePhone;

    @Column(name = "DELETED")
    private boolean deleted;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "TB_CONTACT_GROUP",
            joinColumns = @JoinColumn(name = "CONTACT_ID", referencedColumnName = "ID"),
            inverseJoinColumns = @JoinColumn(name = "GROUP_ID", referencedColumnName = "ID"))
    private Set<Group> groups;

}
