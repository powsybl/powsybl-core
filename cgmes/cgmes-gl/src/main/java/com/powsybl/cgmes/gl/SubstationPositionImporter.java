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
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.iidm.network.extensions.SubstationPositionAdder;
import com.powsybl.triplestore.api.PropertyBag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class SubstationPositionImporter {

    private static final Logger LOG = LoggerFactory.getLogger(SubstationPositionImporter.class);

    private final Network network;

    private final CgmesGLModel cgmesGLModel;

    public SubstationPositionImporter(Network network, CgmesGLModel cgmesGLModel) {
        this.network = Objects.requireNonNull(network);
        this.cgmesGLModel = Objects.requireNonNull(cgmesGLModel);
    }

    public void importPositions() {
        Map<Substation, List<Coordinate>> vlCoordinates = new HashMap<>();

        cgmesGLModel.getSubstationsVoltageLevelsPosition().forEach(propertyBag -> importPositions(propertyBag, vlCoordinates));

        vlCoordinates.forEach((substation, coordinates) -> {
            // only calculating the average position if there's no position on the corresponding substation
            if (substation.getExtension(SubstationPosition.class) == null) {
                double latG = coordinates.stream().mapToDouble(Coordinate::getLatitude).average().orElse(0);
                double longG = coordinates.stream().mapToDouble(Coordinate::getLongitude).average().orElse(0);
                substation.newExtension(SubstationPositionAdder.class).withCoordinate(new Coordinate(latG, longG)).add();
            }
        });
    }

    public void importPositions(PropertyBag psrPositionData, Map<Substation, List<Coordinate>> vlCoordinates) {
        Objects.requireNonNull(psrPositionData);
        String crsUrn = psrPositionData.getId("crsUrn");
        if (!CgmesGLUtils.checkCoordinateSystem(crsUrn)) {
            throw new PowsyblException("Unsupported coordinates system: " + crsUrn);
        }
        // Coordinate system EPSG::4326 is WGS84 with y <=> lat, x <=> lon
        Coordinate coordinate = new Coordinate(psrPositionData.asDouble("y"), psrPositionData.asDouble("x"));

        String psrId = psrPositionData.getId("powerSystemResource");
        Substation substation = network.getSubstation(psrId);
        if (substation != null) {
            // the extension is added right away
            substation.newExtension(SubstationPositionAdder.class).withCoordinate(coordinate).add();
        } else {
            VoltageLevel vl = network.getVoltageLevel(psrId);
            if (vl != null) {
                // we need to collect all the positions of all voltage levels for the corresponding substation before adding the extension
                vl.getSubstation().ifPresent(s -> vlCoordinates.computeIfAbsent(s, k -> new ArrayList<>()).add(coordinate));
            } else {
                String name = psrPositionData.get("name");
                LOG.warn("Cannot find substation/voltage level {}, name {} in network {}: skipping substation position", psrId, name, network.getId());
            }
        }
    }
}
