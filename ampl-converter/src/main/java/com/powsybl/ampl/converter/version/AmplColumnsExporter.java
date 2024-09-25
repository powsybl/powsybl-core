/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter.version;

import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.iidm.network.*;

import java.io.IOException;
import java.util.List;

/**
 * @author Nicolas Pierre {@literal <nicolas.pierre at artelys.com>}
 */
public interface AmplColumnsExporter {

    List<Column> getRtcColumns();

    List<Column> getPtcColumns();

    List<Column> getCurrentLimitsColumns();

    List<Column> getHvdcLinesColumns();

    List<Column> getLccConverterStationsColumns();

    List<Column> getVscConverterStationsColumns();

    List<Column> getSubstationsColumns();

    List<Column> getLoadsColumns();

    List<Column> getShuntsColumns();

    List<Column> getStaticVarCompensatorColumns();

    List<Column> getGeneratorsColumns();

    List<Column> getBatteriesColumns();

    List<Column> getBusesColumns();

    List<Column> getBranchesColumns();

    List<Column> getTapChangerTableColumns();

    void writeRtcToFormatter(TableFormatter formatter) throws IOException;

    void writePtcToFormatter(TableFormatter formatter) throws IOException;

    void writeTwoWindingsTransformerTapChangerTableToFormatter(TableFormatter formatter,
                                                               TwoWindingsTransformer twt) throws IOException;

    void writeThreeWindingsTransformerTapChangerTableToFormatter(TableFormatter formatter,
                                                                 ThreeWindingsTransformer twt) throws IOException;

    void writeCurrentLimits(TableFormatter formatter) throws IOException;

    void writeHvdcToFormatter(TableFormatter formatter, HvdcLine hvdcLine) throws IOException;

    void writeLccConverterStationToFormatter(TableFormatter formatter,
                                             LccConverterStation lccStation) throws IOException;

    void writeVscConverterStationToFormatter(TableFormatter formatter,
                                             VscConverterStation vscStation) throws IOException;

    void writeBusesColumnsToFormatter(TableFormatter formatter, Bus b) throws IOException;

    void writeThreeWindingsTranformersMiddleBusesColumnsToFormatter(TableFormatter formatter,
                                                                    ThreeWindingsTransformer twt,
                                                                    int middleCcNum) throws IOException;

    void writeLinesToFormatter(TableFormatter formatter, Line l) throws IOException;

    void writeDanglingLineMiddleBusesToFormatter(TableFormatter formatter, DanglingLine dl,
                                                 int middleCcNum) throws IOException;

    void writeTieLineMiddleBusesToFormatter(TableFormatter formatter, TieLine tieLine,
                                            int xNodeCcNum) throws IOException;

    void writeTieLineToFormatter(TableFormatter formatter, TieLine tieLine) throws IOException;

    void writeDanglingLineToFormatter(TableFormatter formatter, DanglingLine dl) throws IOException;

    void writeTwoWindingsTranformerToFormatter(TableFormatter formatter, TwoWindingsTransformer twt) throws IOException;

    void writeThreeWindingsTransformerLegToFormatter(TableFormatter formatter, ThreeWindingsTransformer twt,
                                                     int middleBusNum, int middleVlNum,
                                                     ThreeSides side) throws IOException;

    void writeTieLineVoltageLevelToFormatter(TableFormatter formatter, TieLine tieLine) throws IOException;

    void writeDanglingLineVoltageLevelToFormatter(TableFormatter formatter, DanglingLine dl) throws IOException;

    void writeThreeWindingsTransformerVoltageLevelToFormatter(TableFormatter formatter,
                                                              ThreeWindingsTransformer twt) throws IOException;

    void writeVoltageLevelToFormatter(TableFormatter formatter, VoltageLevel vl) throws IOException;

    void writeDanglingLineLoadToFormatter(TableFormatter formatter, DanglingLine dl) throws IOException;

    void writeLoadtoFormatter(TableFormatter formatter, Load l) throws IOException;

    void writeShuntCompensatorToFormatter(TableFormatter formatter, ShuntCompensator sc) throws IOException;

    void writeBatteryToFormatter(TableFormatter formatter, Battery battery) throws IOException;

    void writeStaticVarCompensatorToFormatter(TableFormatter formatter, StaticVarCompensator svc) throws IOException;

    void writeGeneratorToFormatter(TableFormatter formatter, Generator g) throws IOException;

}
