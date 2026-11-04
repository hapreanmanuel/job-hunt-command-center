package org.manu.jobhunt.application.overview;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.manu.jobhunt.domain.round.Round;

/** Where one job stands right now. */
@AllArgsConstructor
@Getter
public class Assessment {

    private final Situation situation;
    /** The round it refers to, if any. */
    private final Round round;
    /** Days since the last round, or since applying. */
    private final int daysSinceActivity;
}
