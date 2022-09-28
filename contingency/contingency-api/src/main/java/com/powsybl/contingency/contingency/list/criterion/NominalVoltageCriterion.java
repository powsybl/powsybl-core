/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.contingency.list.criterion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.iidm.network.*;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class NominalVoltageCriterion implements Criterion {

    private final VoltageInterval voltageInterval;
    private final VoltageInterval voltageInterval1;
    private final VoltageInterval voltageInterval2;
    private final VoltageInterval voltageInterval3;

    public NominalVoltageCriterion(VoltageInterval voltageInterval, VoltageInterval voltageInterval1, VoltageInterval voltageInterval2, VoltageInterval voltageInterval3) {
        this.voltageInterval = voltageInterval == null ? new VoltageInterval(null, null, null, null) : voltageInterval;
        this.voltageInterval1 = voltageInterval1 == null ? new VoltageInterval(null, null, null, null) : voltageInterval1;
        this.voltageInterval2 = voltageInterval2 == null ? new VoltageInterval(null, null, null, null) : voltageInterval2;
        this.voltageInterval3 = voltageInterval3 == null ? new VoltageInterval(null, null, null, null) : voltageInterval3;
    }

    @Override
    public CriterionType getType() {
        return CriterionType.NOMINAL_VOLTAGE;
    }

    @Override
    public boolean filter(Identifiable<?> identifiable, IdentifiableType type) {
        switch (type) {
            case LINE:
                return filterInjection(((Line) identifiable).getTerminal1());
            case TWO_WINDINGS_TRANSFORMER:
                return filterTwoWindingsTransformer(((TwoWindingsTransformer) identifiable).getTerminal1(), ((TwoWindingsTransformer) identifiable).getTerminal2());
            case THREE_WINDINGS_TRANSFORMER:
                return filterThreeWindingsTransformer(((ThreeWindingsTransformer) identifiable).getLeg1().getTerminal(),
                        ((ThreeWindingsTransformer) identifiable).getLeg2().getTerminal(),
                        ((ThreeWindingsTransformer) identifiable).getLeg3().getTerminal());
            case HVDC_LINE:
                return filterInjection(((HvdcLine) identifiable).getConverterStation1().getTerminal());
            case DANGLING_LINE:
            case GENERATOR:
            case SWITCH:
            case LOAD:
            case BATTERY:
            case SHUNT_COMPENSATOR:
            case STATIC_VAR_COMPENSATOR:
            case BUSBAR_SECTION:
                return filterInjection(((Injection) identifiable).getTerminal());
            default:
                return false;
        }
    }

    private boolean filterInjection(Terminal terminal) {
        VoltageLevel voltageLevel = terminal.getVoltageLevel();
        if (voltageLevel == null) {
            return false;
        }
        double injectionNomialVoltage = voltageLevel.getNominalV();
        return voltageInterval.isNull() || voltageInterval.checkIsBetweenBound(injectionNomialVoltage);
    }

    private boolean filterTwoWindingsTransformer(Terminal terminal1, Terminal terminal2) {
        VoltageLevel voltageLevel1 = terminal1.getVoltageLevel();
        VoltageLevel voltageLevel2 = terminal2.getVoltageLevel();
        double branchNominalVoltage1 = voltageLevel1.getNominalV();
        double branchNominalVoltage2 = voltageLevel2.getNominalV();
        if (voltageInterval1.isNull() && voltageInterval2.isNull()) {
            return true;
        } else if (voltageInterval1.isNull()) {
            return voltageInterval2.checkIsBetweenBound(branchNominalVoltage1) || voltageInterval2.checkIsBetweenBound(branchNominalVoltage2);
        } else if (voltageInterval2.isNull()) {
            return voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) || voltageInterval1.checkIsBetweenBound(branchNominalVoltage2);
        } else {
            return (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage1));
        }
    }

    private boolean filterThreeWindingsTransformer(Terminal terminal1, Terminal terminal2, Terminal terminal3) {
        VoltageLevel voltageLevel1 = terminal1.getVoltageLevel();
        VoltageLevel voltageLevel2 = terminal2.getVoltageLevel();
        VoltageLevel voltageLevel3 = terminal3.getVoltageLevel();
        double branchNominalVoltage1 = voltageLevel1.getNominalV();
        double branchNominalVoltage2 = voltageLevel2.getNominalV();
        double branchNominalVoltage3 = voltageLevel3.getNominalV();
        if (voltageInterval1.isNull() && voltageInterval2.isNull() && voltageInterval3.isNull()) {
            return true;
        } else if (voltageInterval1.isNull() && voltageInterval2.isNull()) {
            return voltageInterval3.checkIsBetweenBound(branchNominalVoltage1) || voltageInterval3.checkIsBetweenBound(branchNominalVoltage2) ||
                    voltageInterval3.checkIsBetweenBound(branchNominalVoltage3);
        } else if (voltageInterval1.isNull() && voltageInterval3.isNull()) {
            return voltageInterval2.checkIsBetweenBound(branchNominalVoltage1) || voltageInterval2.checkIsBetweenBound(branchNominalVoltage2) ||
                    voltageInterval2.checkIsBetweenBound(branchNominalVoltage3);
        } else if (voltageInterval2.isNull() && voltageInterval3.isNull()) {
            return voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) || voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) ||
                    voltageInterval1.checkIsBetweenBound(branchNominalVoltage3);
        } else if (voltageInterval1.isNull()) {
            return (voltageInterval2.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval2.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval2.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval2.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage1)) ||
                    (voltageInterval2.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval2.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage1));
        } else if (voltageInterval2.isNull()) {
            return (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage1)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval3.checkIsBetweenBound(branchNominalVoltage1));
        } else if (voltageInterval3.isNull()) {
            return (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage1)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage1)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage2));
        } else {
            return (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage2)
                    && voltageInterval3.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage1) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage3)
                            && voltageInterval3.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage1)
                            && voltageInterval3.checkIsBetweenBound(branchNominalVoltage3)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage2) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage3)
                            && voltageInterval3.checkIsBetweenBound(branchNominalVoltage1)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage1)
                            && voltageInterval3.checkIsBetweenBound(branchNominalVoltage2)) ||
                    (voltageInterval1.checkIsBetweenBound(branchNominalVoltage3) && voltageInterval2.checkIsBetweenBound(branchNominalVoltage2)
                            && voltageInterval3.checkIsBetweenBound(branchNominalVoltage1));
        }
    }

    public static class VoltageInterval {
        Double nominalVoltageLowBound;
        Double nominalVoltageHighBound;
        Boolean lowClosed;
        Boolean highClosed;

        public VoltageInterval() {
        }

        public VoltageInterval(Double nominalVoltageLowBound, Double nominalVoltageHighBound, Boolean lowClosed, Boolean highClosed) {
            this.nominalVoltageLowBound = nominalVoltageLowBound;
            this.nominalVoltageHighBound = nominalVoltageHighBound;
            this.lowClosed = lowClosed;
            this.highClosed = highClosed;
        }

        @JsonIgnore
        public boolean isNull() {
            return nominalVoltageLowBound == null || nominalVoltageHighBound == null || lowClosed == null || highClosed == null;
        }

        public boolean checkIsBetweenBound(double value) {
            if (lowClosed && highClosed) {
                return nominalVoltageLowBound <= value && value <= nominalVoltageHighBound;
            } else if (lowClosed) {
                return nominalVoltageLowBound <= value && value < nominalVoltageHighBound;
            } else if (highClosed) {
                return nominalVoltageLowBound < value && value <= nominalVoltageHighBound;
            } else {
                return nominalVoltageLowBound < value && value < nominalVoltageHighBound;
            }
        }

        public Double getNominalVoltageLowBound() {
            return nominalVoltageLowBound;
        }

        public Double getNominalVoltageHighBound() {
            return nominalVoltageHighBound;
        }

        public Boolean getLowClosed() {
            return lowClosed;
        }

        public Boolean getHighClosed() {
            return highClosed;
        }
    }

    public VoltageInterval getVoltageInterval() {
        return voltageInterval;
    }

    public VoltageInterval getVoltageInterval1() {
        return voltageInterval1;
    }

    public VoltageInterval getVoltageInterval2() {
        return voltageInterval2;
    }

    public VoltageInterval getVoltageInterval3() {
        return voltageInterval3;
    }
}
