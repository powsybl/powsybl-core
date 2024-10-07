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
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class OverloadManagementSystemAdderImpl extends AbstractIdentifiableAdder<OverloadManagementSystemAdderImpl>
        implements OverloadManagementSystemAdder {

    private boolean validateAfterCreation = false;

    abstract class AbstractTrippingAdderImpl<I extends TrippingAdder<I>> implements Validable, TrippingAdder<I> {
        protected String key = null;
        protected String name = null;
        protected double currentLimit = Double.NaN;
        protected boolean openAction = true;

        public I setKey(String key) {
            this.key = key;
            return (I) this;
        }

        public I setName(String name) {
            this.name = name;
            return (I) this;
        }

        public I setCurrentLimit(double currentLimit) {
            this.currentLimit = currentLimit;
            return (I) this;
        }

        public I setOpenAction(boolean open) {
            this.openAction = open;
            return (I) this;
        }

        public OverloadManagementSystemAdder add() {
            trippingAdders.add(this);
            return OverloadManagementSystemAdderImpl.this;
        }

        protected String getTrippingAttribute() {
            return String.format("tripping '%s'", key);
        }

        @Override
        public String getMessageHeader() {
            return String.format("Overload management system in substation '%s':  - %s:", substation.getId(), getTrippingAttribute());
        }

        protected static String getNotFoundMessage(String type, String id) {
            return type + " '" + id + "' not found";
        }

        protected <E> String checkElementId(String elementId, BiFunction<Network, String, E> getter, String attributeName, String type) {
            if (elementId == null) {
                throw new ValidationException(this, attributeName + " is not set");
            }
            E element = getter.apply(getNetwork(), elementId);
            if (element == null) {
                throw new ValidationException(this, getNotFoundMessage(type, elementId));
            }
            return elementId;
        }
    }

    class SwitchTrippingAdderImpl extends AbstractTrippingAdderImpl<SwitchTrippingAdder>
            implements OverloadManagementSystemAdder.SwitchTrippingAdder {
        private String switchId;

        @Override
        public OverloadManagementSystemAdder.SwitchTrippingAdder setSwitchToOperateId(String switchId) {
            this.switchId = switchId;
            return this;
        }

        protected String checkSwitchId() {
            return checkElementId(switchId, Network::getSwitch, "switchId", "switch");
        }

        @Override
        public Collection<Consumer<OverloadManagementSystem>> getValidationChecks() {
            return Collections.emptyList();
        }
    }

    class BranchTrippingAdderImpl extends AbstractTrippingAdderImpl<BranchTrippingAdder>
            implements OverloadManagementSystemAdder.BranchTrippingAdder {
        private String branchId;
        private TwoSides side;

        @Override
        public OverloadManagementSystemAdder.BranchTrippingAdder setBranchToOperateId(String branchId) {
            this.branchId = branchId;
            return this;
        }

        @Override
        public OverloadManagementSystemAdder.BranchTrippingAdder setSideToOperate(TwoSides side) {
            this.side = side;
            return this;
        }

        protected String checkBranchId() {
            return checkElementId(branchId, Network::getBranch, "branchId", "branch");
        }

        @Override
        public Collection<Consumer<OverloadManagementSystem>> getValidationChecks() {
            return Collections.emptyList();
        }
    }

    class ThreeWindingsTransformerTrippingAdderImpl extends AbstractTrippingAdderImpl<ThreeWindingsTransformerTrippingAdder>
            implements OverloadManagementSystemAdder.ThreeWindingsTransformerTrippingAdder {
        private String threeWindingsTransformerId;
        private ThreeSides side;

        @Override
        public OverloadManagementSystemAdder.ThreeWindingsTransformerTrippingAdder setThreeWindingsTransformerToOperateId(
                String threeWindingsTransformerId) {
            this.threeWindingsTransformerId = threeWindingsTransformerId;
            return this;
        }

        @Override
        public OverloadManagementSystemAdder.ThreeWindingsTransformerTrippingAdder setSideToOperate(ThreeSides side) {
            this.side = side;
            return this;
        }

        protected String checkThreeWindingsTransformerId() {
            return checkElementId(threeWindingsTransformerId, Network::getThreeWindingsTransformer,
                    "threeWindingsTransformerId", "three windings transformer");
        }

        @Override
        public Collection<Consumer<OverloadManagementSystem>> getValidationChecks() {
            return Collections.emptyList();
        }
    }

    private final SubstationImpl substation;
    private boolean enabled = true;

    private String monitoredElementId;
    private ThreeSides monitoredElementSide;
    private final List<AbstractTrippingAdderImpl<?>> trippingAdders = new ArrayList<>();

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
    public OverloadManagementSystemAdder validateAfterCreation() {
        validateAfterCreation = true;
        return this;
    }

    @Override
    public Collection<Consumer<OverloadManagementSystem>> getValidationChecks() {
        return List.of(this::validateMonitoredElementId);
    }

    public void validateMonitoredElementId(OverloadManagementSystem oms) {
        Identifiable<?> element = getNetwork().getIdentifiable(monitoredElementId);
        if (element == null) {
            throw new ValidationException(this, " '" + monitoredElementId + "' not found");
        }
    }

    @Override
    public OverloadManagementSystem add() {
        String id = checkAndGetUniqueId();

        OverloadManagementSystemImpl overloadManagementSystem = new OverloadManagementSystemImpl(id, getName(), substation,
                monitoredElementId, monitoredElementSide, enabled);

        if (!validateAfterCreation) {
            validateMonitoredElementId(overloadManagementSystem);
        }

        // Add the trippings
        Set<String> knownTrippingKeys = new HashSet<>();
        for (AbstractTrippingAdderImpl<?> adder : trippingAdders) {
            overloadManagementSystem.addTripping(createTripping(adder, id, knownTrippingKeys));
        }

        getNetwork().getIndex().checkAndAdd(overloadManagementSystem);
        substation.addOverloadManagementSystem(overloadManagementSystem);
        getNetwork().getListeners().notifyCreation(overloadManagementSystem);
        return overloadManagementSystem;
    }

    private OverloadManagementSystem.Tripping createTripping(AbstractTrippingAdderImpl<?> adder, String overloadManagementSystemId,
                                                             Set<String> knownTrippingKeys) {
        String key = adder.key;
        if (!knownTrippingKeys.add(key)) {
            throw new ValidationException(adder, "key \"" + key +
                    "\" is already used for another tripping in the overload management system.");
        }
        return switch (adder.getType()) {
            case SWITCH_TRIPPING -> createTripping((SwitchTrippingAdderImpl) adder, overloadManagementSystemId);
            case BRANCH_TRIPPING -> createTripping((BranchTrippingAdderImpl) adder, overloadManagementSystemId);
            case THREE_WINDINGS_TRANSFORMER_TRIPPING -> createTripping((ThreeWindingsTransformerTrippingAdderImpl) adder,
                    overloadManagementSystemId);
        };
    }

    private OverloadManagementSystem.Tripping createTripping(SwitchTrippingAdderImpl adder, String overloadManagementSystemId) {
        return new OverloadManagementSystemImpl.SwitchTrippingImpl(
                overloadManagementSystemId, adder.key, adder.name,
                adder.currentLimit, adder.openAction,
                adder.checkSwitchId());
    }

    private OverloadManagementSystem.Tripping createTripping(BranchTrippingAdderImpl adder, String overloadManagementSystemId) {
        return new OverloadManagementSystemImpl.BranchTrippingImpl(
                overloadManagementSystemId,
                adder.key, adder.name, adder.currentLimit, adder.openAction,
                adder.checkBranchId(), adder.side);
    }

    private OverloadManagementSystem.Tripping createTripping(ThreeWindingsTransformerTrippingAdderImpl adder,
                                                             String overloadManagementSystemId) {
        return new OverloadManagementSystemImpl.ThreeWindingsTransformerTrippingImpl(
                overloadManagementSystemId,
                adder.key, adder.name, adder.currentLimit, adder.openAction,
                adder.checkThreeWindingsTransformerId(), adder.side);
    }
}
