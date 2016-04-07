/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class T4X {

    public final String name1;
    public final String name2;
    public final float rate;
    public final float pcu;
    public final float pfer;
    public final float cmagn;
    public final float esat;
    public final int ktpnom;
    public final int ktap8;
    public final List<Integer> iplo;
    public final List<Float> uno1;
    public final List<Float> uno2;
    public final List<Float> ucc ;
    public final List<Float> dephas;

    T4X(String name1, String name2, float rate, float pcu,
            float pfer, float cmagn, float esat, int ktpnom, int ktap8,
            List<Integer> iplo, List<Float> uno1, List<Float> uno2,
            List<Float> ucc, List<Float> dephas) {
        this.name1 = name1;
        this.name2 = name2;
        this.rate = rate;
        this.pcu = pcu;
        this.pfer = pfer;
        this.cmagn = cmagn;
        this.esat = esat;
        this.ktpnom = ktpnom;
        this.ktap8 = ktap8;
        this.iplo = iplo;
        this.uno1 = uno1;
        this.uno2 = uno2;
        this.ucc = ucc;
        this.dephas = dephas;
    }

    @Override
    public String toString() {
        return "4X: name1=" + name1 +
                ", name2=" + name2 +
                ", rate=" + rate +
                ", pcu=" + pcu +
                ", pfer=" + pfer +
                ", cmagn=" + cmagn +
                ", esat=" + esat +
                ", ktpnom=" + ktpnom +
                ", ktap8=" + ktap8 +
                ", iplo=" + iplo +
                ", uno1=" + uno1 +
                ", uno2=" + uno2 +
                ", ucc=" + ucc +
                ", dephas=" + dephas;
    }

    static T4X parse(String line1, String line2, List<String> lines3andNext) {
        Objects.requireNonNull(line1, "line 1 must not be null");
        if (!line1.startsWith("45") && !line1.startsWith("48")) {
            throw new IllegalArgumentException("line 1 is not a valid 45 or 48 record: '" + line1 + "'");
        }
        Objects.requireNonNull(line2, "line 2 must not be null");
        if (!line2.startsWith("45") && !line2.startsWith("48")) {
            throw new IllegalArgumentException("line 2 is not a valid 45 or 48 record: '" + line2 + "'");
        }
        for (int i = 0; i < lines3andNext.size(); i++) {
            String line3andNext = lines3andNext.get(i);
            Objects.requireNonNull(line3andNext, "line " + (i + 3) + " must not be null");
            if (!line3andNext.startsWith("45") && !line3andNext.startsWith("48")) {
                throw new IllegalArgumentException("line " + (i + 3) + " is not a valid 45 or 48 record: '" + line3andNext + "'");
            }
        }
        String name1 = RecordUtil.parseString(line1, 2, 10);
        String name2 = RecordUtil.parseString(line1, 11, 19);
        float rate = RecordUtil.parseFloat(line1, 21, 29);
        float pcu = RecordUtil.parseFloat(line1, 29, 37);
        float pfer = RecordUtil.parseFloat(line1, 38, 46);
        float cmagn = RecordUtil.parseFloat(line1, 47, 55);
        float esat = RecordUtil.parseFloat(line1, 56, 64);
        int ktpnom = RecordUtil.parseInt(line2, 21, 25);
        int ktap8 = RecordUtil.parseInt(line2, 26, 30);
        List<Integer> iplo = new ArrayList<>(lines3andNext.size());
        List<Float> uno1 = new ArrayList<>(lines3andNext.size());
        List<Float> uno2 = new ArrayList<>(lines3andNext.size());
        List<Float> ucc = new ArrayList<>(lines3andNext.size());
        List<Float> dephas = new ArrayList<>(lines3andNext.size());
        for (String line3andNext : lines3andNext) {
            iplo.add(RecordUtil.parseInt(line3andNext, 21, 25));
            uno1.add(RecordUtil.parseFloat(line3andNext, 26, 34));
            uno2.add(RecordUtil.parseFloat(line3andNext, 35, 43));
            ucc.add(RecordUtil.parseFloat(line3andNext, 44, 52));
            dephas.add(RecordUtil.parseFloat(line3andNext, 54, 61));
        }
        if (Collections.min(iplo) != 1 || Collections.max(iplo) != iplo.size()) {
            throw new RuntimeException("Tap numbering is exptected to start at 1 and be contiguous");
        }
        return new T4X(name1, name2, rate, pcu, pfer, cmagn, esat, ktpnom, ktap8,
                       iplo, uno1, uno2, ucc, dephas);
    }

}

