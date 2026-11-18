package org.manu.jobhunt.api.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.manu.jobhunt.domain.note.Note;

import java.time.Instant;

@AllArgsConstructor
@Getter
public class NoteResponse {

    private Long id;
    private Long jobId;
    private String text;
    private Instant createdAt;

    public static NoteResponse toDto(Note note) {
        return new NoteResponse(note.getId(), note.getJobId(), note.getText(), note.getCreatedAt());
    }
}
