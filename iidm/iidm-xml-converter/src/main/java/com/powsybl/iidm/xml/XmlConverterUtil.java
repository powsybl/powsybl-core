/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.ReadOnlyDataSource;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class XmlConverterUtil {

    private XmlConverterUtil() {
    }

    static final String[] EXTENSIONS = {"xiidm", "iidm", "xml"};

    static String findExtension(ReadOnlyDataSource dataSource) {
        for (String ext : XmlConverterUtil.EXTENSIONS) {
            if (dataSource.getMainFileName().endsWith('.' + ext)) {
                return ext;
            }
        }
        return null;
    }

    static String getBaseName(ReadOnlyDataSource dataSource) {
        String ext = findExtension(dataSource);
        if (ext == null) {
            throw new PowsyblException("IIDM extension not found, impossible de find a base name");
        }
        String mainFileName = dataSource.getMainFileName();
        return mainFileName.substring(0, mainFileName.length() - ext.length() - 1);
    }
}
