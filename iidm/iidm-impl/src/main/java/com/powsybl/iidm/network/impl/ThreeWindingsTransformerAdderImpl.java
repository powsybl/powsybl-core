/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.ThreeWindingsTransformerImpl.LegImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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

        protected double g = 0.0;

        protected double b = 0.0;

        protected double ratedU = Double.NaN;

        protected double ratedS = Double.NaN;

        private final ThreeSides side;

        LegAdderImpl(ThreeSides side) {
            this.side = Objects.requireNonNull(side);
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
            VoltageLevelExt voltageLevel = checkAndGetVoltageLevel();
            return new TerminalBuilder(voltageLevel.getNetworkRef(), this, side)
                .setNode(node)
                .setBus(bus)
                .setConnectableBus(connectableBus)
                .build();
        }

        protected VoltageLevelExt checkAndGetVoltageLevel() {
            if (voltageLevelId == null) {
                String defaultVoltageLevelId = checkAndGetDefaultVoltageLevelId();
                if (defaultVoltageLevelId == null) {
                    throw new ValidationException(this, "voltage level is not set and has no default value");
                } else {
                    voltageLevelId = defaultVoltageLevelId;
                }
            }
            VoltageLevelExt voltageLevel = getNetwork().getVoltageLevel(voltageLevelId);
            if (voltageLevel == null) {
                throw new ValidationException(this, "voltage level '" + voltageLevelId
                    + "' not found");
            }
            return voltageLevel;
        }

        private String checkAndGetDefaultVoltageLevelId() {
            if (connectableBus == null) {
                return null;
            }
            BusExt busExt = (BusExt) getNetwork().getBusBreakerView().getBus(connectableBus);
            if (busExt == null) {
                throw new ValidationException(this, "bus ID '" + connectableBus + "' not found");
            }
            return busExt.getVoltageLevel().getId();
        }

        protected void checkConnectableBus() {
            if (connectableBus == null && bus != null) {
                connectableBus = bus;
            }
        }

        public ThreeWindingsTransformerAdderImpl add() {
            checkParams();
            switch (side) {
                case ONE:
                    legAdder1 = this;
                    break;
                case TWO:
                    legAdder2 = this;
                    break;
                case THREE:
                    legAdder3 = this;
                    break;
                default:
                    throw new IllegalStateException("Unexpected side: " + side);
            }
            return ThreeWindingsTransformerAdderImpl.this;
        }

        @Override
        public String getMessageHeader() {
            return String.format("3 windings transformer leg%d in substation %s: ", side.getNum(), substation.getId());
        }
    }

    private final SubstationImpl substation;

    private LegAdderImpl legAdder1;

    private LegAdderImpl legAdder2;

    private LegAdderImpl legAdder3;

    private double ratedU0 = Double.NaN;

    ThreeWindingsTransformerAdderImpl(SubstationImpl substation) {
        this.substation = substation;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return substation.getNetwork();
    }

    @Override
    protected String getTypeDescription() {
        return "3 windings transformer";
    }

    @Override
    public LegAdder newLeg1() {
        return new LegAdderImpl(ThreeSides.ONE);
    }

    @Override
    public LegAdder newLeg2() {
        return new LegAdderImpl(ThreeSides.TWO);
    }

    @Override
    public LegAdder newLeg3() {
        return new LegAdderImpl(ThreeSides.THREE);
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
            legAdder1.checkConnectableBus();
            voltageLevel1 = legAdder1.checkAndGetVoltageLevel();
            terminal1 = legAdder1.checkAndGetTerminal();
            leg1 = new LegImpl(legAdder1.r, legAdder1.x, legAdder1.g, legAdder1.b, legAdder1.ratedU, legAdder1.ratedS, legAdder1.side);
        } else {
            throw new ValidationException(this, "Leg1 is not set");
        }

        if (legAdder2 != null) {
            legAdder2.checkConnectableBus();
            voltageLevel2 = legAdder2.checkAndGetVoltageLevel();
            terminal2 = legAdder2.checkAndGetTerminal();
            leg2 = new LegImpl(legAdder2.r, legAdder2.x, legAdder2.g, legAdder2.b, legAdder2.ratedU, legAdder2.ratedS, legAdder2.side);
        } else {
            throw new ValidationException(this, "Leg2 is not set");
        }

        if (legAdder3 != null) {
            legAdder3.checkConnectableBus();
            voltageLevel3 = legAdder3.checkAndGetVoltageLevel();
            terminal3 = legAdder3.checkAndGetTerminal();
            leg3 = new LegImpl(legAdder3.r, legAdder3.x, legAdder3.g, legAdder3.b, legAdder3.ratedU, legAdder3.ratedS, legAdder3.side);
        } else {
            throw new ValidationException(this, "Leg3 is not set");
        }

        if (voltageLevel1.getSubstation().map(s -> s != substation).orElse(true) || voltageLevel2.getSubstation().map(s -> s != substation).orElse(true) || voltageLevel3.getSubstation().map(s -> s != substation).orElse(true)) {
            throw new ValidationException(this,
                    "the 3 windings of the transformer shall belong to the substation '"
                            + substation.getId() + "' ('" + voltageLevel1.getSubstation().map(Substation::getId).orElse("null") + "', '"
                            + voltageLevel2.getSubstation().map(Substation::getId).orElse("null") + "', '"
                            + voltageLevel3.getSubstation().map(Substation::getId).orElse("null") + "')");
        }

        // Define ratedU0 equal to ratedU1 if it has not been defined
        if (Double.isNaN(ratedU0)) {
            ratedU0 = leg1.getRatedU();
            LOGGER.info("RatedU0 is not set. Fixed to leg1 ratedU: {}", leg1.getRatedU());
        }

        ThreeWindingsTransformerImpl transformer = new ThreeWindingsTransformerImpl(substation.getNetworkRef(), id, getName(), isFictitious(), leg1, leg2, leg3,
            ratedU0);
        transformer.addTerminal(terminal1);
        transformer.addTerminal(terminal2);
        transformer.addTerminal(terminal3);

        leg1.setTransformer(transformer);
        leg2.setTransformer(transformer);
        leg3.setTransformer(transformer);

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
