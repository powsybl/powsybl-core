/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model.pf;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

import java.io.IOException;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PsseCaseIdentification {

    private int ic = 0;
    private double sbase = 100;

    // Similar way writing revision for Json,
    // but relying on PsseVersion class
    @JsonSerialize(using = RevisionSerializer.class)
    private float rev = 33;

    private double xfrrat = Double.NaN;
    private double nxfrat = Double.NaN;
    private double basfrq = Double.NaN;
    private String title1;
    private String title2;

    public static PsseCaseIdentification fromRecord(NamedCsvRecord rec, PsseVersion version) {
        PsseCaseIdentification psseCaseIdentification = new PsseCaseIdentification();
        psseCaseIdentification.setIc(Integer.parseInt(rec.getField("ic")));
        psseCaseIdentification.setSbase(Double.parseDouble(rec.getField("sbase")));
        psseCaseIdentification.setRev(Float.parseFloat(rec.getField("rev")));
        psseCaseIdentification.setXfrrat(Double.parseDouble(rec.getField("xfrrat")));
        psseCaseIdentification.setNxfrat(Double.parseDouble(rec.getField("nxfrat")));
        psseCaseIdentification.setBasfrq(Double.parseDouble(rec.getField("basfrq")));
        psseCaseIdentification.setTitle1(rec.getField("title1"));
        psseCaseIdentification.setTitle2(rec.getField("title2"));
        return psseCaseIdentification;
    }

    public static String[] toRecord(PsseCaseIdentification psseCaseIdentification, String[] headers) {
        String[] row = new String[headers.length];
        for (int i = 0; i < headers.length; i++) {
            row[i] = switch (headers[i]) {
                case "ic" -> String.valueOf(psseCaseIdentification.getIc());
                case "sbase" -> String.valueOf(psseCaseIdentification.getSbase());
                case "rev" -> PsseVersion.fromRevision(psseCaseIdentification.getRev()).toString();
                case "xfrrat" -> String.format("%.0f", psseCaseIdentification.getXfrrat());
                case "nxfrat" -> String.format("%.0f", psseCaseIdentification.getNxfrat());
                case "basfrq" -> String.valueOf(psseCaseIdentification.getBasfrq());
                case "title1" -> psseCaseIdentification.getTitle1();
                case "title2" -> psseCaseIdentification.getTitle2();
                default -> throw new PsseException("Unsupported header: " + headers[i]);
            };
        }
        return row;
    }

    public int getIc() {
        return ic;
    }

    public void setIc(int ic) {
        this.ic = ic;
    }

    public double getSbase() {
        return sbase;
    }

    public void setSbase(double sbase) {
        this.sbase = sbase;
    }

    public float getRev() {
        return rev;
    }

    public void setRev(float rev) {
        this.rev = rev;
    }

    public double getXfrrat() {
        return xfrrat;
    }

    public void setXfrrat(double xfrrat) {
        this.xfrrat = xfrrat;
    }

    public double getNxfrat() {
        return nxfrat;
    }

    public void setNxfrat(double nxfrat) {
        this.nxfrat = nxfrat;
    }

    public double getBasfrq() {
        return basfrq;
    }

    public void setBasfrq(double basfrq) {
        this.basfrq = basfrq;
    }

    public String getTitle1() {
        return title1;
    }

    public void setTitle1(String title1) {
        this.title1 = title1;
    }

    public String getTitle2() {
        return title2;
    }

    public void setTitle2(String title2) {
        this.title2 = title2;
    }

    public void validate() {
        if (ic == 1) {
            throw new PsseException("Incremental load of data option (IC = 1) is not supported");
        }
        PsseVersion v = PsseVersion.fromRevision(rev);
        if (!v.isSupported()) {
            throw new PsseException(String.format("Version %s not supported. Supported versions are: %s", v, PsseVersion.supportedVersions()));
        }
    }

    private static final class RevisionSerializer extends JsonSerializer<Float> {
        @Override
        public void serialize(Float value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            String rev = PsseVersion.fromRevision(value).toString();
            generator.writeRawValue(rev);
        }
    }
}
