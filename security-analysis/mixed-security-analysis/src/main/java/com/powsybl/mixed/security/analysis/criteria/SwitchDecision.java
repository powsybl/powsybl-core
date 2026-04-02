package com.powsybl.mixed.security.analysis.criteria;

import java.util.Objects;

/**
 * Represents a decision on whether to switch to dynamic analysis for a contingency.
 *
 * @author Riad Benradi {@literal <riad.benradi at rte-france.com>}
 */
public class SwitchDecision {
    private final boolean shouldSwitch;
    private final String reason;

    public SwitchDecision(boolean shouldSwitch, String reason) {
        this.shouldSwitch = shouldSwitch;
        this.reason = Objects.requireNonNull(reason);
    }

    public boolean shouldSwitch() {
        return shouldSwitch;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwitchDecision that = (SwitchDecision) o;
        return shouldSwitch == that.shouldSwitch && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shouldSwitch, reason);
    }

    @Override
    public String toString() {
        return "SwitchDecision{" +
                "shouldSwitch=" + shouldSwitch +
                ", reason='" + reason + '\'' +
                '}';
    }
}

