/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ieeecdf.converter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.ieeecdf.model.IeeeCdfBus;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;

import java.util.Properties;
import java.util.function.ToDoubleFunction;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class IeeeCdfNetworkFactory {

    private IeeeCdfNetworkFactory() {
    }

    private static Network create(String baseName, NetworkFactory networkFactory, Properties properties,
                                  ToDoubleFunction<IeeeCdfBus> nominalVoltageProvider) {
        return new IeeeCdfImporter(nominalVoltageProvider).importData(new ResourceDataSource(baseName, new ResourceSet("/", baseName + ".txt")), networkFactory, properties);
    }

    private static Network create(String baseName, NetworkFactory networkFactory, Properties properties) {
        return create(baseName, networkFactory, properties, IeeeCdfImporter.DEFAULT_NOMINAL_VOLTAGE_PROVIDER);
    }

    private static Network create(String baseName, NetworkFactory networkFactory) {
        return create(baseName, networkFactory, null);
    }

    public static Network create9(NetworkFactory networkFactory) {
        return create("ieee9cdf", networkFactory);
    }

    public static Network create9() {
        return create9(NetworkFactory.findDefault());
    }

    public static Network create14(NetworkFactory networkFactory) {
        // the nominal voltage provider given here follows no convention rules but is an assumption
        return create("ieee14cdf", networkFactory, null, ieeeCdfBus -> {
            if (ieeeCdfBus.getName().endsWith("HV")) {
                return 135;
            } else if (ieeeCdfBus.getName().endsWith("TV")) {
                return 20;
            } else if (ieeeCdfBus.getName().endsWith("ZV")) {
                return 14;
            } else if (ieeeCdfBus.getName().endsWith("LV")) {
                return 12;
            } else {
                throw new PowsyblException("Cannot find base voltage from bus name: '" + ieeeCdfBus.getName() + "'");
            }
        });
    }

    public static Network create14() {
        return create14(NetworkFactory.findDefault());
    }

    // test case adapted to CDF from .raw file obtained
    // https://icseg.iti.illinois.edu/ieee-14-bus-system/
    public static Network create14Solved(NetworkFactory networkFactory) {
        Properties properties = new Properties();
        properties.setProperty("ignore-base-voltage", "true");
        return create("ieee14cdf-solved", networkFactory, properties);
    }

    public static Network create14Solved() {
        return create14Solved(NetworkFactory.findDefault());
    }

    public static Network create30(NetworkFactory networkFactory) {
        return create("ieee30cdf", networkFactory);
    }

    public static Network create30() {
        return create30(NetworkFactory.findDefault());
    }

    public static Network create57(NetworkFactory networkFactory) {
        return create("ieee57cdf", networkFactory);
    }

    public static Network create57() {
        return create57(NetworkFactory.findDefault());
    }

    public static Network create118(NetworkFactory networkFactory) {
        // the nominal voltage provider given here follows no convention rules but is an assumption
        return create("ieee118cdf", networkFactory, null, ieeeCdfBus -> {
            if (ieeeCdfBus.getName().endsWith("V1")) {
                return 138;
            } else if (ieeeCdfBus.getName().endsWith("V2")) {
                return 161;
            } else if (ieeeCdfBus.getName().endsWith("V3")) {
                return 345;
            } else {
                throw new PowsyblException("Cannot find base voltage from bus name: '" + ieeeCdfBus.getName() + "'");
            }
        });
    }

    public static Network create118() {
        return create118(NetworkFactory.findDefault());
    }

    public static Network create300(NetworkFactory networkFactory) {
        return create("ieee300cdf", networkFactory);
    }

    public static Network create300() {
        return create300(NetworkFactory.findDefault());
    }

    public static Network create9zeroimpedance(NetworkFactory networkFactory) {
        return create("ieee9zeroimpedancecdf", networkFactory);
    }

    public static Network create9zeroimpedance() {
        return create9zeroimpedance(NetworkFactory.findDefault());
    }
}
