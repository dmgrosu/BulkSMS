package com.emotion.ecm.dao;

import com.emotion.ecm.model.SmsPriority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SmsPriorityDao extends JpaRepository<SmsPriority, Integer> {
}
