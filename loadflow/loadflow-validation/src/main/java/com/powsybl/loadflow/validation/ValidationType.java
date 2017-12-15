/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import com.powsybl.iidm.network.Network;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public enum ValidationType {
    FLOWS("branches_flows.csv"),
    GENERATORS("generators.csv"),
    BUSES("buses.csv"),
    SVCS("svcs.csv"),
    SHUNTS("shunts.csv");

    private final String file;

    ValidationType(String file) {
        this.file = Objects.requireNonNull(file);
    }

    public boolean check(Network network, ValidationConfig config, Path folder) throws IOException {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(folder);
        switch (this) {
            case FLOWS:
                return FlowsValidation.checkFlows(network, config, folder.resolve(file));
            case GENERATORS:
                return GeneratorsValidation.checkGenerators(network, config, folder.resolve(file));
            case BUSES:
                return BusesValidation.checkBuses(network, config, folder.resolve(file));
            case SVCS:
                return StaticVarCompensatorsValidation.checkSVCs(network, config, folder.resolve(file));
            case SHUNTS:
                return ShuntCompensatorsValidation.checkShunts(network, config, folder.resolve(file));
            default:
                throw new AssertionError("Unexpected ValidationType value: " + this);
        }
    }

}
