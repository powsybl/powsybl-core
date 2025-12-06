package com.powsybl.loadflow.validation.data;

import java.util.Objects;

public record ValidatedFlowData(String branchId, double p1, double p1Calc, double q1, double q1Calc, double p2,
                                double p2Calc, double q2, double q2Calc, double r, double x, double g1, double g2,
                                double b1, double b2, double rho1, double rho2, double alpha1, double alpha2, double u1,
                                double u2, double theta1, double theta2, double z, double y, double ksi,
                                int phaseAngleClock, boolean connected1, boolean connected2, boolean mainComponent1,
                                boolean mainComponent2, boolean validated) {
    public ValidatedFlowData {
        Objects.requireNonNull(branchId);
    }
}
