package com.usei.usei.repositories;

import com.usei.usei.models.LogRol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRolDAO extends JpaRepository<LogRol, Long> {
}
