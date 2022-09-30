/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.impl.ThreeWindingsTransformerImpl.LegImpl;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ThreeWindingsTransformerAdderImpl extends AbstractIdentifiableAdder<ThreeWindingsTransformerAdderImpl>
        implements ThreeWindingsTransformerAdder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreeWindingsTransformerAdderImpl.class);

    class LegAdderImpl implements Validable, LegAdder {

        protected String voltageLevelId;

        protected Integer node;

        protected String bus;

        protected String connectableBus;

        protected double r = Double.NaN;

        protected double x = Double.NaN;

        protected double g = Double.NaN;

        protected double b = Double.NaN;

        protected double ratedU = Double.NaN;

        protected double ratedS = Double.NaN;

        private final int legNumber;

        LegAdderImpl(int legNumber) {
            this.legNumber = legNumber;
        }

        LegAdderImpl(ThreeWindingsTransformer.Leg leg, int legNumber) {
            this(legNumber);
            r = leg.getR();
            x = leg.getX();
            g = leg.getG();
            b = leg.getB();
            ratedS = leg.getRatedS();
            ratedU = leg.getRatedU();
        }

        public LegAdder setVoltageLevel(String voltageLevelId) {
            this.voltageLevelId = voltageLevelId;
            return this;
        }

        public LegAdder setNode(int node) {
            this.node = node;
            return this;
        }

        public LegAdder setBus(String bus) {
            this.bus = bus;
            return this;
        }

        public LegAdder setConnectableBus(String connectableBus) {
            this.connectableBus = connectableBus;
            return this;
        }

        public LegAdder setR(double r) {
            this.r = r;
            return this;
        }

        public LegAdder setX(double x) {
            this.x = x;
            return this;
        }

        public LegAdder setG(double g) {
            this.g = g;
            return this;
        }

        public LegAdder setB(double b) {
            this.b = b;
            return this;
        }

        public LegAdder setRatedU(double ratedU) {
            this.ratedU = ratedU;
            return this;
        }

        @Override
        public LegAdder setRatedS(double ratedS) {
            this.ratedS = ratedS;
            return this;
        }

        protected void checkParams() {
            if (Double.isNaN(r)) {
                throw new ValidationException(this, "r is not set");
            }
            if (Double.isNaN(x)) {
                throw new ValidationException(this, "x is not set");
            }
            if (Double.isNaN(g)) {
                throw new ValidationException(this, "g is not set");
            }
            if (Double.isNaN(b)) {
                throw new ValidationException(this, "b is not set");
            }
            ValidationUtil.checkRatedU(this, ratedU, "");
            ValidationUtil.checkRatedS(this, ratedS);
        }

        protected TerminalExt checkAndGetTerminal() {
            return new TerminalBuilder(getNetwork().getRef(), this)
                    .setNode(node)
                    .setBus(bus)
                    .setConnectableBus(connectableBus)
                    .build();
        }

        protected VoltageLevelExt checkAndGetVoltageLevel() {
            if (voltageLevelId == null) {
                throw new ValidationException(this, "voltage level is not set");
            }
            VoltageLevelExt voltageLevel = getNetwork().getVoltageLevel(voltageLevelId);
            if (voltageLevel == null) {
                throw new ValidationException(this, "voltage level '" + voltageLevelId
                        + "' not found");
            }
            return voltageLevel;
        }

        public ThreeWindingsTransformerAdderImpl add() {
            checkParams();
            switch (legNumber) {
                case 1:
                    legAdder1 = this;
                    break;
                case 2:
                    legAdder2 = this;
                    break;
                case 3:
                    legAdder3 = this;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + legNumber);
            }
            return ThreeWindingsTransformerAdderImpl.this;
        }

        @Override
        public String getMessageHeader() {
            return String.format("3 windings transformer leg%d in substation %s: ", legNumber, substation != null ? substation.getId() : "");
        }
    }

    private final Ref<NetworkImpl> networkRef;
    private final SubstationImpl substation;

    private LegAdderImpl legAdder1;

    private LegAdderImpl legAdder2;

    private LegAdderImpl legAdder3;

    private double ratedU0 = Double.NaN;

    ThreeWindingsTransformerAdderImpl(SubstationImpl substation) {
        networkRef = null;
        this.substation = substation;
    }

    ThreeWindingsTransformerAdderImpl(Ref<NetworkImpl> networkRef) {
        this.networkRef = networkRef;
        substation = null;
    }

    ThreeWindingsTransformerAdderImpl(ThreeWindingsTransformer twt, SubstationImpl substation) {
        this(substation);
        ratedU0 = twt.getRatedU0();
        setFictitious(twt.isFictitious());
    }

    @Override
    protected NetworkImpl getNetwork() {
        return Optional.ofNullable(networkRef)
                .map(Ref::get)
                .orElseGet(() -> Optional.ofNullable(substation)
                        .map(SubstationImpl::getNetwork)
                        .orElseThrow(() -> new PowsyblException("Three windings transformer has no container")));
    }

    @Override
    protected String getTypeDescription() {
        return "3 windings transformer";
    }

    @Override
    public LegAdder newLeg1() {
        return new LegAdderImpl(1);
    }

    @Override
    public LegAdder newLeg1(ThreeWindingsTransformer.Leg leg) {
        return new LegAdderImpl(Objects.requireNonNull(leg), 1);
    }

    @Override
    public LegAdder newLeg2() {
        LegAdderImpl legAdder = new LegAdderImpl(2);
        legAdder.g = 0.0;
        legAdder.b = 0.0;
        return legAdder;
    }

    @Override
    public LegAdder newLeg2(ThreeWindingsTransformer.Leg leg) {
        return new LegAdderImpl(Objects.requireNonNull(leg), 2);
    }

    @Override
    public LegAdder newLeg3() {
        LegAdderImpl legAdder = new LegAdderImpl(3);
        legAdder.g = 0.0;
        legAdder.b = 0.0;
        return legAdder;
    }

    @Override
    public LegAdder newLeg3(ThreeWindingsTransformer.Leg leg) {
        return new LegAdderImpl(Objects.requireNonNull(leg), 3);
    }

    @Override
    public ThreeWindingsTransformerAdder setRatedU0(double ratedU0) {
        this.ratedU0 = ratedU0;
        return this;
    }

    @Override
    public ThreeWindingsTransformerImpl add() {
        String id = checkAndGetUniqueId();

        ThreeWindingsTransformerImpl.LegImpl leg1;
        ThreeWindingsTransformerImpl.LegImpl leg2;
        ThreeWindingsTransformerImpl.LegImpl leg3;
        VoltageLevelExt voltageLevel1;
        VoltageLevelExt voltageLevel2;
        VoltageLevelExt voltageLevel3;
        TerminalExt terminal1;
        TerminalExt terminal2;
        TerminalExt terminal3;

        if (legAdder1 != null) {
            voltageLevel1 = legAdder1.checkAndGetVoltageLevel();
            terminal1 = legAdder1.checkAndGetTerminal();
            leg1 = new LegImpl(legAdder1.r, legAdder1.x, legAdder1.g, legAdder1.b, legAdder1.ratedU, legAdder1.ratedS, legAdder1.legNumber);
        } else {
            throw new ValidationException(this, "Leg1 is not set");
        }

        if (legAdder2 != null) {
            voltageLevel2 = legAdder2.checkAndGetVoltageLevel();
            terminal2 = legAdder2.checkAndGetTerminal();
            leg2 = new LegImpl(legAdder2.r, legAdder2.x, legAdder2.g, legAdder2.b, legAdder2.ratedU, legAdder2.ratedS, legAdder2.legNumber);
        } else {
            throw new ValidationException(this, "Leg2 is not set");
        }

        if (legAdder3 != null) {
            voltageLevel3 = legAdder3.checkAndGetVoltageLevel();
            terminal3 = legAdder3.checkAndGetTerminal();
            leg3 = new LegImpl(legAdder3.r, legAdder3.x, legAdder3.g, legAdder3.b, legAdder3.ratedU, legAdder3.ratedS, legAdder3.legNumber);
        } else {
            throw new ValidationException(this, "Leg3 is not set");
        }

        if (substation != null) {
            if (voltageLevel1.getSubstation().map(s -> s != substation).orElse(true) || voltageLevel2.getSubstation().map(s -> s != substation).orElse(true) || voltageLevel3.getSubstation().map(s -> s != substation).orElse(true)) {
                throw new ValidationException(this,
                        "the 3 windings of the transformer shall belong to the substation '"
                                + substation.getId() + "' ('" + voltageLevel1.getSubstation().map(Substation::getId).orElse("null") + "', '"
                                + voltageLevel2.getSubstation().map(Substation::getId).orElse("null") + "', '"
                                + voltageLevel3.getSubstation().map(Substation::getId).orElse("null") + "')");
            }
        } else if (voltageLevel1.getSubstation().isPresent() || voltageLevel2.getSubstation().isPresent() || voltageLevel3.getSubstation().isPresent()) {
            throw new ValidationException(this,
                    "the 3 windings of the transformer shall belong to a substation since there are located in voltage levels with substations ('"
                            + voltageLevel1.getId() + "', '" + voltageLevel2.getId() + "', '" + voltageLevel3.getId() + "')");
        }

        // Define ratedU0 equal to ratedU1 if it has not been defined
        if (Double.isNaN(ratedU0)) {
            ratedU0 = leg1.getRatedU();
            LOGGER.info("RatedU0 is not set. Fixed to leg1 ratedU: {}", leg1.getRatedU());
        }

        ThreeWindingsTransformerImpl transformer = new ThreeWindingsTransformerImpl(substation != null ? substation.getNetwork().getRef() : networkRef, id, getName(), isFictitious(), leg1, leg2, leg3,
            ratedU0);
        transformer.addTerminal(terminal1);
        transformer.addTerminal(terminal2);
        transformer.addTerminal(terminal3);

        leg1.setTransformer(transformer);
        leg2.setTransformer(transformer);
        leg3.setTransformer(transformer);
        terminal1.setNum(1);
        terminal2.setNum(2);
        terminal3.setNum(3);

        // check that the 3 windings transformer is attachable on the 3 sides (only
        // verify)
        voltageLevel1.attach(terminal1, true);
        voltageLevel2.attach(terminal2, true);
        voltageLevel3.attach(terminal3, true);

        // do attach
        voltageLevel1.attach(terminal1, false);
        voltageLevel2.attach(terminal2, false);
        voltageLevel3.attach(terminal3, false);

        getNetwork().getIndex().checkAndAdd(transformer);
        getNetwork().getListeners().notifyCreation(transformer);
        return transformer;
    }
}
