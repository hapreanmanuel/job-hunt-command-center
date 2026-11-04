package org.manu.jobhunt.application.tracking;

import lombok.RequiredArgsConstructor;
import org.manu.jobhunt.application.tracking.command.SaveRoundCommand;
import org.manu.jobhunt.application.tracking.exception.RoundNotFoundException;
import org.manu.jobhunt.domain.round.Round;
import org.manu.jobhunt.domain.round.RoundOutcome;
import org.manu.jobhunt.domain.round.RoundRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static org.manu.jobhunt.application.common.Validations.requireBetween;
import static org.manu.jobhunt.application.common.Validations.requireMaxLength;
import static org.manu.jobhunt.application.common.Validations.requireNonNull;

/**
 * Round validation and building rounds from commands. Package-private: only
 * TrackingApplication uses it.
 */
@Service
@RequiredArgsConstructor
class RoundService {

    private static final int MIN_SCORE = 1;
    private static final int MAX_SCORE = 5;
    private static final int MAX_TEXT_LENGTH = 20_000;

    private final RoundRepository roundRepository;

    Round requireRound(Long id, UUID ownerId) {
        return roundRepository.findById(id, ownerId).orElseThrow(() -> new RoundNotFoundException(id));
    }

    void validate(SaveRoundCommand command) {
        requireNonNull(command.getType(), "type");
        requireBetween(command.getFeltScore(), MIN_SCORE, MAX_SCORE, "feltScore");
        requireBetween(command.getDifficultyScore(), MIN_SCORE, MAX_SCORE, "difficultyScore");
        requireMaxLength(command.getQuestionsAsked(), MAX_TEXT_LENGTH, "questionsAsked");
        requireMaxLength(command.getDebrief(), MAX_TEXT_LENGTH, "debrief");
    }

    Round build(SaveRoundCommand command) {
        return filled(Round.builder(), command).build();
    }

    /** The given round with the command's fields; what the command doesn't cover stays. */
    Round applied(Round round, SaveRoundCommand command) {
        return filled(round.toBuilder(), command).build();
    }

    private Round.RoundBuilder filled(Round.RoundBuilder builder, SaveRoundCommand command) {
        return builder
                .type(command.getType())
                .scheduledAt(command.getScheduledAt())
                .outcome(command.getOutcome() != null ? command.getOutcome() : RoundOutcome.PENDING)
                .feltScore(command.getFeltScore())
                .difficultyScore(command.getDifficultyScore())
                .questionsAsked(command.getQuestionsAsked())
                .debrief(command.getDebrief());
    }
}
