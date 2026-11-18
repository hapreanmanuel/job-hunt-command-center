package org.manu.jobhunt.persistence.note;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.manu.jobhunt.domain.note.Note;
import org.manu.jobhunt.persistence.AuditedEntity;

/** Holds the job's id rather than the job: a note never needs the job's details. */
@Entity
@Table(name = "note")
@Getter
@Setter
@NoArgsConstructor
public class NoteEntity extends AuditedEntity {

    @Column(name = "job_id", nullable = false, updatable = false)
    private Long jobId;

    @Column(name = "note_text", nullable = false, columnDefinition = "text")
    private String text;

    Note toDomain() {
        return Note.builder()
                .id(getId())
                .jobId(jobId)
                .text(text)
                .createdAt(getCreatedAt())
                .build();
    }
}
