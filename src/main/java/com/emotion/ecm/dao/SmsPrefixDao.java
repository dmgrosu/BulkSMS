package com.emotion.ecm.dao;

import com.emotion.ecm.exception.PrefixException;
import com.emotion.ecm.model.SmsPrefix;
import com.emotion.ecm.model.SmsPrefixGroup;
import com.emotion.ecm.model.dto.PrefixDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SmsPrefixDao extends JpaRepository<SmsPrefix, Integer> {

    Optional<SmsPrefix> findByPrefixGroupIdAndPrefix(int groupId, String prefix);

    List<SmsPrefix> findAllByPrefixGroup(SmsPrefixGroup group);

    @Query("select new com.emotion.ecm.model.dto.PrefixDto" +
            "(p.id, p.prefix, p.prefixGroup.id) " +
            "from SmsPrefix p " +
            "where id = ?1")
    PrefixDto findDtoById(int id) throws PrefixException;
}
