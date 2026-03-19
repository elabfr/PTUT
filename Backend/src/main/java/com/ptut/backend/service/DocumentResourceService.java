package com.ptut.backend.service;

import com.ptut.backend.dto.ResourceSummaryResponse;
import com.ptut.backend.entity.DocumentResource;
import com.ptut.backend.repository.DocumentResourceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Service
public class DocumentResourceService {

    private final DocumentResourceRepository documentResourceRepository;

    public DocumentResourceService(DocumentResourceRepository documentResourceRepository) {
        this.documentResourceRepository = documentResourceRepository;
    }

    public ResourceSummaryResponse upload(MultipartFile file, String uploadedBy) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le fichier est obligatoire");
        }

        DocumentResource resource = new DocumentResource();
        resource.setFileName(file.getOriginalFilename());
        resource.setContentType(file.getContentType());
        resource.setSize(file.getSize());
        resource.setUploadedAt(Instant.now());
        resource.setUploadedBy(uploadedBy);

        try {
            resource.setContent(file.getBytes());
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Impossible de lire le fichier");
        }

        DocumentResource saved = documentResourceRepository.save(resource);
        return toSummary(saved);
    }

    public List<ResourceSummaryResponse> listAll() {
        return documentResourceRepository.findAllSummariesOrderByUploadedAtDesc();
    }

    public DocumentResource getById(Long id) {
        return documentResourceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource introuvable"));
    }

    public void deleteById(Long id) {
        if (!documentResourceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ressource introuvable");
        }
        documentResourceRepository.deleteById(id);
    }

    private ResourceSummaryResponse toSummary(DocumentResource resource) {
        ResourceSummaryResponse response = new ResourceSummaryResponse();
        response.setId(resource.getId());
        response.setFileName(resource.getFileName());
        response.setContentType(resource.getContentType());
        response.setSize(resource.getSize());
        response.setUploadedAt(resource.getUploadedAt());
        response.setUploadedBy(resource.getUploadedBy());
        return response;
    }
}
