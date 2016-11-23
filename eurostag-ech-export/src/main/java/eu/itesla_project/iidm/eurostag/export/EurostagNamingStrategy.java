/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.eurostag.export;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface EurostagNamingStrategy {

    enum NameType {
        NODE(8),
        GENERATOR(8),
        LOAD(8),
        BANK(8),
        SVC(8);

        int length;

        NameType(int length) {
            this.length = length;
        }

        public int getLength() {
            return length;
        }
    }

    void fillDictionary(EurostagDictionary dictionary, NameType nameType, Set<String> iidmIds);

}
