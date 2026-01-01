package com.risk.scoring.repository;

import com.riskplatform.common.entity.RiskRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiskRuleRepository extends MongoRepository<RiskRule, String> {

    Optional<RiskRule> findByRuleName(String ruleName);

    List<RiskRule> findByEnabledIsTrue();
}