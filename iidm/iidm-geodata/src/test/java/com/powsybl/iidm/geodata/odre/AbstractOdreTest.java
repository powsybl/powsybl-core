/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.geodata.odre;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
abstract class AbstractOdreTest {

    public static final OdreConfig ODRE_CONFIG1 = new OdreConfig(
            "type_ouvrage",
            "NULL",
            "AERIEN",
            "SOUTERRAIN",
            "code_ligne",
            "identification_2",
            "identification_3",
            "identification_4",
            "identification_5",
            "geo_shape",
            "code_poste",
            "longitude_poste",
            "latitude_poste"
    );

    static Stream<Arguments> provideTestArguments() {
        return Stream.of(
                Arguments.of("default", "eurostag-test/default-headers/", OdreConfig.getDefaultOdreConfig()),
                Arguments.of("alternate-config", "eurostag-test/", ODRE_CONFIG1)
        );
    }
}
