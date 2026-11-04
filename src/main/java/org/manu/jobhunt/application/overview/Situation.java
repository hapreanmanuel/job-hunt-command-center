package org.manu.jobhunt.application.overview;

/** Where a job stands. Declaration order is urgency order, which is how Home sorts. */
public enum Situation {
    /** A round within the next 48 hours: time to prepare. */
    ROUND_SOON,
    /** An offer is waiting for an answer. */
    OFFER_OPEN,
    /** A round has passed and the debrief isn't written. */
    NEEDS_DEBRIEF,
    /** Two weeks since the last round, with no result recorded. */
    RESULT_OVERDUE,
    /** The last round was passed and nothing is planned after it. */
    PASSED_NO_NEXT,
    /** The last round was failed, but the job is still open: it should be closed. */
    FAILED_OPEN,
    /** Two weeks since applying, with no round at all. */
    NO_REPLY,
    /** On the wishlist: not applied yet. */
    NOT_APPLIED,
    /** A round is planned, more than 48 hours away. */
    ROUND_LATER,
    /** Nothing to do yet. */
    WAITING
}
