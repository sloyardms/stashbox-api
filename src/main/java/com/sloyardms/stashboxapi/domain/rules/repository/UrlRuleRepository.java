package com.sloyardms.stashboxapi.domain.rules.repository;

import com.sloyardms.stashboxapi.domain.rules.model.UrlRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UrlRuleRepository extends JpaRepository<UrlRule, UUID> { }
