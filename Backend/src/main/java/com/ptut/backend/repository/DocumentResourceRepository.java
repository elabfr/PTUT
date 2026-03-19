package com.ptut.backend.repository;

import com.ptut.backend.dto.ResourceSummaryResponse;
import com.ptut.backend.entity.DocumentResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DocumentResourceRepository extends JpaRepository<DocumentResource, Long> {

    @Query("""
            SELECT new com.ptut.backend.dto.ResourceSummaryResponse(
                r.id,
                r.fileName,
                r.contentType,
                r.size,
                r.uploadedAt,
                r.uploadedBy
            )
            FROM DocumentResource r
            ORDER BY r.uploadedAt DESC
            """)
    List<ResourceSummaryResponse> findAllSummariesOrderByUploadedAtDesc();
}
