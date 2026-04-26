package com.banking.orchestration.repository;

import com.banking.orchestration.domain.AiModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiModelRepository extends JpaRepository<AiModel, UUID> {

    @Query("SELECT am FROM AiModel am WHERE am.id = :id AND am.deletedAt IS NULL")
    Optional<AiModel> findByIdAndNotDeleted(@Param("id") UUID id);

    @Query("SELECT am FROM AiModel am WHERE am.name = :name AND am.deletedAt IS NULL")
    Optional<AiModel> findByNameAndNotDeleted(@Param("name") String name);

    @Query("SELECT am FROM AiModel am WHERE am.provider = :provider AND am.enabled = true AND am.deletedAt IS NULL ORDER BY am.priority DESC")
    List<AiModel> findByProviderAndEnabledOrderByPriority(@Param("provider") String provider);

    @Query("SELECT am FROM AiModel am WHERE am.enabled = true AND am.deletedAt IS NULL ORDER BY am.priority DESC")
    List<AiModel> findAllEnabledOrderByPriority();

    @Query("SELECT am FROM AiModel am WHERE am.deletedAt IS NULL ORDER BY am.priority DESC")
    List<AiModel> findAllNotDeleted();
}
