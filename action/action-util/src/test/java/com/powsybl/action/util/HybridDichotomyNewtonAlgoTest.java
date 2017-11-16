/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.util;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import static java.lang.Math.log;
import static org.junit.Assert.assertEquals;

public class HybridDichotomyNewtonAlgoTest {

    @Test
    public void test() {
        Map<Integer, Float> test1 = new HashMap<>();
        for (int i = 0; i < 40; i++) {
            test1.put(i, (float) log(4242 * (i + 1)));
        }
        /**
         0:8.352790135124629
         1:9.045937315684574
         2:9.451402423792738
         3:9.739084496244518
         4:9.962228047558728
         5:10.144549604352683
         6:10.29870028417994
         7:10.432231676804463
         8:10.550014712460847
         9:10.655375228118674
         10:10.750685407922997
         11:10.837696784912628
         12:10.917739492586165
         13:10.991847464739886
         14:11.060840336226837
         15:11.125378857364408
         16:11.186003479180844
         17:11.243161893020792
         18:11.297229114291069
         19:11.348522408678619
         20:11.397312572848051
         21:11.443832588482943
         22:11.488284351053778
         23:11.530843965472574
         24:11.571665959992828
         25:11.61088667314611
         26:11.648627001128958
         27:11.684994645299831
         28:11.720085965111101
         29:11.753987516786783
         30:11.786777339609774
         31:11.818526037924354
         32:11.849297696591108
         33:11.87915065974079
         34:11.908138196614042
         35:11.936309073580738
         36:11.963708047768852
         37:11.990376294851014
         38:12.016351781254274
         39:12.041669589238564
         **/
        assertEquals(24, find(1, 11.6f, test1));
        assertEquals(25, find(2, 11.62f, test1));
        assertEquals(30, find(29, 11.81f, test1));
        assertEquals(39, find(12, 1000f, test1));
        assertEquals(38, find(10, (float) log(4242 * (39 + 1)), test1));
        assertEquals(0, find(0, 9.0f, test1));

        Map<Integer, Float> test2 = new HashMap<>();
        for (int i = 0; i < 40; i++) {
            test2.put(i, (float) (20 - log(4242 * (i + 1))));
        }
        /**
         0:11.647209864875371
         1:10.954062684315426
         2:10.548597576207262
         3:10.260915503755482
         4:10.037771952441272
         5:9.855450395647317
         6:9.70129971582006
         7:9.567768323195537
         8:9.449985287539153
         9:9.344624771881326
         10:9.249314592077003
         11:9.162303215087372
         12:9.082260507413835
         13:9.008152535260114
         14:8.939159663773163
         15:8.874621142635592
         16:8.813996520819156
         17:8.756838106979208
         18:8.702770885708931
         19:8.651477591321381
         20:8.602687427151949
         21:8.556167411517057
         22:8.511715648946222
         23:8.469156034527426
         24:8.428334040007172
         25:8.38911332685389
         26:8.351372998871042
         27:8.315005354700169
         28:8.279914034888899
         29:8.246012483213217
         30:8.213222660390226
         31:8.181473962075646
         32:8.150702303408892
         33:8.12084934025921
         34:8.091861803385958
         35:8.063690926419262
         36:8.036291952231148
         37:8.009623705148986
         38:7.983648218745726
         39:7.958330410761436
         */
        assertEquals(0, find(39, 4242f, test2));
        assertEquals(0, find(0, 11.65f, test2));
        assertEquals(10, find(30, 9.3f, test2));
        assertEquals(28, find(29, 8.30f, test2));
        assertEquals(28, find(29, (float) (20 - log(4242 * (27 + 1))), test2));
        assertEquals(29, find(38, 8.25f, test2));
        assertEquals(39, find(39, 7.96f, test2));

        HashMap<Integer, Float> test3 = new HashMap<>();
        test3.put(0, 11.1f);
        test3.put(1, 20.0f);
        assertEquals(1, find(1, 25.15f, test3));

        HashMap<Integer, Float> test4 = new HashMap<>();
        test4.put(0, 20.0f);
        test4.put(1, 10.0f);
        assertEquals(1, find(1, 11.0f, test4));
        assertEquals(0, find(1, 30.0f, test4));
    }

    static int find(int tapPosArgu, float limit, Map<Integer, Float> tapCurrentMap) {
        int tapPos = tapPosArgu;
        TreeSet<Integer> taps = new TreeSet<>(tapCurrentMap.keySet());

        int tapPosInc = 1;
        int optimalTap = tapPos;
        int maxTap = taps.last();
        int minTap = taps.first();
        int topTap = maxTap;
        int btmTap = minTap;

        float iMax = tapCurrentMap.get(maxTap);
        float iMin = tapCurrentMap.get(minTap);
        float i = tapCurrentMap.get(tapPos);
        float direction = iMax > i ? 1.0f : -1.0f;
        if (direction < 0) {
            float itmp = iMin;
            iMin = iMax;
            iMax = itmp;
            int ttmp = minTap;
            minTap = maxTap;
            maxTap = ttmp;
        }

        if ((iMax - limit) * (iMin - limit) < 0) {
            // we can find a optimal tap
            optimalTap = tapPos;
            if (direction > 0) {
                btmTap = tapPos;
            } else {
                topTap = tapPos;
            }
            iMin = i;
            do {
                float ratio = (limit - i) / (iMax - iMin);
                if (direction > 0) {
                    tapPosInc = (int) (ratio * (topTap - tapPos));
                } else {
                    tapPosInc = (int) (ratio * (tapPos - btmTap));
                }
                if (tapPosInc == 0) {
                    // too little
                    tapPosInc = ratio > 0 ? 1 : -1;
                }
                tapPosInc = (int) direction * tapPosInc;

                tapPos += tapPosInc;
                i = tapCurrentMap.get(tapPos);

                if (i >= limit) {
                    if (direction > 0) {
                        topTap = tapPos;
                    } else {
                        btmTap = tapPos;
                    }
                    iMax = i;
                } else {
                    if (direction > 0) {
                        btmTap = optimalTap;
                    } else {
                        topTap = optimalTap;
                    }
                    optimalTap = tapPos;
                    iMin = i;
                }
            } while (topTap - btmTap > 1);
        } else if (iMax == limit) {
            optimalTap = maxTap - 1;
        } else if (iMin == limit) {
            optimalTap = minTap + 1;
        } else {
            // all the tap under limit
            optimalTap = iMax > iMin ? maxTap : minTap;
        }
        return optimalTap;
    }
}
