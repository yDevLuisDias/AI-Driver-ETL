package com.ydevluisdias.AiDriveEtl.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ydevluisdias.AiDriveEtl.entity.LeadEntity;

public interface LeadRepository extends JpaRepository<LeadEntity, UUID> {

}