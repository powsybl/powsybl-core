/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation.securityindexes;

import com.google.common.collect.ImmutableMap;
import com.powsybl.commons.exceptions.UncheckedJaxbException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Quinary <itesla@quinary.com>
 */
public class SmallSignalSecurityIndex extends AbstractSecurityIndex {

    static final String XML_NAME = "smallsignal";

    private double gmi = Double.NaN;
    private double[] ami;
    private double[][] smi;

    private static final JAXBContext JAXB_CONTEXT;

    static {
        try {
            JAXB_CONTEXT = JAXBContext.newInstance(Index.class);
        } catch (JAXBException e) {
            throw new UncheckedJaxbException(e);
        }
    }

    public static SmallSignalSecurityIndex fromXml(String contingencyId, XMLStreamReader xmlsr) {
        try {
            Unmarshaller u = JAXB_CONTEXT.createUnmarshaller();
            Index index = (Index) u.unmarshal(xmlsr);

            double gmi = Double.NaN;
            double[] ami = null;
            double[][] smi = null;
            for (Matrix m : index.getMatrices()) {
                switch (m.getName()) {
                    case "gmi":
                        double[][] gmiIndexData = m.getValues();
                        if ((gmiIndexData != null) && (gmiIndexData[0].length > 0)) {
                            gmi = gmiIndexData[0][0];
                        }
                        break;
                    case "ami":
                        double[][] amiIndexData = m.getValues();
                        if (amiIndexData != null) {
                            ami = amiIndexData[0];
                        }
                        break;
                    case "smi":
                        double[][] smiIndexData = m.getValues();
                        smi = smiIndexData;
                        break;
                    default:
                        break;
                }
            }
            return new SmallSignalSecurityIndex(contingencyId, gmi, ami, smi);
        } catch (JAXBException e) {
            throw new UncheckedJaxbException(e);
        }
    }


    public SmallSignalSecurityIndex(String contingencyId, double gmi, double[] ami, double[][] smi) {
        super(contingencyId, SecurityIndexType.SMALLSIGNAL);
        this.gmi = gmi;
        this.ami = ami;
        this.smi = smi;
    }

    public SmallSignalSecurityIndex(String contingencyId, double gmi) {
        this(contingencyId, gmi, null, null);
    }

    public double getGmi() {
        return gmi;
    }

    public double[] getAmi() {
        return ami;
    }

    public double[][] getSmi() {
        return smi;
    }

    @Override
    public boolean isOk() {
        return Double.isNaN(gmi) || gmi >= 0.0000;
    }

    @Override
    public Map<String, String> toMap() {
        return ImmutableMap.of("gmi", Double.toString(gmi), "ami", Arrays.toString(ami), "smi", Arrays.deepToString(smi));
    }

    public static void toXml(SmallSignalSecurityIndex index, XMLStreamWriter writer) throws JAXBException {
        JAXBContext jc;
        jc = JAXBContext.newInstance(Index.class);
        SmallSignalSecurityIndex.Index lindex = new Index(index.getId().getSecurityIndexType().getLabel().toLowerCase());
        double[][] aGmi = new double[1][1];
        aGmi[0][0] = index.getGmi();
        SmallSignalSecurityIndex.Matrix lmatGmi = new Matrix("gmi", aGmi);
        double[][] aAmi;
        if (!Double.isNaN(index.getGmi())) {
            aAmi = new double[1][index.getAmi().length];
            aAmi[0] = index.getAmi();
        } else {
            aAmi = new double[0][0];
        }
        SmallSignalSecurityIndex.Matrix lmatAmi = new Matrix("ami", aAmi);
        SmallSignalSecurityIndex.Matrix lmatSmi = new Matrix("smi", index.getSmi());

        lindex.setMatrices(Arrays.asList(lmatGmi, lmatAmi, lmatSmi));

        Marshaller m = jc.createMarshaller();
        m.marshal(lindex, writer);
    }

    @Override
    public void toXml(XMLStreamWriter xmlWriter) throws XMLStreamException {
        try {
            toXml(this, xmlWriter);
        } catch (JAXBException e) {
            throw new UncheckedJaxbException(e);
        }
    }

    /*
     * xml serialization handling, e.g.
     *

    <index name="smallsignal">
    <matrix name="gmi"><m><r>0.0225</r></m></matrix>
    <matrix name="ami"><m><r>0.1227 0.0726 0.0225 </r></m></matrix>
    <matrix name="smi">
     <m>
      <r>0.1227 0.0726 0.0225 </r>
      <r>0.1866 0.1366 0.0864 </r>
     </m>
    </matrix>
    </index>
     */

    @XmlRootElement(name = "index")
    public static class Index {
        private String name;

        private List<Matrix> matrices = new ArrayList<>();

        public Index(String name) {
            this.name = name;
        }

        public Index(String name, List<Matrix> matrices) {
            this.name = name;
            this.matrices = matrices;
        }


        @XmlAttribute
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlElement(name = "matrix")
        public List<Matrix> getMatrices() {
            return matrices;
        }

        public void setMatrices(List<Matrix> matrices) {
            this.matrices = matrices;
        }

        public Index() {
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("[")
                    .append(getName()).append(":");
            for (Matrix m : getMatrices()) {
                builder.append(m.toString());
            }
            builder.append("]");
            return builder.toString();
        }
    }


    public static class Matrix {
        private String name;
        private double[][] values;

        public Matrix(String name, double[][] values) {
            this.name = name;
            this.values = values;
        }

        @XmlAttribute
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("[")
                    .append(getName()).append(": [");
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    builder.append("[");
                    for (int j = 0; j < values[0].length; j++) {
                        builder.append(values[i][j]).append(" ");
                    }
                    builder.append("]");
                }
            }
            builder.append("]]");
            return builder.toString();
        }


        @XmlElement(name = "m")
        @XmlJavaTypeAdapter(MatrixAdapter.class)
        public double[][] getValues() {
            return values;
        }

        public void setValues(double[][] values) {
            this.values = values;
        }

        public Matrix() {
        }

    }

    //http://stackoverflow.com/questions/17119708/csv-as-text-node-of-an-xml-element
    public static class MatrixAdapter extends XmlAdapter<MatrixAdapter.AdaptedMatrix, double[][]> {

        public static class AdaptedMatrix {
            @XmlElement(name = "r")
            public List<AdaptedRow> rows;
        }

        public static class AdaptedRow {

            @XmlValue
            public double[] row;
        }

        @Override
        public AdaptedMatrix marshal(double[][] matrix) throws Exception {
            AdaptedMatrix adaptedMatrix = new AdaptedMatrix();
            if (matrix != null) {
                adaptedMatrix.rows = new ArrayList<>(matrix.length);
                for (double[] row : matrix) {
                    if (matrix[0].length == 1 && Double.isNaN(matrix[0][0])) {
                        break;
                    }
                    AdaptedRow adaptedRow = new AdaptedRow();
                    adaptedRow.row = row;
                    adaptedMatrix.rows.add(adaptedRow);
                }
            }
            return adaptedMatrix;
        }

        @Override
        public double[][] unmarshal(AdaptedMatrix adaptedMatrix) throws Exception {
            List<AdaptedRow> adaptedRows = adaptedMatrix.rows;
            double[][] matrix = new double[adaptedRows.size()][];
            for (int x = 0; x < adaptedRows.size(); x++) {
                matrix[x] = adaptedRows.get(x).row;
            }
            return matrix;
        }

    }
}
