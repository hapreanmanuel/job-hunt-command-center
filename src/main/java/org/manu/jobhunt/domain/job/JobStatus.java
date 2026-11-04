package org.manu.jobhunt.domain.job;

/** The phases of a job. Each closed status can be reached from any phase. */
public enum JobStatus {
    WISHLIST,
    APPLIED,
    INTERVIEWING,
    OFFER,
    ACCEPTED,
    DECLINED,
    REJECTED,
    WITHDRAWN,
    GHOSTED;

    public boolean isClosed() {
        return switch (this) {
            case ACCEPTED, DECLINED, REJECTED, WITHDRAWN, GHOSTED -> true;
            default -> false;
        };
    }
}
