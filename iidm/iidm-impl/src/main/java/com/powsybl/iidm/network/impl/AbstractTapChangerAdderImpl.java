/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.NetworkReports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
abstract class AbstractTapChangerAdderImpl<
        A extends AbstractTapChangerAdderImpl<A, H, T, S>,
        H extends TapChangerParent,
        T extends TapChanger<T, ?, ?, ?>,
        S extends TapChangerStepImpl<S>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTapChangerAdderImpl.class);

    protected final H parent;
    private int lowTapPosition = 0;
    private Integer tapPosition;
    protected final List<S> steps;
    private double regulationValue = Double.NaN;
    private boolean regulating = false;
    private double targetDeadband = Double.NaN;
    private TerminalExt regulationTerminal;
    private boolean loadTapChangingCapabilities;
    private Integer solvedTapPosition;

    protected AbstractTapChangerAdderImpl(H parent, boolean loadTapChangingCapabilities) {
        this.parent = parent;
        this.steps = new ArrayList<>();
        this.loadTapChangingCapabilities = loadTapChangingCapabilities;
    }

    public A setLowTapPosition(int lowTapPosition) {
        this.lowTapPosition = lowTapPosition;
        return self();
    }

    public A setTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return self();
    }

    public A setSolvedTapPosition(Integer solvedTapPosition) {
        this.solvedTapPosition = solvedTapPosition;
        return self();
    }

    public A setRegulationValue(double regulationValue) {
        this.regulationValue = regulationValue;
        return self();
    }

    public A setRegulating(boolean regulating) {
        this.regulating = regulating;
        return self();
    }

    public A setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return self();
    }

    public A setRegulationTerminal(Terminal regulationTerminal) {
        this.regulationTerminal = (TerminalExt) regulationTerminal;
        return self();
    }

    public A setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        this.loadTapChangingCapabilities = loadTapChangingCapabilities;
        return self();
    }

    NetworkImpl getNetwork() {
        return parent.getNetwork();
    }

    public T add() {
        NetworkImpl network = getNetwork();
        if (tapPosition == null) {
            ValidationUtil.throwExceptionOrIgnore(parent, "tap position is not set", network.getMinValidationLevel());
            network.setValidationLevelIfGreaterThan(ValidationLevel.EQUIPMENT);
        }
        if (steps.isEmpty()) {
            throw new ValidationException(parent, getValidableType() + " should have at least one step");
        }
        int highTapPosition = lowTapPosition + steps.size() - 1;
        if (tapPosition != null && (tapPosition < lowTapPosition || tapPosition > highTapPosition)) {
            ValidationUtil.throwExceptionOrIgnore(parent, "incorrect tap position "
                + tapPosition + " [" + lowTapPosition + ", "
                + highTapPosition + "]", network.getMinValidationLevel());
            network.setValidationLevelIfGreaterThan(ValidationLevel.EQUIPMENT);
        }

        if (solvedTapPosition != null && (solvedTapPosition < lowTapPosition || solvedTapPosition > highTapPosition)) {
            throw new ValidationException(parent, "incorrect solved tap position "
                + solvedTapPosition + " [" + lowTapPosition + ", " + highTapPosition
                + "]");
        }

        network.setValidationLevelIfGreaterThan(checkTapChangerRegulation(parent, regulationValue, regulating, loadTapChangingCapabilities, regulationTerminal));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkTargetDeadband(parent, getValidableType(), regulating,
                targetDeadband, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));

        T tapChanger = createTapChanger(parent, lowTapPosition, steps, regulationTerminal, tapPosition, solvedTapPosition, regulating, loadTapChangingCapabilities, regulationValue, targetDeadband);

        Set<TapChanger<?, ?, ?, ?>> otherTapChangers = new HashSet<>(parent.getAllTapChangers());
        otherTapChangers.remove(tapChanger);
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, otherTapChangers, regulating,
                network.getMinValidationLevel(), network.getReportNodeContext().getReportNode()));

        if (parent.hasPhaseTapChanger() && parent.hasRatioTapChanger()) {
            LOGGER.warn("{} has both Ratio and Phase Tap Changer", parent);
            NetworkReports.transformerHasBothRatioAndPhaseTapChanger(network.getReportNodeContext().getReportNode(), parent.getTransformer().getId());
        }

        return tapChanger;
    }

    protected abstract T createTapChanger(H parent, int lowTapPosition, List<S> steps, TerminalExt regulationTerminal, Integer tapPosition, Integer solvedTapPosition, boolean regulating, boolean loadTapChangingCapabilities, double regulationValue, double targetDeadband);

    protected abstract A self();

    protected abstract ValidationLevel checkTapChangerRegulation(H parent, double regulationValue, boolean regulating, boolean loadTapChangingCapabilities, TerminalExt regulationTerminal);

    protected abstract String getValidableType();

}
