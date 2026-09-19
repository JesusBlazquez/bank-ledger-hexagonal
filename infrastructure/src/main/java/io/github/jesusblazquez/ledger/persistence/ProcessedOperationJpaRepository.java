package io.github.jesusblazquez.ledger.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProcessedOperationJpaRepository extends JpaRepository<ProcessedOperationEntity, String> {}
