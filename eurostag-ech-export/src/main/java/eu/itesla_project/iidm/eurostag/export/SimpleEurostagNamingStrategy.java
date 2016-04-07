/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.eurostag.export;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SimpleEurostagNamingStrategy implements EurostagNamingStrategy {

    private static String createIndexedName(char code, long index, int maxSize) {
        int number = maxSize - 1;
        return String.format("%s%0" + number + "d" , code, index);
    }

    private final Map<NameType, AtomicLong> index = new EnumMap<>(NameType.class);

    public SimpleEurostagNamingStrategy() {
        for (NameType nameType : NameType.values()) {
            index.put(nameType, new AtomicLong());
        }
    }

    private String newEsgId(NameType nameType) {
        char code;
        switch (nameType) {
            case NODE:
                code = 'N';
                break;
            case GENERATOR:
                code = 'G';
                break;
            case LOAD:
                code = 'L';
                break;
            case BANK:
                code = 'B';
                break;
            default:
                throw new AssertionError();

        }
        return createIndexedName(code, index.get(nameType).getAndIncrement(), nameType.getLength());
    }

    @Override
    public void fillDictionary(EurostagDictionary dictionary, NameType nameType, Set<String> iidmIds) {
        for (String iidmId : iidmIds) {
            if (!dictionary.iidmIdExists(iidmId)) {
                String esgId;
                while (dictionary.esgIdExists(esgId = newEsgId(nameType))) {
                    // nothing
                }
                dictionary.add(iidmId, esgId);
            }
        }
    }

}
