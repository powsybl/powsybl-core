/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.dgs;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class DgsIdValueParser implements DgsValueParser {

    private final int index;

    DgsIdValueParser(int index) {
        this.index = index;
    }

    @Override
    public void parse(String[] fields, DgsHandler handler, DgsParsingContext context) {
        handler.onID(Long.parseLong(fields[index]));
    }
}
