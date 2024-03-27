/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata;

import com.powsybl.iidm.geodata.dto.LineGeoData;
import com.powsybl.iidm.geodata.dto.SubstationGeoData;

import java.nio.file.Path;
import java.util.List;

/**
 * @author Hugo Kulesza <hugo.kulesza at rte-france.com>
 */
public interface GeoDataSourceLoader {

    List<SubstationGeoData> getSubstationsGeoData(Path path);

    List<LineGeoData> getLinesGeoData(Path path);
}
