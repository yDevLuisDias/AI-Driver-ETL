package com.ydevluisdias.AiDriveEtl.repository;

import com.ydevluisdias.AiDriveEtl.entity.LeadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LeadRepository extends JpaRepository<LeadEntity, UUID> {
}