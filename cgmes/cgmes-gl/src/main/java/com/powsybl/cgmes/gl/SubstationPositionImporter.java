/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPositionAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class SubstationPositionImporter {

    private static final Logger LOG = LoggerFactory.getLogger(SubstationPositionImporter.class);

    private Network network;

    public SubstationPositionImporter(Network network) {
        this.network = Objects.requireNonNull(network);
    }

    public void importPosition(PropertyBag substationPositionData) {
        Objects.requireNonNull(substationPositionData);
        if (!CgmesGLUtils.checkCoordinateSystem(substationPositionData.getId("crsName"), substationPositionData.getId("crsUrn"))) {
            throw new PowsyblException("Unsupported coodinates system: " + substationPositionData.getId("crsName"));
        }
        String substationId = substationPositionData.getId("powerSystemResource");
        Substation substation = network.getSubstation(substationId);
        if (substation != null) {
            // y <=> lat, x <=> lon
            substation.newExtension(SubstationPositionAdder.class).withCoordinate(
                new Coordinate(substationPositionData.asDouble("y"), substationPositionData.asDouble("x"))
            ).add();
        } else {
            LOG.warn("Cannot find substation {}, name {} in network {}: skipping substation position", substationId, substationPositionData.get("name"), network.getId());
        }
    }

}
