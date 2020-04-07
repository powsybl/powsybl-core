/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;
import com.powsybl.iidm.network.impl.ThreeWindingsTransformerImpl.LegImpl;
import com.powsybl.iidm.network.Validable;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;

/**
 *
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

        protected int legNumber = 0;

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
            if (voltageLevel.getSubstation() != substation) {
                throw new ValidationException(this,
                    "voltage level shall belong to the substation '"
                        + substation.getId() + "'");
            }
            return voltageLevel;
        }

        public ThreeWindingsTransformerAdderImpl add() {
            checkParams();
            if (legNumber == 1) {
                voltageLevel1 = checkAndGetVoltageLevel();
                terminal1 = checkAndGetTerminal();
                leg1 = new LegImpl(r, x, g, b, ratedU, ratedS, legNumber);
            } else if (legNumber == 2) {
                voltageLevel2 = checkAndGetVoltageLevel();
                terminal2 = checkAndGetTerminal();
                leg2 = new LegImpl(r, x, g, b, ratedU, ratedS, legNumber);
            } else {
                voltageLevel3 = checkAndGetVoltageLevel();
                terminal3 = checkAndGetTerminal();
                leg3 = new LegImpl(r, x, g, b, ratedU, ratedS, legNumber);
            }
            return ThreeWindingsTransformerAdderImpl.this;
        }

        @Override
        public String getMessageHeader() {
            return String.format("3 windings transformer leg%d: ", legNumber);
        }
    }

    private final SubstationImpl substation;

    private ThreeWindingsTransformerImpl.LegImpl leg1;

    private ThreeWindingsTransformerImpl.LegImpl leg2;

    private ThreeWindingsTransformerImpl.LegImpl leg3;

    private VoltageLevelExt voltageLevel1;

    private VoltageLevelExt voltageLevel2;

    private VoltageLevelExt voltageLevel3;

    private TerminalExt terminal1;

    private TerminalExt terminal2;

    private TerminalExt terminal3;

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
        LegAdderImpl legAdderImp = new LegAdderImpl();
        legAdderImp.legNumber = 1;
        return legAdderImp;
    }

    @Override
    public LegAdder newLeg2() {
        LegAdderImpl legAdderImp = new LegAdderImpl();
        legAdderImp.g = 0.0;
        legAdderImp.b = 0.0;
        legAdderImp.legNumber = 2;
        return legAdderImp;
    }

    @Override
    public LegAdder newLeg3() {
        LegAdderImpl legAdderImp = new LegAdderImpl();
        legAdderImp.g = 0.0;
        legAdderImp.b = 0.0;
        legAdderImp.legNumber = 3;
        return legAdderImp;
    }

    @Override
    public ThreeWindingsTransformerAdder setRatedU0(double ratedU0) {
        this.ratedU0 = ratedU0;
        return this;
    }

    @Override
    public ThreeWindingsTransformerImpl add() {
        String id = checkAndGetUniqueId();

        if (leg1 == null || voltageLevel1 == null || terminal1 == null) {
            throw new ValidationException(this, "Leg1 is not set");
        }
        if (leg2 == null || voltageLevel2 == null || terminal2 == null) {
            throw new ValidationException(this, "Leg2 is not set");
        }
        if (leg3 == null || voltageLevel3 == null || terminal3 == null) {
            throw new ValidationException(this, "Leg3 is not set");
        }

        // check that the 3 windings transformer is attachable on the 3 sides (only
        // verify)
        voltageLevel1.attach(terminal1, true);
        voltageLevel2.attach(terminal2, true);
        voltageLevel3.attach(terminal3, true);

        // Define ratedU0 equal to ratedU1 if it has not been defined
        if (Double.isNaN(ratedU0)) {
            ratedU0 = leg1.getRatedU();
            LOGGER.info("RatedU0 is not set. Fixed to leg1 ratedU: {}", leg1.getRatedU());
        }

        ThreeWindingsTransformerImpl transformer = new ThreeWindingsTransformerImpl(id, getName(), isFictitious(), leg1, leg2, leg3,
            ratedU0);
        leg1.setTransformer(transformer);
        leg2.setTransformer(transformer);
        leg3.setTransformer(transformer);
        terminal1.setNum(1);
        terminal2.setNum(2);
        terminal3.setNum(3);
        transformer.addTerminal(terminal1);
        transformer.addTerminal(terminal2);
        transformer.addTerminal(terminal3);

        // do attach
        voltageLevel1.attach(terminal1, false);
        voltageLevel2.attach(terminal2, false);
        voltageLevel3.attach(terminal3, false);

        getNetwork().getIndex().checkAndAdd(transformer);
        getNetwork().getListeners().notifyCreation(transformer);

        return transformer;
    }
}
