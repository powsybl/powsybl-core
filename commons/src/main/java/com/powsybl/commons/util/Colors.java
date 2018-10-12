/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class Colors {

    private static final double GOLDEN_RATIO_CONJUGATE = 0.618033988749895;

    private static final Random RANDOM = new SecureRandom();

    private Colors() {
    }

    public static String[] generateColorScale(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Color scale size must be positive");
        }
        String[] colors = new String[n];
        for (int i = 0; i < n; i++) {
            double h = RANDOM.nextDouble();
            h += GOLDEN_RATIO_CONJUGATE;
            h %= 1;
            long[] rgb = hsvToRgb(h, 0.5, 0.95);
            String hex = String.format("#%02x%02x%02x", rgb[0], rgb[1], rgb[2]).toUpperCase();
            colors[i] = hex;
        }
        return colors;
    }

    private static long[] hsvToRgb(double h, double s, double v) {
        int hi = (int) Math.floor(h * 6);
        double f = h * 6 - hi;
        double p = v * (1 - s);
        double q = v * (1 - f * s);
        double t = v * (1 - (1 - f) * s);
        double r;
        double g;
        double b;
        switch (hi) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            case 5:
                r = v;
                g = p;
                b = q;
                break;
            default:
                throw new AssertionError();
        }
        return new long[] {Math.round(r * 256), Math.round(g * 256), Math.round(b * 256)};
    }
}
