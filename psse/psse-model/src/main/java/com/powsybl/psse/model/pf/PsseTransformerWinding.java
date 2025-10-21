/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.PsseVersioned;
import com.powsybl.psse.model.Revision;
import de.siegmar.fastcsv.reader.CsvRecord;

import java.util.Optional;

import static com.powsybl.psse.model.io.Util.parseDoubleFromRecord;
import static com.powsybl.psse.model.io.Util.parseIntFromRecord;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
public class PsseTransformerWinding extends PsseVersioned {

    private double windv = Double.NaN;
    private double nomv = 0;
    private double ang = 0;
    private int cod = 0;
    private int cont = 0;

    @Revision(since = 35)
    private int node = 0;

    private double rma = Double.NaN;
    private double rmi = Double.NaN;
    private double vma = Double.NaN;
    private double vmi = Double.NaN;
    private int ntp = 33;
    private int tab = 0;
    private double cr = 0;
    private double cx = 0;
    private double cnxa = 0;

    public static PsseTransformerWinding fromRecord(CsvRecord rec, PsseVersion version, String[] headers) {
        return fromRecord(rec, version, headers, "");
    }

    public static PsseTransformerWinding fromRecord(CsvRecord rec, PsseVersion version, String[] headers, String headerSuffix) {
        PsseTransformerWinding psseTransformerWinding = new PsseTransformerWinding();
        psseTransformerWinding.setWindv(parseDoubleFromRecord(rec, Double.NaN, headers, "windv" + headerSuffix));
        psseTransformerWinding.setNomv(parseDoubleFromRecord(rec, 0.0, headers, "nomv" + headerSuffix));
        psseTransformerWinding.setAng(parseDoubleFromRecord(rec, 0.0, headers, "ang" + headerSuffix));
        psseTransformerWinding.setCod(parseIntFromRecord(rec, 0, headers, "cod" + headerSuffix));
        psseTransformerWinding.setCont(parseIntFromRecord(rec, 0, headers, "cont" + headerSuffix));
        if (version.getMajorNumber() >= 35) {
            psseTransformerWinding.setNode(parseIntFromRecord(rec, 0, headers, "node" + headerSuffix));
        }
        psseTransformerWinding.setRma(parseDoubleFromRecord(rec, Double.NaN, headers, "rma" + headerSuffix));
        psseTransformerWinding.setRmi(parseDoubleFromRecord(rec, Double.NaN, headers, "rmi" + headerSuffix));
        psseTransformerWinding.setVma(parseDoubleFromRecord(rec, Double.NaN, headers, "vma" + headerSuffix));
        psseTransformerWinding.setVmi(parseDoubleFromRecord(rec, Double.NaN, headers, "vmi" + headerSuffix));
        psseTransformerWinding.setNtp(parseIntFromRecord(rec, 33, headers, "ntp" + headerSuffix));
        psseTransformerWinding.setTab(parseIntFromRecord(rec, 0, headers, "tab" + headerSuffix));
        psseTransformerWinding.setCr(parseDoubleFromRecord(rec, 0.0, headers, "cr" + headerSuffix));
        psseTransformerWinding.setCx(parseDoubleFromRecord(rec, 0.0, headers, "cx" + headerSuffix));
        psseTransformerWinding.setCnxa(parseDoubleFromRecord(rec, 0.0, headers, "cnxa" + headerSuffix));
        return psseTransformerWinding;
    }

    public static String[] toRecord(PsseTransformerWinding psseTransformerWinding, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            Optional<String> optionalValue = psseTransformerWinding.headerToString(headers[i]);
            if (optionalValue.isEmpty()) {
                throw new PsseException("Unsupported header: " + headers[i]);
            }
            row[i] = optionalValue.get();
        }
        return row;
    }

    public Optional<String> headerToString(String header) {
        return switch (header) {
            case "windv" -> Optional.of(String.valueOf(getWindv()));
            case "nomv" -> Optional.of(String.valueOf(getNomv()));
            case "ang" -> Optional.of(String.valueOf(getAng()));
            case "cod" -> Optional.of(String.valueOf(getCod()));
            case "cont" -> Optional.of(String.valueOf(getCont()));
            case "node" -> Optional.of(String.valueOf(getNode()));
            case "rma" -> Optional.of(String.valueOf(getRma()));
            case "rmi" -> Optional.of(String.valueOf(getRmi()));
            case "vma" -> Optional.of(String.valueOf(getVma()));
            case "vmi" -> Optional.of(String.valueOf(getVmi()));
            case "ntp" -> Optional.of(String.valueOf(getNtp()));
            case "tab" -> Optional.of(String.valueOf(getTab()));
            case "cr" -> Optional.of(String.valueOf(getCr()));
            case "cx" -> Optional.of(String.valueOf(getCx()));
            case "cnxa" -> Optional.of(String.valueOf(getCnxa()));
            default -> Optional.empty();
        };
    }

    public double getWindv() {
        return windv;
    }

    public double getNomv() {
        return nomv;
    }

    public double getAng() {
        return ang;
    }

    public int getCod() {
        return cod;
    }

    public int getCont() {
        return cont;
    }

    public int getNode() {
        checkVersion("node");
        return node;
    }

    public double getRma() {
        return rma;
    }

    public double getRmi() {
        return rmi;
    }

    public double getVma() {
        return vma;
    }

    public double getVmi() {
        return vmi;
    }

    public int getNtp() {
        return ntp;
    }

    public int getTab() {
        return tab;
    }

    public double getCr() {
        return cr;
    }

    public double getCx() {
        return cx;
    }

    public double getCnxa() {
        return cnxa;
    }

    public void setWindv(double windv) {
        this.windv = windv;
    }

    public void setNomv(double nomv) {
        this.nomv = nomv;
    }

    public void setAng(double ang) {
        this.ang = ang;
    }

    public void setCod(int cod) {
        this.cod = cod;
    }

    public void setCont(int cont) {
        this.cont = cont;
    }

    public void setNode(int node) {
        this.node = node;
    }

    public void setRma(double rma) {
        this.rma = rma;
    }

    public void setRmi(double rmi) {
        this.rmi = rmi;
    }

    public void setVma(double vma) {
        this.vma = vma;
    }

    public void setVmi(double vmi) {
        this.vmi = vmi;
    }

    public void setNtp(int ntp) {
        this.ntp = ntp;
    }

    public void setTab(int tab) {
        this.tab = tab;
    }

    public void setCr(double cr) {
        this.cr = cr;
    }

    public void setCx(double cx) {
        this.cx = cx;
    }

    public void setCnxa(double cnxa) {
        this.cnxa = cnxa;
    }

    public PsseTransformerWinding copy() {
        PsseTransformerWinding copy = new PsseTransformerWinding();
        copy.windv = this.windv;
        copy.nomv = this.nomv;
        copy.ang = this.ang;
        copy.cod = this.cod;
        copy.cont = this.cont;
        copy.node = this.node;
        copy.rma = this.rma;
        copy.rmi = this.rmi;
        copy.vma = this.vma;
        copy.vmi = this.vmi;
        copy.ntp = this.ntp;
        copy.tab = this.tab;
        copy.cr = this.cr;
        copy.cx = this.cx;
        copy.cnxa = this.cnxa;
        return copy;
    }
}
