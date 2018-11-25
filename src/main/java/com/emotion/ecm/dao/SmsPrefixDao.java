package com.emotion.ecm.dao;

import com.emotion.ecm.model.SmsPrefix;
import com.emotion.ecm.model.SmsPrefixGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SmsPrefixDao extends JpaRepository<SmsPrefix, Integer> {

    Optional<SmsPrefix> findByPrefixGroupAndPrefix(SmsPrefixGroup group, String prefix);

    List<SmsPrefix> findAllByPrefixGroup(SmsPrefixGroup group);

}
