/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum UcteFormatVersion {
    FIRST("2003.09.01"),
    SECOND("2007.05.01");

    private final String date;

    UcteFormatVersion(String date) {
        this.date = Objects.requireNonNull(date);
    }

    public String getDate() {
        return date;
    }

    public static UcteFormatVersion findByDate(String date) {
        if (date == null) {
            return null;
        } else {
            for (UcteFormatVersion v : UcteFormatVersion.values()) {
                if (v.getDate().equals(date)) {
                    return v;
                }
            }
        }
        throw new IllegalArgumentException("Unknown format version " + date);
    }

}
