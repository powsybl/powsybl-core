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
        private String key;
        private String name;
        private boolean enabled;
        private Double lowLimit;
        private Double highLimit;
        private boolean openAction;

        protected AbstractTrippingImpl(String key, String name, boolean enabled,
                                       Double lowLimit, Double highLimit, boolean openAction) {
            this.key = Objects.requireNonNull(key);
            this.name = name;
            this.enabled = enabled;
            this.lowLimit = lowLimit;
            this.highLimit = highLimit;
            this.openAction = openAction;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public Tripping setKey(String key) {
            this.key = Objects.requireNonNull(key);
            return this;
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
        public boolean isEnabled() {
            return this.enabled;
        }

        @Override
        public Tripping setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        @Override
        public Optional<Double> getLowLimit() {
            return Optional.ofNullable(this.lowLimit);
        }

        @Override
        public Tripping setLowLimit(Double lowLimit) {
            this.lowLimit = lowLimit;
            return this;
        }

        @Override
        public Optional<Double> getHighLimit() {
            return Optional.ofNullable(this.highLimit);
        }

        @Override
        public Tripping setHighLimit(Double highLimit) {
            this.highLimit = highLimit;
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
    }

    static class SwitchTrippingImpl extends AbstractTrippingImpl implements SwitchTripping {
        private Switch switchToOperate;

        public SwitchTrippingImpl(String key, String name, boolean enabled,
                                  Double lowLimit, Double highLimit, boolean openAction,
                                  Switch switchToOperate) {
            super(key, name, enabled, lowLimit, highLimit, openAction);
            this.switchToOperate = switchToOperate;
        }

        @Override
        public Switch getSwitchToOperate() {
            return this.switchToOperate;
        }

        @Override
        public SwitchTripping setSwitchToOperate(Switch switchToOperate) {
            this.switchToOperate = switchToOperate;
            return this;
        }

        @Override
        public String getMessageHeader() {
            return "Switch tripping"; //TODO define a useful message header
        }
    }

    static class BranchTrippingImpl extends AbstractTrippingImpl implements OverloadManagementSystem.BranchTripping {
        private Branch branchToOperate;
        private ThreeSides side;

        protected BranchTrippingImpl(String key, String name, boolean enabled,
                                     Double lowLimit, Double highLimit, boolean openAction,
                                     Branch branchToOperate, ThreeSides side) {
            super(key, name, enabled, lowLimit, highLimit, openAction);
            this.branchToOperate = branchToOperate;
            this.side = side;
        }

        @Override
        public Branch getBranchToOperate() {
            return this.branchToOperate;
        }

        @Override
        public BranchTripping setBranchToOperate(Branch<?> branch) {
            this.branchToOperate = branch;
            return this;
        }

        @Override
        public ThreeSides getBranchToOperateSide() {
            return this.side;
        }

        @Override
        public BranchTripping setBranchToOperateSide(ThreeSides side) {
            this.side = side;
            return this;
        }

        @Override
        public String getMessageHeader() {
            return "Branch tripping"; //TODO define a useful message header
        }
    }

    static class ThreeWindingsTransformerTrippingImpl extends AbstractTrippingImpl
            implements OverloadManagementSystem.ThreeWindingsTransformerTripping {

        private ThreeWindingsTransformer threeWindingsTransformer;
        private ThreeSides side;

        protected ThreeWindingsTransformerTrippingImpl(String key, String name, boolean enabled,
                                                       Double lowLimit, Double highLimit, boolean openAction,
                                                       ThreeWindingsTransformer threeWindingsTransformer, ThreeSides side) {
            super(key, name, enabled, lowLimit, highLimit, openAction);
            this.threeWindingsTransformer = threeWindingsTransformer;
            this.side = side;
        }

        @Override
        public ThreeWindingsTransformer getThreeWindingsTransformerToOperate() {
            return this.threeWindingsTransformer;
        }

        @Override
        public ThreeWindingsTransformerTripping setThreeWindingsTransformerToOperate(ThreeWindingsTransformer threeWindingsTransformer) {
            this.threeWindingsTransformer = threeWindingsTransformer;
            return this;
        }

        @Override
        public ThreeSides getSideToOperate() {
            return this.side;
        }

        @Override
        public ThreeWindingsTransformerTripping setSideToOperateSide(ThreeSides side) {
            this.side = side;
            return this;
        }

        @Override
        public String getMessageHeader() {
            return "Three windings transformer tripping"; //TODO define a useful message header
        }
    }

    private final SubstationImpl substation;
    private final String monitoredElementId;
    private final ThreeSides monitoredSide;
    private final List<Tripping> trippings;

    OverloadManagementSystemImpl(String id, String name, SubstationImpl substation,
                                 String monitoredElementId, ThreeSides monitoredSide,
                                 boolean enabled) {
        super(id, name, enabled);
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
    public NetworkImpl getNetwork() {
        return substation.getNetwork();
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
