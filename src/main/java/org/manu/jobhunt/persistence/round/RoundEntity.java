package org.manu.jobhunt.persistence.round;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.manu.jobhunt.domain.round.Round;
import org.manu.jobhunt.domain.round.RoundOutcome;
import org.manu.jobhunt.domain.round.RoundType;
import org.manu.jobhunt.persistence.AuditedEntity;
import org.manu.jobhunt.persistence.job.JobEntity;

import java.time.Instant;

@Entity
@Table(name = "round")
@Getter
@Setter
@NoArgsConstructor
public class RoundEntity extends AuditedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobEntity job;

    @Enumerated(EnumType.STRING)
    @Column(name = "round_type", nullable = false, length = 30)
    private RoundType type;

    @Column(name = "scheduled_at")
    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoundOutcome outcome;

    @Column(name = "felt_score")
    private Short feltScore;

    @Column(name = "difficulty_score")
    private Short difficultyScore;

    @Column(name = "questions_asked", columnDefinition = "text")
    private String questionsAsked;

    @Column(columnDefinition = "text")
    private String debrief;

    void copyFrom(Round round) {
        type = round.getType();
        scheduledAt = round.getScheduledAt();
        outcome = round.getOutcome();
        feltScore = round.getFeltScore();
        difficultyScore = round.getDifficultyScore();
        questionsAsked = round.getQuestionsAsked();
        debrief = round.getDebrief();
    }

    /** Reads the job, so it must run inside the transaction. */
    Round toDomain() {
        return Round.builder()
                .id(getId())
                .job(job.toDomain())
                .type(type)
                .scheduledAt(scheduledAt)
                .outcome(outcome)
                .feltScore(feltScore)
                .difficultyScore(difficultyScore)
                .questionsAsked(questionsAsked)
                .debrief(debrief)
                .createdAt(getCreatedAt())
                .updatedAt(getUpdatedAt())
                .build();
    }
}
