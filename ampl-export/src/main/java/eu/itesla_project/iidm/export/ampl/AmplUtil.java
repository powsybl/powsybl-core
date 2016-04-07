/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.export.ampl;

import eu.itesla_project.commons.util.StringToIntMapper;
import eu.itesla_project.iidm.network.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AmplUtil {

    private AmplUtil() {
    }

    public static Iterable<Bus> getBuses(Network n) {
        return n.getBusView().getBuses();
    }

    public static Bus getBus(Terminal t) {
        return t.getBusView().getBus();
    }

    public static Bus getConnectableBus(Terminal t) {
        return t.getBusView().getConnectableBus();
    }

    private static void createLimitsIds(StringToIntMapper<AmplSubset> mapper, CurrentLimits limits, String branchId, String sideId) {
        for (CurrentLimits.TemporaryLimit tl : limits.getTemporaryLimits()) {
            String limitId = branchId + "_" + sideId + "_" + tl.getAcceptableDuration();
            mapper.newInt(AmplSubset.TEMPORARY_CURRENT_LIMIT, limitId);
        }
    }

    public static StringToIntMapper<AmplSubset> createMapper(Network network) {
        StringToIntMapper<AmplSubset> mapper = new StringToIntMapper<>(AmplSubset.class);
        fillMapper(mapper, network);
        return mapper;
    }

    static String getXnodeBusId(TieLine tieLine) {
        return tieLine.getUcteXnodeCode();
    }

    static String getXnodeVoltageLevelId(TieLine tieLine) {
        return tieLine.getUcteXnodeCode();
    }

    public static void fillMapper(StringToIntMapper<AmplSubset> mapper, Network network) {
         // substations
        for (VoltageLevel vl : network.getVoltageLevels()) {
            mapper.newInt(AmplSubset.VOLTAGE_LEVEL, vl.getId());
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            mapper.newInt(AmplSubset.VOLTAGE_LEVEL, twt.getId());
        }
        for (DanglingLine dl : network.getDanglingLines()) {
            mapper.newInt(AmplSubset.VOLTAGE_LEVEL, dl.getId());
        }
        for (Line l : network.getLines()) {
            if (l.isTieLine()) {
                TieLine tl = (TieLine) l;
                String vlId = AmplUtil.getXnodeVoltageLevelId(tl);
                mapper.newInt(AmplSubset.VOLTAGE_LEVEL, vlId);
            }
        }

        // buses
        for (Bus b : getBuses(network)) {
            mapper.newInt(AmplSubset.BUS, b.getId());
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            String id = twt.getId(); // same id as the transformer
            mapper.newInt(AmplSubset.BUS, id);
        }
        for (DanglingLine dl : network.getDanglingLines()) {
            mapper.newInt(AmplSubset.BUS, dl.getId());
        }
        for (Line l : network.getLines()) {
            if (l.isTieLine()) {
                TieLine tl = (TieLine) l;
                String busId = AmplUtil.getXnodeBusId(tl);
                mapper.newInt(AmplSubset.BUS, busId);
            }
        }

        // branches
        for (Line l : network.getLines()) {
            mapper.newInt(AmplSubset.BRANCH, l.getId());
            if (l.isTieLine()) {
                TieLine tl = (TieLine) l;
                mapper.newInt(AmplSubset.BRANCH, tl.getHalf1().getId());
                mapper.newInt(AmplSubset.BRANCH, tl.getHalf2().getId());
            }
        }
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            mapper.newInt(AmplSubset.BRANCH, twt.getId());
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            mapper.newInt(AmplSubset.THREE_WINDINGS_TRANSFO, twt.getId());
            mapper.newInt(AmplSubset.BRANCH, twt.getId() + "_leg1");
            mapper.newInt(AmplSubset.BRANCH, twt.getId() + "_leg2");
            mapper.newInt(AmplSubset.BRANCH, twt.getId() + "_leg3");
        }
        for (DanglingLine dl : network.getDanglingLines()) {
            mapper.newInt(AmplSubset.BRANCH, dl.getId());
        }

        // tct
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            if (twt.getRatioTapChanger() != null) {
                mapper.newInt(AmplSubset.TAP_CHANGER_TABLE, twt.getId() + "_ratio_table");
            }
            if (twt.getPhaseTapChanger() != null) {
                mapper.newInt(AmplSubset.TAP_CHANGER_TABLE, twt.getId() + "_phase_table");
            }
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            if (twt.getLeg2().getRatioTapChanger() != null) {
                mapper.newInt(AmplSubset.TAP_CHANGER_TABLE, twt.getId() + "_leg2_ratio_table");
            }
            if (twt.getLeg3().getRatioTapChanger() != null) {
                mapper.newInt(AmplSubset.TAP_CHANGER_TABLE, twt.getId() + "_leg3_ratio_table");
            }
        }

        // rtc
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            if (twt.getRatioTapChanger() != null) {
                String rtcId = twt.getId();
                mapper.newInt(AmplSubset.RATIO_TAP_CHANGER, rtcId);
            }
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            if (twt.getLeg2().getRatioTapChanger() != null) {
                mapper.newInt(AmplSubset.RATIO_TAP_CHANGER, twt.getId() + "_leg2");
            }
            if (twt.getLeg3().getRatioTapChanger() != null) {
                mapper.newInt(AmplSubset.RATIO_TAP_CHANGER, twt.getId() + "_leg3");
            }
        }

        // ptc
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            if (twt.getPhaseTapChanger() != null) {
                mapper.newInt(AmplSubset.PHASE_TAP_CHANGER, twt.getId());
            }
        }

        // loads
        for (Load l : network.getLoads()) {
            mapper.newInt(AmplSubset.LOAD, l.getId());
        }
        for (DanglingLine dl : network.getDanglingLines()) {
            mapper.newInt(AmplSubset.LOAD, dl.getId());
        }

        // shunts
        for (ShuntCompensator sc : network.getShunts()) {
            mapper.newInt(AmplSubset.SHUNT, sc.getId());
        }

        // generators
        for (Generator g : network.getGenerators()) {
            mapper.newInt(AmplSubset.GENERATOR, g.getId());
        }

        // limits
        for (Line l : network.getLines()) {
            if (l.getCurrentLimits1() != null) {
                createLimitsIds(mapper, l.getCurrentLimits1(), l.getId(), "_1_");
            }
            if (l.getCurrentLimits2() != null) {
                createLimitsIds(mapper, l.getCurrentLimits2(), l.getId(), "_2_");
            }
        }
        for (TwoWindingsTransformer twt : network.getTwoWindingsTransformers()) {
            if (twt.getCurrentLimits1() != null) {
                createLimitsIds(mapper, twt.getCurrentLimits1(), twt.getId(), "_1_");
            }
            if (twt.getCurrentLimits2() != null) {
                createLimitsIds(mapper, twt.getCurrentLimits2(), twt.getId(), "_2_");
            }
        }
        for (ThreeWindingsTransformer twt : network.getThreeWindingsTransformers()) {
            if (twt.getLeg1().getCurrentLimits() != null) {
                createLimitsIds(mapper, twt.getLeg1().getCurrentLimits(), twt.getId() + "_leg1", "");
            }
            if (twt.getLeg2().getCurrentLimits() != null) {
                createLimitsIds(mapper, twt.getLeg2().getCurrentLimits(),  twt.getId() + "_leg2", "");
            }
            if (twt.getLeg3().getCurrentLimits() != null) {
                createLimitsIds(mapper, twt.getLeg3().getCurrentLimits(),  twt.getId() + "_leg3", "");
            }
        }
        for (DanglingLine dl : network.getDanglingLines()) {
            if (dl.getCurrentLimits() != null) {
                createLimitsIds(mapper, dl.getCurrentLimits(), dl.getId(), "");
            }
        }
    }

}
