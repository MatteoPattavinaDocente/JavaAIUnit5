package it.epicode.demo.allegati.repository;

import it.epicode.demo.allegati.model.Attachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByDocumentId(Long documentId);
}
