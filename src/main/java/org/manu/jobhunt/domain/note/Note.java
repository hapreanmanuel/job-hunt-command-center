package org.manu.jobhunt.domain.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/** A dated line about one job, such as where the posting was found. */
@AllArgsConstructor
@Builder
@Getter
public class Note {

    private Long id;
    private Long jobId;
    private String text;
    private Instant createdAt;
}
