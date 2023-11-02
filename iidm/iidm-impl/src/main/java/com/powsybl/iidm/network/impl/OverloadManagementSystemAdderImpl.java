/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class OverloadManagementSystemAdderImpl extends AbstractIdentifiableAdder<OverloadManagementSystemAdderImpl> implements OverloadManagementSystemAdder {

    abstract class AbstractTrippingAdderImpl implements Validable, OverloadManagementSystemAdder.TrippingAdder {
        protected String key = null;
        protected String name = null;
        protected boolean enabled = true;
        protected Double lowLimit = null;
        protected Double highLimit = null;
        protected boolean openAction = true;

        @Override
        public TrippingAdder setKey(String key) {
            this.key = key;
            return this;
        }

        @Override
        public TrippingAdder setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public TrippingAdder setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        @Override
        public TrippingAdder setLowLimit(Double lowLimit) {
            this.lowLimit = lowLimit;
            return this;
        }

        @Override
        public TrippingAdder setHighLimit(Double highLimit) {
            this.highLimit = highLimit;
            return this;
        }

        @Override
        public TrippingAdder setOpenAction(boolean open) {
            this.openAction = open;
            return this;
        }

        @Override
        public OverloadManagementSystemAdder add() {
            trippingAdders.add(this);
            return OverloadManagementSystemAdderImpl.this;
        }

        protected static String getNotFoundMessage(String type, String id) {
            return type + " '" + id + "' not found";
        }

        protected <I> I checkAndGetElement(String elementId, BiFunction<Network, String, I> getter, String attributeName, String type) {
            if (elementId == null) {
                throw new ValidationException(this, attributeName + " is not set");
            }
            I element = getter.apply(getNetwork(), elementId);
            if (element == null) {
                throw new ValidationException(this, getNotFoundMessage(type, elementId));
            }
            return element;
        }
    }

    class SwitchTrippingAdderImpl extends AbstractTrippingAdderImpl
            implements OverloadManagementSystemAdder.SwitchTrippingAdder {
        private String switchId;

        @Override
        public OverloadManagementSystemAdder.SwitchTrippingAdder setSwitchToOperateId(String switchId) {
            this.switchId = switchId;
            return this;
        }

        protected Switch checkAndGetSwitch() {
            return checkAndGetElement(switchId, Network::getSwitch, "switchId", "switch");
        }

        @Override
        public String getMessageHeader() {
            return "Switch tripping"; //TODO define a useful message header
        }
    }

    class BranchTrippingAdderImpl extends AbstractTrippingAdderImpl
            implements OverloadManagementSystemAdder.BranchTrippingAdder {
        private String branchId;
        private ThreeSides side; //TODO replace by a TwoSides

        @Override
        public OverloadManagementSystemAdder.BranchTrippingAdder setBranchToOperateId(String branchId) {
            this.branchId = branchId;
            return this;
        }

        @Override
        public OverloadManagementSystemAdder.BranchTrippingAdder setBranchToOperateSide(ThreeSides side) {
            this.side = side;
            return this;
        }

        protected Branch<?> checkAndGetBranch() {
            return checkAndGetElement(branchId, Network::getBranch, "branchId", "branch");
        }

        @Override
        public String getMessageHeader() {
            return "Branch tripping"; //TODO define a useful message header
        }
    }

    class ThreeWindingsTransformerTrippingAdderImpl extends AbstractTrippingAdderImpl
            implements OverloadManagementSystemAdder.ThreeWindingsTransformerTrippingAdder {
        private String threeWindingsTransformerId;
        private ThreeSides side;

        @Override
        public OverloadManagementSystemAdder.ThreeWindingsTransformerTrippingAdder setThreeWindingsTransformerToOperate(
                String threeWindingsTransformerId) {
            this.threeWindingsTransformerId = threeWindingsTransformerId;
            return this;
        }

        @Override
        public OverloadManagementSystemAdder.ThreeWindingsTransformerTrippingAdder setSideToOperateSide(ThreeSides side) {
            this.side = side;
            return this;
        }

        protected ThreeWindingsTransformer checkAndGetThreeWindingsTransformer() {
            return checkAndGetElement(threeWindingsTransformerId, Network::getThreeWindingsTransformer,
                    "threeWindingsTransformerId", "three windings transformer");
        }

        @Override
        public String getMessageHeader() {
            return "Three windings transformer tripping"; //TODO define a useful message header
        }
    }

    private final SubstationImpl substation;
    private boolean enabled = true;

    private String monitoredElementId;
    private ThreeSides monitoredElementSide;
    private final List<AbstractTrippingAdderImpl> trippingAdders = new ArrayList<>();

    OverloadManagementSystemAdderImpl(SubstationImpl substation) {
        this.substation = Objects.requireNonNull(substation);
    }

    @Override
    protected NetworkImpl getNetwork() {
        return substation.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "Overload management system";
    }

    @Override
    public OverloadManagementSystemAdder setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Override
    public OverloadManagementSystemAdder setMonitoredElementId(String monitoredElementId) {
        this.monitoredElementId = monitoredElementId;
        return this;
    }

    @Override
    public OverloadManagementSystemAdder setMonitoredElementSide(ThreeSides monitoredElementSide) {
        this.monitoredElementSide = monitoredElementSide;
        return this;
    }

    @Override
    public SwitchTrippingAdder newSwitchTripping() {
        return new SwitchTrippingAdderImpl();
    }

    @Override
    public BranchTrippingAdder newBranchTripping() {
        return new BranchTrippingAdderImpl();
    }

    @Override
    public OverloadManagementSystemAdder.ThreeWindingsTransformerTrippingAdder newThreeWindingsTransformerTripping() {
        return new ThreeWindingsTransformerTrippingAdderImpl();
    }

    @Override
    public OverloadManagementSystem add() {
        String id = checkAndGetUniqueId();

        OverloadManagementSystemImpl overloadManagementSystem = new OverloadManagementSystemImpl(id, getName(), substation,
                monitoredElementId, monitoredElementSide, enabled);

        // Add the trippings
        for (TrippingAdder adder : trippingAdders) {
            // TODO Check key uniqueness
            overloadManagementSystem.addTripping(createTripping(adder));
        }

        getNetwork().getIndex().checkAndAdd(overloadManagementSystem);
        substation.addOverloadManagementSystem(overloadManagementSystem);
        getNetwork().getListeners().notifyCreation(overloadManagementSystem);
        return overloadManagementSystem;
    }

    private OverloadManagementSystem.Tripping createTripping(TrippingAdder adder) {
        return switch (adder.getType()) {
            case SWITCH_TRIPPING -> createTripping((SwitchTrippingAdderImpl) adder);
            case BRANCH_TRIPPING -> createTripping((BranchTrippingAdderImpl) adder);
            case THREE_WINDINGS_TRANSFORMER_TRIPPING -> createTripping((ThreeWindingsTransformerTrippingAdderImpl) adder);
        };
    }

    private OverloadManagementSystem.Tripping createTripping(SwitchTrippingAdderImpl adder) {
        return new OverloadManagementSystemImpl.SwitchTrippingImpl(
                adder.key, adder.name, adder.enabled, adder.lowLimit, adder.highLimit, adder.openAction,
                adder.checkAndGetSwitch());
    }

    private OverloadManagementSystem.Tripping createTripping(BranchTrippingAdderImpl adder) {
        return new OverloadManagementSystemImpl.BranchTrippingImpl(
                adder.key, adder.name, adder.enabled, adder.lowLimit, adder.highLimit, adder.openAction,
                adder.checkAndGetBranch(), adder.side);
    }

    private OverloadManagementSystem.Tripping createTripping(ThreeWindingsTransformerTrippingAdderImpl adder) {
        ThreeWindingsTransformer threeWindingsTransformerToOperate;
        return new OverloadManagementSystemImpl.ThreeWindingsTransformerTrippingImpl(
                adder.key, adder.name, adder.enabled, adder.lowLimit, adder.highLimit, adder.openAction,
                adder.checkAndGetThreeWindingsTransformer(), adder.side);
    }

}
