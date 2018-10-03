/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.anonymizer;

import com.powsybl.iidm.network.Country;

import java.io.BufferedReader;
import java.io.BufferedWriter;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Anonymizer {

    String anonymizeString(String str);

    String deanonymizeString(String str);

    Country anonymizeCountry(Country country);

    Country deanonymizeCountry(Country country);

    void read(BufferedReader reader);

    void write(BufferedWriter writer);
}
