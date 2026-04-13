package com.powsybl.mixed.security.analysis.criteria;

import com.powsybl.mixed.security.analysis.parameters.MixedModeParametersExtension;
import com.powsybl.security.PostContingencyComputationStatus;
import com.powsybl.security.results.PostContingencyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
/**
 * Evaluates whether a contingency result should trigger a switch to dynamic analysis.
 *
 * @author Riad Benradi {@literal <riad.benradi at rte-france.com>}
 */

public class AnalysisSwitchCriteria {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisSwitchCriteria.class);
    private final MixedModeParametersExtension extension;

    public AnalysisSwitchCriteria(MixedModeParametersExtension extension) {
        this.extension = Objects.requireNonNull(extension);
    }

    public SwitchDecision evaluate(PostContingencyResult result) {
        Objects.requireNonNull(result);
        if (extension.getSwitchCriteria() == null || extension.getSwitchCriteria().isEmpty()) {
            LOGGER.debug("No switch criteria defined, keeping current result");
            return new SwitchDecision(false, "No criteria defined");
        }
        for (String criterion : extension.getSwitchCriteria()) {
            if (evaluateCriterion(result, criterion)) {
                return new SwitchDecision(true, "Criterion '" + criterion + "' met");
            }
        }
        return new SwitchDecision(false, "No criteria met");
    }

    private boolean evaluateCriterion(PostContingencyResult result, String criterion) {
        return switch (criterion.toUpperCase()) {
            case "FAILED" -> evaluateNonConvergence(result);
            case "LIMIT_VIOLATIONS" -> evaluateLimitViolations(result);
            case "SPS_TRIGGERED" -> evaluateSpsTriggered(result);
            default -> {
                LOGGER.warn("Unknown criterion: {}", criterion);
                yield false;
            }
        };
    }

    private boolean evaluateNonConvergence(PostContingencyResult result) {
        boolean converged = result.getStatus() == PostContingencyComputationStatus.CONVERGED;
        if (!converged) {
            LOGGER.debug("Non-convergence detected for contingency {}", result.getContingency().getId());
        }
        return !converged;
    }

    private boolean evaluateLimitViolations(PostContingencyResult result) {
        boolean hasViolations = !result.getLimitViolationsResult().getLimitViolations().isEmpty();
        if (hasViolations) {
            LOGGER.debug("Limit violations detected for contingency {}", result.getContingency().getId());
        }
        return hasViolations;
    }

    private boolean evaluateSpsTriggered(PostContingencyResult result) {
        return true;
    }
}
