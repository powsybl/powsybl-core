/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.dataframe.DataframeElementType;
import com.powsybl.dataframe.network.extensions.NetworkExtensions;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Map;

import static com.powsybl.dataframe.DataframeElementType.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 */
public final class NetworkElementAdders {

    private static final Map<DataframeElementType, NetworkElementAdder> ADDERS = Map.ofEntries(
        Map.entry(BUS, new BusDataframeAdder()),
        Map.entry(GENERATOR, new GeneratorDataframeAdder()),
        Map.entry(LINE, new LineDataframeAdder()),
        Map.entry(STATIC_VAR_COMPENSATOR, new SvcDataframeAdder()),
        Map.entry(TWO_WINDINGS_TRANSFORMER, new TwtDataframeAdder()),
        Map.entry(LOAD, new LoadDataframeAdder()),
        Map.entry(VSC_CONVERTER_STATION, new VscStationDataframeAdder()),
        Map.entry(LCC_CONVERTER_STATION, new LccStationDataframeAdder()),
        Map.entry(BUSBAR_SECTION, new BusBarDataframeAdder()),
        Map.entry(DANGLING_LINE, new DanglingLineDataframeAdder()),
        Map.entry(VOLTAGE_LEVEL, new VoltageLevelDataframeAdder()),
        Map.entry(SUBSTATION, new SubstationDataframeAdder()),
        Map.entry(HVDC_LINE, new HvdcDataframeAdder()),
        Map.entry(BATTERY, new BatteryDataframeAdder()),
        Map.entry(SWITCH, new SwitchDataframeAdder()),
        Map.entry(SHUNT_COMPENSATOR, new ShuntDataframeAdder()),
        Map.entry(PHASE_TAP_CHANGER, new PhaseTapChangerDataframeAdder()),
        Map.entry(RATIO_TAP_CHANGER, new RatioTapChangerDataframeAdder()),
        Map.entry(OPERATIONAL_LIMITS, new OperationalLimitsDataframeAdder()),
        Map.entry(MINMAX_REACTIVE_LIMITS, new MinMaxReactiveLimitsDataframeAdder()),
        Map.entry(REACTIVE_CAPABILITY_CURVE_POINT, new CurveReactiveLimitsDataframeAdder()),
        Map.entry(ALIAS, new AliasDataframeAdder()),
        Map.entry(TIE_LINE, new TieLineDataframeAdder())
    );

    private static final Map<String, NetworkElementAdder> EXTENSIONS_ADDERS = NetworkExtensions.createExtensionsAdders();

    private NetworkElementAdders() {
    }

    public static NetworkElementAdder getAdder(DataframeElementType type) {
        return ADDERS.get(type);
    }

    public static List<NetworkElementAdder> getAdders() {
        return List.copyOf(ADDERS.values());
    }

    public static void addElements(DataframeElementType type, Network network, List<UpdatingDataframe> dfs) {
        NetworkElementAdder adder = ADDERS.get(type);
        if (adder == null) {
            throw new PowsyblException("Creation not implemented for type " + type.name());
        }
        adder.addElements(network, dfs);
    }

    public static void addElementsWithBay(DataframeElementType type, Network network, List<UpdatingDataframe> df,
                                          boolean throwException, Reporter reporter) {
        NetworkElementAdder adder = ADDERS.get(type);
        if (adder == null) {
            throw new PowsyblException("Creation not implemented for type " + type.name());
        }
        adder.addElementsWithBay(network, df, throwException, reporter);
    }

    public static NetworkElementAdder getExtensionAdder(String extensionName) {
        return EXTENSIONS_ADDERS.get(extensionName);
    }

    public static void addExtensions(String name, Network network, List<UpdatingDataframe> dfs) {
        NetworkElementAdder adder = EXTENSIONS_ADDERS.get(name);
        if (adder == null) {
            throw new PowsyblException("Creation not implemented for extension " + name);
        }
        adder.addElements(network, dfs);
    }

}
