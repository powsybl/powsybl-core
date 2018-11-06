/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class XmlConverterUtil {

    private XmlConverterUtil() {
    }

    static final String[] EXTENSIONS = {"xiidm", "iidm", "xml"};

    static String findExtension(String mainFileName) {
        Objects.requireNonNull(mainFileName);
        for (String ext : XmlConverterUtil.EXTENSIONS) {
            if (mainFileName.endsWith('.' + ext)) {
                return ext;
            }
        }
        return null;
    }

    static String getBaseName(String mainFileName) {
        String ext = findExtension(mainFileName);
        if (ext == null) {
            throw new PowsyblException("IIDM extension not found, impossible to find a base name");
        }
        return mainFileName.substring(0, mainFileName.length() - ext.length() - 1);
    }
}
