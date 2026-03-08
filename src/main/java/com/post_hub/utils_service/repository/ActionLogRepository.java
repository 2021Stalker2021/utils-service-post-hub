package com.post_hub.utils_service.repository;

import com.post_hub.utils_service.entity.ActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActionLogRepository extends JpaRepository<ActionLog, Integer>, JpaSpecificationExecutor<ActionLog> {

    Optional<ActionLog> findByIdAndUserId(Integer logId, Integer userId);

    @Modifying
    @Query(value = "UPDATE ActionLog as al SET al.isRead = true WHERE al.id IN ?1")
    Integer setIsReadEqualsTrue(List<Integer> ids); // возвращает количество обновленных записей (как Integer)
}
