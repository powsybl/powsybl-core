/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;
import com.powsybl.iidm.network.impl.ThreeWindingsTransformerImpl.Leg1Impl;
import com.powsybl.iidm.network.impl.ThreeWindingsTransformerImpl.Leg2Impl;
import com.powsybl.iidm.network.impl.ThreeWindingsTransformerImpl.Leg3Impl;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ThreeWindingsTransformerAdderImpl extends AbstractIdentifiableAdder<ThreeWindingsTransformerAdderImpl> implements ThreeWindingsTransformerAdder {

    abstract class AbstractLegBaseAdder<L extends AbstractLegBaseAdder<L>> implements Validable {

        protected String voltageLevelId;

        protected Integer node;

        protected String bus;

        protected String connectableBus;

        protected double r = Double.NaN;

        protected double x = Double.NaN;

        protected double ratedU = Double.NaN;

        public L setVoltageLevel(String voltageLevelId) {
            this.voltageLevelId = voltageLevelId;
            return (L) this;
        }

        public L setNode(int node) {
            this.node = node;
            return (L) this;
        }

        public L setBus(String bus) {
            this.bus = bus;
            return (L) this;
        }

        public L setConnectableBus(String connectableBus) {
            this.connectableBus = connectableBus;
            return (L) this;
        }

        public L setR(double r) {
            this.r = r;
            return (L) this;
        }

        public L setX(double x) {
            this.x = x;
            return (L) this;
        }

        public L setRatedU(double ratedU) {
            this.ratedU = ratedU;
            return (L) this;
        }

        protected void checkParams() {
            if (Double.isNaN(r)) {
                throw new ValidationException(this, "r is not set");
            }
            if (Double.isNaN(x)) {
                throw new ValidationException(this, "x is not set");
            }
            if (Double.isNaN(ratedU)) {
                throw new ValidationException(this, "rated u is not set");
            }
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
    }

    class Leg1AdderImpl extends AbstractLegBaseAdder<Leg1AdderImpl> implements Leg1Adder {

        protected double g = Double.NaN;

        protected double b = Double.NaN;

        @Override
        public Leg1AdderImpl setG(double g) {
            this.g = g;
            return this;
        }

        @Override
        public Leg1AdderImpl setB(double b) {
            this.b = b;
            return this;
        }

        @Override
        protected void checkParams() {
            super.checkParams();
            if (Double.isNaN(g)) {
                throw new ValidationException(this, "g is not set");
            }
            if (Double.isNaN(b)) {
                throw new ValidationException(this, "b is not set");
            }
        }

        @Override
        public ThreeWindingsTransformerAdderImpl add() {
            checkParams();
            voltageLevel1 = checkAndGetVoltageLevel();
            terminal1 = checkAndGetTerminal();
            leg1 = new Leg1Impl(r, x, g, b, ratedU);
            return ThreeWindingsTransformerAdderImpl.this;
        }

        @Override
        public String getMessageHeader() {
            return "3 windings transformer leg 1: ";
        }

    }

    class Leg2AdderImpl extends AbstractLegBaseAdder<Leg2AdderImpl> implements Leg2or3Adder {

        @Override
        public ThreeWindingsTransformerAdderImpl add() {
            checkParams();
            voltageLevel2 = checkAndGetVoltageLevel();
            terminal2 = checkAndGetTerminal();
            leg2 = new Leg2Impl(r, x, ratedU);
            return ThreeWindingsTransformerAdderImpl.this;
        }

        @Override
        public String getMessageHeader() {
            return "3 windings transformer leg 2: ";
        }

    }

    class Leg3AdderImpl extends AbstractLegBaseAdder<Leg3AdderImpl> implements Leg2or3Adder {

        @Override
        public ThreeWindingsTransformerAdderImpl add() {
            checkParams();
            voltageLevel3 = checkAndGetVoltageLevel();
            terminal3 = checkAndGetTerminal();
            leg3 = new Leg3Impl(r, x, ratedU);
            return ThreeWindingsTransformerAdderImpl.this;
        }

        @Override
        public String getMessageHeader() {
            return "3 windings transformer leg 3: ";
        }

    }

    private final SubstationImpl substation;

    private ThreeWindingsTransformerImpl.Leg1Impl leg1;

    private ThreeWindingsTransformerImpl.Leg2Impl leg2;

    private ThreeWindingsTransformerImpl.Leg3Impl leg3;

    private VoltageLevelExt voltageLevel1;

    private VoltageLevelExt voltageLevel2;

    private VoltageLevelExt voltageLevel3;

    private TerminalExt terminal1;

    private TerminalExt terminal2;

    private TerminalExt terminal3;

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
    public Leg1AdderImpl newLeg1() {
        return new Leg1AdderImpl();
    }

    @Override
    public Leg2AdderImpl newLeg2() {
        return new Leg2AdderImpl();
    }

    @Override
    public Leg3AdderImpl newLeg3() {
        return new Leg3AdderImpl();
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

        // check that the 3 windings transformer is attachable on the 3 sides
        voltageLevel1.attach(terminal1, true);
        voltageLevel2.attach(terminal2, true);
        voltageLevel3.attach(terminal3, true);

        ThreeWindingsTransformerImpl transformer
                = new ThreeWindingsTransformerImpl(id, getName(), leg1, leg2, leg3);
        leg1.setTransformer(transformer);
        leg2.setTransformer(transformer);
        leg3.setTransformer(transformer);
        terminal1.setNum(1);
        terminal2.setNum(2);
        terminal3.setNum(3);
        transformer.addTerminal(terminal1);
        transformer.addTerminal(terminal2);
        transformer.addTerminal(terminal3);

        voltageLevel1.attach(terminal1, false);
        voltageLevel2.attach(terminal2, false);
        voltageLevel3.attach(terminal3, false);

        getNetwork().getObjectStore().checkAndAdd(transformer);
        getNetwork().getListeners().notifyCreation(transformer);

        return transformer;
    }

}
