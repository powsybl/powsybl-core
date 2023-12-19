/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.*;

/**
 * Overload management system implementation.
 *
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class OverloadManagementSystemImpl extends AbstractAutomationSystem<OverloadManagementSystem> implements OverloadManagementSystem {

    abstract static class AbstractTrippingImpl implements Tripping, Validable {
        private final String overloadManagementSystemId;
        private final String key;
        private String name;
        private double currentLimit;
        private boolean openAction;

        protected AbstractTrippingImpl(String overloadManagementSystemId, String key, String name,
                                       double currentLimit, boolean openAction) {
            this.overloadManagementSystemId = overloadManagementSystemId;
            this.key = Objects.requireNonNull(key);
            this.name = name;
            if (Double.isNaN(currentLimit)) {
                throw new ValidationException(this, "Current limit is mandatory.");
            } else if (currentLimit < 0) {
                throw new ValidationException(this, "Current limit must be positive.");
            }
            this.currentLimit = currentLimit;
            this.openAction = openAction;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getNameOrKey() {
            return name != null ? name : key;
        }

        @Override
        public Tripping setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public double getCurrentLimit() {
            return this.currentLimit;
        }

        @Override
        public Tripping setCurrentLimit(double currentLimit) {
            this.currentLimit = currentLimit;
            return this;
        }

        @Override
        public boolean isOpenAction() {
            return this.openAction;
        }

        @Override
        public Tripping setOpenAction(boolean open) {
            this.openAction = open;
            return this;
        }

        protected String getTrippingAttribute() {
            return String.format("tripping '%s'", key);
        }

        @Override
        public String getMessageHeader() {
            return String.format("Overload management system '%s' - %s:", overloadManagementSystemId, getTrippingAttribute());
        }
    }

    static class SwitchTrippingImpl extends AbstractTrippingImpl implements SwitchTripping {
        private Switch switchToOperate;

        public SwitchTrippingImpl(String overloadManagementSystemId, String key, String name,
                                  double currentLimit, boolean openAction,
                                  Switch switchToOperate) {
            super(overloadManagementSystemId, key, name, currentLimit, openAction);
            this.switchToOperate = Objects.requireNonNull(switchToOperate);
        }

        @Override
        public Switch getSwitchToOperate() {
            return this.switchToOperate;
        }

        @Override
        public SwitchTripping setSwitchToOperate(Switch switchToOperate) {
            this.switchToOperate = Objects.requireNonNull(switchToOperate);
            return this;
        }
    }

    static class BranchTrippingImpl extends AbstractTrippingImpl implements OverloadManagementSystem.BranchTripping {
        private Branch<?> branchToOperate;
        private TwoSides side;

        protected BranchTrippingImpl(String overloadManagementSystemId, String key, String name,
                                     double currentLimit, boolean openAction,
                                     Branch<?> branchToOperate, TwoSides side) {
            super(overloadManagementSystemId, key, name, currentLimit, openAction);
            this.branchToOperate = Objects.requireNonNull(branchToOperate);
            this.side = Objects.requireNonNull(side);
        }

        @Override
        public Branch getBranchToOperate() {
            return this.branchToOperate;
        }

        @Override
        public BranchTripping setBranchToOperate(Branch<?> branch) {
            this.branchToOperate = Objects.requireNonNull(branch);
            return this;
        }

        @Override
        public TwoSides getSideToOperate() {
            return this.side;
        }

        @Override
        public BranchTripping setSideToOperate(TwoSides side) {
            this.side = Objects.requireNonNull(side);
            return this;
        }
    }

    static class ThreeWindingsTransformerTrippingImpl extends AbstractTrippingImpl
            implements OverloadManagementSystem.ThreeWindingsTransformerTripping {

        private ThreeWindingsTransformer threeWindingsTransformer;
        private ThreeSides side;

        protected ThreeWindingsTransformerTrippingImpl(String overloadManagementSystemId, String key, String name,
                                                       double currentLimit, boolean openAction,
                                                       ThreeWindingsTransformer threeWindingsTransformer, ThreeSides side) {
            super(overloadManagementSystemId, key, name, currentLimit, openAction);
            this.threeWindingsTransformer = Objects.requireNonNull(threeWindingsTransformer);
            this.side = Objects.requireNonNull(side);
        }

        @Override
        public ThreeWindingsTransformer getThreeWindingsTransformerToOperate() {
            return this.threeWindingsTransformer;
        }

        @Override
        public ThreeWindingsTransformerTripping setThreeWindingsTransformerToOperate(ThreeWindingsTransformer threeWindingsTransformer) {
            this.threeWindingsTransformer = Objects.requireNonNull(threeWindingsTransformer);
            return this;
        }

        @Override
        public ThreeSides getSideToOperate() {
            return this.side;
        }

        @Override
        public ThreeWindingsTransformerTripping setSideToOperate(ThreeSides side) {
            this.side = Objects.requireNonNull(side);
            return this;
        }
    }

    private final SubstationImpl substation;
    private final String monitoredElementId;
    private final ThreeSides monitoredSide;
    private final List<Tripping> trippings;

    OverloadManagementSystemImpl(String id, String name, SubstationImpl substation,
                                 String monitoredElementId, ThreeSides monitoredSide,
                                 boolean enabled) {
        super(Objects.requireNonNull(substation).getNetworkRef(), id, name, enabled);
        this.substation = Objects.requireNonNull(substation);
        this.monitoredElementId = Objects.requireNonNull(monitoredElementId);
        this.monitoredSide = Objects.requireNonNull(monitoredSide);
        this.trippings = new ArrayList<>();
    }

    @Override
    public Substation getSubstation() {
        return this.substation;
    }

    @Override
    protected String getTypeDescription() {
        return "Overload management system";
    }

    @Override
    public String getMonitoredElementId() {
        return monitoredElementId;
    }

    @Override
    public ThreeSides getMonitoredSide() {
        return monitoredSide;
    }

    @Override
    public void addTripping(Tripping tripping) {
        this.trippings.add(tripping);
    }

    @Override
    public List<Tripping> getTrippings() {
        return Collections.unmodifiableList(trippings);
    }
}
