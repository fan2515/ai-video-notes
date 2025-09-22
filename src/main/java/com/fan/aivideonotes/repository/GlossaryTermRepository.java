package com.fan.aivideonotes.repository;

import com.fan.aivideonotes.model.GlossaryTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GlossaryTermRepository extends JpaRepository<GlossaryTerm, Long> {

    // 根据术语名称精确查找
    Optional<GlossaryTerm> findByTerm(String term);
}