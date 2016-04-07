/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.eurostag.export;

import com.google.common.base.Strings;

import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class CutEurostagNamingStrategy implements EurostagNamingStrategy {
    @Override
    public void fillDictionary(EurostagDictionary dictionary, NameType nameType, Set<String> iidmIds) {
        iidmIds.forEach(iidmId -> {
            if (!dictionary.iidmIdExists(iidmId)) {
                String esgId = iidmId.length() > nameType.getLength() ? iidmId.substring(0, nameType.getLength())
                                                                      : Strings.padEnd(iidmId, nameType.getLength(), ' ');
                int counter = 0;
                while (dictionary.esgIdExists(esgId)) {
                    String counterStr = Integer.toString(counter++);
                    if (counterStr.length() > nameType.getLength()) {
                        throw new RuntimeException("Renaming fatal error " + iidmId + " -> " + esgId);
                    }
                    esgId = esgId.substring(0, nameType.getLength() - counterStr.length()) + counterStr;
                }
                dictionary.add(iidmId, esgId);
            }
        });
    }
}

