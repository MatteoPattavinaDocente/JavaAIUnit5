package it.epicode.demo.allegati.dto;

import it.epicode.demo.allegati.model.Attachment;

public record AttachmentDto(Long id, String originalName, String contentType, long sizeBytes, String storageKey) {

    public static AttachmentDto from(Attachment attachment) {
        return new AttachmentDto(attachment.getId(), attachment.getOriginalName(),
                attachment.getContentType(), attachment.getSizeBytes(), attachment.getStorageKey());
    }
}
