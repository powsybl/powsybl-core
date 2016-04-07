/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EsgDetailedTwoWindingTransformer {

    public enum RegulatingMode {
        NOT_REGULATING,
        VOLTAGE,
        ACTIVE_FLUX_SIDE_1,
        ACTIVE_FLUX_SIDE_2
    }

    public static class Tap {
        private final int iplo; // tap number
        private final float uno1; // sending side voltage [kV]
        private final float uno2; // receiving side voltage [kV]
        private final float ucc; // leakage impedance [%]
        private final float dephas; // phase shift angle [deg]

        public Tap(int iplo, float dephas, float uno1, float uno2, float ucc) {
            this.iplo = iplo;
            this.dephas = dephas;
            this.uno1 = uno1;
            this.uno2 = uno2;
            this.ucc = ucc;
        }

        public float getDephas() {
            return dephas;
        }

        public int getIplo() {
            return iplo;
        }

        public float getUcc() {
            return ucc;
        }

        public float getUno1() {
            return uno1;
        }

        public float getUno2() {
            return uno2;
        }
    }

    private final EsgBranchName name;
    private final EsgBranchConnectionStatus status;
    private final float rate; // rated apparent power [MVA]
    private final float pcu; // Cu losses [% base RATE]
    private final float pfer; // Iron losses [% base RATE]
    private final float cmagn; // magnetizing current [%]
    private final float esat; // saturation exponent

    private final int ktpnom; // nominal tap number
    private final int ktap8; // initial tap position (tap number)
    private final Esg8charName zbusr; // regulated node name (if empty, no tap change)
    private float voltr; // voltage target [kV]
    private final float pregmin; // min active flux [MW]
    private final float pregmax; //  max active flux [MW]
    private final RegulatingMode xregtr; // regulating mode

    private final List<Tap> taps = new ArrayList<>(1);

    public EsgDetailedTwoWindingTransformer(EsgBranchName name, EsgBranchConnectionStatus status, float cmagn,
                                            float rate, float pcu, float pfer, float esat, int ktpnom, int ktap8, Esg8charName zbusr,
                                            float voltr, float pregmin, float pregmax, RegulatingMode xregtr) {
        this.name = Objects.requireNonNull(name);
        this.status = status;
        this.cmagn = cmagn;
        this.rate = rate;
        this.pcu = pcu;
        this.pfer = pfer;
        this.esat = esat;
        this.ktpnom = ktpnom;
        this.ktap8 = ktap8;
        this.zbusr = zbusr;
        this.voltr = voltr;
        this.pregmin = pregmin;
        this.pregmax = pregmax;
        this.xregtr = Objects.requireNonNull(xregtr);
    }

    public EsgBranchName getName() {
        return name;
    }

    public EsgBranchConnectionStatus getStatus() {
        return status;
    }

    public float getCmagn() {
        return cmagn;
    }

    public float getEsat() {
        return esat;
    }

    public int getKtap8() {
        return ktap8;
    }

    public int getKtpnom() {
        return ktpnom;
    }

    public float getPcu() {
        return pcu;
    }

    public float getPfer() {
        return pfer;
    }

    public float getPregmax() {
        return pregmax;
    }

    public float getPregmin() {
        return pregmin;
    }

    public float getRate() {
        return rate;
    }

    public List<Tap> getTaps() {
        return taps;
    }

    public float getVoltr() {
        return voltr;
    }

    public void setVoltr(float voltr) {
        this.voltr = voltr;
    }

    public RegulatingMode getXregtr() {
        return xregtr;
    }

    public Esg8charName getZbusr() {
        return zbusr;
    }
}
