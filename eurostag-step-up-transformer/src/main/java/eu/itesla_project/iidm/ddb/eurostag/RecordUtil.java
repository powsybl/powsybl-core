/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class RecordUtil {

    private RecordUtil() {
    }

    static String parseString(String line, int start, int end) {
        int end2 = Math.min(end, line.length());
        if (end2 <= start) {
            return "";
        }
        return line.substring(start, end2).trim();
    }

    static float parseFloat(String line, int start, int end) {
        int end2 = Math.min(end, line.length());
        if (end2 <= start) {
            return Float.NaN;
        }
        String str = line.substring(start, end2);
        if (str.trim().isEmpty()) {
            return Float.NaN;
        }
        return Float.parseFloat(str);
    }

    static int parseInt(String line, int start, int end) {
        int end2 = Math.min(end, line.length());
        if (end2 <= start) {
            return -1;
        }
        String str = line.substring(start, end2);
        if (str.trim().isEmpty()) {
            return -1;
        }
        return Integer.parseInt(str.trim());
    }

}
