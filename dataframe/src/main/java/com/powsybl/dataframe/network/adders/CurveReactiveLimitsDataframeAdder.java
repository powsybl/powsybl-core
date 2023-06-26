/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.DoubleSeries;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.*;

import java.util.*;

import static com.powsybl.dataframe.network.adders.NetworkUtils.getIdentifiableOrThrow;
import static com.powsybl.dataframe.network.adders.SeriesUtils.getRequiredDoubles;
import static com.powsybl.dataframe.network.adders.SeriesUtils.getRequiredStrings;

/**
 * @author Massimo Ferraro <massimo.ferraro@soft.it>
 */
public class CurveReactiveLimitsDataframeAdder implements NetworkElementAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.doubles("p"),
        SeriesMetadata.doubles("min_q"),
        SeriesMetadata.doubles("max_q")
    );

    private static final class CurveReactiveLimitsSeries {

        private final StringSeries elementIds;
        private final DoubleSeries ps;
        private final DoubleSeries minQs;
        private final DoubleSeries maxQs;

        CurveReactiveLimitsSeries(UpdatingDataframe dataframe) {
            this.elementIds = getRequiredStrings(dataframe, "id");
            this.ps = getRequiredDoubles(dataframe, "p");
            this.minQs = getRequiredDoubles(dataframe, "min_q");
            this.maxQs = getRequiredDoubles(dataframe, "max_q");
        }

        public StringSeries getElementIds() {
            return elementIds;
        }

        public DoubleSeries getPs() {
            return ps;
        }

        public DoubleSeries getMinQs() {
            return minQs;
        }

        public DoubleSeries getMaxQs() {
            return maxQs;
        }
    }

    private static final class CurvePoint {

        private final double p;
        private final double minQ;
        private final double maxQ;

        CurvePoint(double p, double minQ, double maxQ) {
            this.p = p;
            this.minQ = minQ;
            this.maxQ = maxQ;
        }

        public double getP() {
            return p;
        }

        public double getMinQ() {
            return minQ;
        }

        public double getMaxQ() {
            return maxQ;
        }

    }

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    @Override
    public void addElements(Network network, List<UpdatingDataframe> dataframes) {
        UpdatingDataframe primaryTable = dataframes.get(0);
        CurveReactiveLimitsSeries series = new CurveReactiveLimitsSeries(primaryTable);
        Map<String, List<CurvePoint>> curvesPoints = new HashMap<>();
        for (int i = 0; i < primaryTable.getRowCount(); i++) {
            String elementId = series.getElementIds().get(i);
            double p = series.getPs().get(i);
            double minQ = series.getMinQs().get(i);
            double maxQ = series.getMaxQs().get(i);
            CurvePoint curvePoint = new CurvePoint(p, minQ, maxQ);
            curvesPoints.computeIfAbsent(elementId, id -> new ArrayList<>()).add(curvePoint);
        }
        curvesPoints.forEach((elementId, curvePoints) -> createLimits(network, elementId, curvePoints));
    }

    private static void createLimits(Network network, String elementId, List<CurvePoint> curvePoints) {
        Identifiable identifiable = getIdentifiableOrThrow(network, elementId);
        if (identifiable instanceof ReactiveLimitsHolder) {
            ReactiveCapabilityCurveAdder curveAdder = ((ReactiveLimitsHolder) identifiable)
                .newReactiveCapabilityCurve();
            for (CurvePoint curvePoint : curvePoints) {
                curveAdder.beginPoint()
                    .setP(curvePoint.getP())
                    .setMaxQ(curvePoint.getMaxQ())
                    .setMinQ(curvePoint.getMinQ())
                    .endPoint();
            }
            curveAdder.add();
        } else {
            throw new PowsyblException("Element " + elementId + " does not have reactive limits.");
        }
    }
}
