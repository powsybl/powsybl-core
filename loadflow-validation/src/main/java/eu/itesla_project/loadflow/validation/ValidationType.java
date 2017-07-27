/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.loadflow.validation;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import eu.itesla_project.iidm.network.Network;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public enum ValidationType {
    FLOWS("branches_flows.csv"),
    GENERATORS("generators.csv");

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
            return Validation.checkFlows(network, config, folder.resolve(file));
        case GENERATORS:
            return Validation.checkGenerators(network, config, folder.resolve(file));
        default:
            throw new InternalError();
        }
    }

}
