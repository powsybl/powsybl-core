/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
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
            setName(name);
            setCurrentLimit(currentLimit);
            setOpenAction(openAction);
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
            if (Double.isNaN(currentLimit)) {
                throw new ValidationException(this, "Current limit is mandatory.");
            } else if (currentLimit < 0) {
                throw new ValidationException(this, "Current limit must be positive.");
            }
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
        private String switchToOperateId;

        public SwitchTrippingImpl(String overloadManagementSystemId, String key, String name,
                                  double currentLimit, boolean openAction,
                                  String switchToOperateId) {
            super(overloadManagementSystemId, key, name, currentLimit, openAction);
            setSwitchToOperateId(switchToOperateId);
        }

        @Override
        public String getSwitchToOperateId() {
            return this.switchToOperateId;
        }

        @Override
        public SwitchTripping setSwitchToOperateId(String switchToOperateId) {
            this.switchToOperateId = Objects.requireNonNull(switchToOperateId);
            return this;
        }
    }

    static class BranchTrippingImpl extends AbstractTrippingImpl implements OverloadManagementSystem.BranchTripping {
        private String branchToOperateId;
        private TwoSides side;

        protected BranchTrippingImpl(String overloadManagementSystemId, String key, String name,
                                     double currentLimit, boolean openAction,
                                     String branchToOperateId, TwoSides side) {
            super(overloadManagementSystemId, key, name, currentLimit, openAction);
            setBranchToOperateId(branchToOperateId);
            setSideToOperate(side);
        }

        @Override
        public String getBranchToOperateId() {
            return this.branchToOperateId;
        }

        @Override
        public BranchTripping setBranchToOperateId(String branchId) {
            this.branchToOperateId = Objects.requireNonNull(branchId);
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

        private String threeWindingsTransformerId;
        private ThreeSides side;

        protected ThreeWindingsTransformerTrippingImpl(String overloadManagementSystemId, String key, String name,
                                                       double currentLimit, boolean openAction,
                                                       String threeWindingsTransformerId, ThreeSides side) {
            super(overloadManagementSystemId, key, name, currentLimit, openAction);
            setThreeWindingsTransformerToOperateId(threeWindingsTransformerId);
            setSideToOperate(side);
        }

        @Override
        public String getThreeWindingsTransformerToOperateId() {
            return this.threeWindingsTransformerId;
        }

        @Override
        public ThreeWindingsTransformerTripping setThreeWindingsTransformerToOperateId(String threeWindingsTransformerId) {
            this.threeWindingsTransformerId = Objects.requireNonNull(threeWindingsTransformerId);
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

    protected boolean removed = false;
    private final SubstationImpl substation;
    private final String monitoredElementId;
    private final ThreeSides monitoredSide;
    private final List<Tripping> trippings;

    OverloadManagementSystemImpl(String id, String name, SubstationImpl substation,
                                 String monitoredElementId, ThreeSides monitoredSide,
                                 boolean enabled) {
        super(Objects.requireNonNull(substation).getNetworkRef(), id, name, enabled);
        this.substation = Objects.requireNonNull(substation);
        this.monitoredElementId = checkMonitoredElementId(monitoredElementId);
        this.monitoredSide = Objects.requireNonNull(monitoredSide);
        this.trippings = new ArrayList<>();
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();
        network.getListeners().notifyBeforeRemoval(this);
        network.getIndex().remove(this);
        substation.remove(this);
        network.getListeners().notifyAfterRemoval(id);
        removed = true;
    }

    private String checkMonitoredElementId(String monitoredElementId) {
        if (monitoredElementId == null) {
            throw new ValidationException(this, "monitoredElementId is not set");
        }
        return monitoredElementId;
    }

    @Override
    public Substation getSubstation() {
        throwExceptionIfRemoved("substation");
        return this.substation;
    }

    @Override
    public NetworkImpl getNetwork() {
        throwExceptionIfRemoved("network");
        return this.substation.getNetwork();
    }

    @Override
    public Network getParentNetwork() {
        throwExceptionIfRemoved("parent network");
        return this.substation.getParentNetwork();
    }

    protected void throwExceptionIfRemoved(String accessedElement) {
        if (removed) {
            throw new PowsyblException(String.format("Cannot access %s of removed equipment %s", accessedElement, id));
        }
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
