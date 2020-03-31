/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.comparator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.powsybl.iidm.network.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkStateComparator {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkStateComparator.class);

    private interface ColumnMapper<T extends Identifiable> {

        String getTitle();

        void setValue(T obj, Cell cell);

    }

    private static final ColumnMapper<Bus> BUS_V = new ColumnMapper<Bus>() {

        @Override
        public String getTitle() {
            return "v (KV)";
        }

        @Override
        public void setValue(Bus bus, Cell cell) {
            if (!Double.isNaN(bus.getV())) {
                cell.setCellValue(bus.getV());
            }
        }
    };

    private static final ColumnMapper<Bus> BUS_ANGLE = new ColumnMapper<Bus>() {

        @Override
        public String getTitle() {
            return "\u03B8 (°)";
        }

        @Override
        public void setValue(Bus bus, Cell cell) {
            if (!Double.isNaN(bus.getAngle())) {
                cell.setCellValue(bus.getAngle());
            }
        }
    };

    private static class BranchP1ColumnMapper<T extends Branch> implements ColumnMapper<T> {

        @Override
        public String getTitle() {
            return "p1 (MW)";
        }

        @Override
        public void setValue(Branch branch, Cell cell) {
            if (!Double.isNaN(branch.getTerminal1().getP())) {
                cell.setCellValue(branch.getTerminal1().getP());
            }
        }
    }

    private static class BranchQ1ColumnMapper<T extends Branch> implements ColumnMapper<T> {

        @Override
        public String getTitle() {
            return "q1 (MW)";
        }

        @Override
        public void setValue(Branch branch, Cell cell) {
            if (!Double.isNaN(branch.getTerminal1().getQ())) {
                cell.setCellValue(branch.getTerminal1().getQ());
            }
        }
    }

    private static class BranchP2ColumnMapper<T extends Branch> implements ColumnMapper<T> {

        @Override
        public String getTitle() {
            return "p2 (MW)";
        }

        @Override
        public void setValue(Branch branch, Cell cell) {
            if (!Double.isNaN(branch.getTerminal2().getP())) {
                cell.setCellValue(branch.getTerminal2().getP());
            }
        }
    }

    private static class BranchQ2ColumnMapper<T extends Branch> implements ColumnMapper<T> {

        @Override
        public String getTitle() {
            return "q2 (MW)";
        }

        @Override
        public void setValue(Branch branch, Cell cell) {
            if (!Double.isNaN(branch.getTerminal2().getQ())) {
                cell.setCellValue(branch.getTerminal2().getQ());
            }
        }
    }

    private static class BranchRatioColumnMapper<T extends Branch> implements ColumnMapper<T> {

        @Override
        public String getTitle() {
            return "v1/v2 (pu)";
        }

        @Override
        public void setValue(Branch branch, Cell cell) {
            if (branch instanceof TwoWindingsTransformer
                    && (((TwoWindingsTransformer) branch).getRatioTapChanger() != null ||
                        ((TwoWindingsTransformer) branch).getPhaseTapChanger() != null)) {
                Bus b1 = branch.getTerminal1().getBusView().getBus();
                Bus b2 = branch.getTerminal2().getBusView().getBus();
                if (b1 != null && !Double.isNaN(b1.getV()) && b2 != null && !Double.isNaN(b2.getV()) && b2.getV() != 0) {
                    cell.setCellValue(b1.getV() / b2.getV());
                }
            }
        }
    }

    private static class BranchDephaColumnMapper<T extends Branch> implements ColumnMapper<T> {

        @Override
        public String getTitle() {
            return "depha (°)";
        }

        @Override
        public void setValue(Branch branch, Cell cell) {
            if (branch instanceof TwoWindingsTransformer
                    && (((TwoWindingsTransformer) branch).getRatioTapChanger() != null ||
                    ((TwoWindingsTransformer) branch).getPhaseTapChanger() != null)) {
                Bus b1 = branch.getTerminal1().getBusView().getBus();
                Bus b2 = branch.getTerminal2().getBusView().getBus();
                if (b1 != null && !Double.isNaN(b1.getAngle()) && b2 != null && !Double.isNaN(b2.getAngle())) {
                    cell.setCellValue(b1.getAngle() - b2.getAngle());
                }
            }
        }
    }

    private static final BranchP1ColumnMapper<Line> LINE_P1 = new BranchP1ColumnMapper<>();

    private static final BranchQ1ColumnMapper<Line> LINE_Q1 = new BranchQ1ColumnMapper<>();

    private static final BranchP2ColumnMapper<Line> LINE_P2 = new BranchP2ColumnMapper<>();

    private static final BranchQ2ColumnMapper<Line> LINE_Q2 = new BranchQ2ColumnMapper<>();

    private static final BranchP1ColumnMapper<TwoWindingsTransformer> TWT_P1 = new BranchP1ColumnMapper<>();

    private static final BranchQ1ColumnMapper<TwoWindingsTransformer> TWT_Q1 = new BranchQ1ColumnMapper<>();

    private static final BranchP2ColumnMapper<TwoWindingsTransformer> TWT_P2 = new BranchP2ColumnMapper<>();

    private static final BranchQ2ColumnMapper<TwoWindingsTransformer> TWT_Q2 = new BranchQ2ColumnMapper<>();

    private static final BranchRatioColumnMapper<TwoWindingsTransformer> TWT_RATIO = new BranchRatioColumnMapper<>();

    private static final BranchDephaColumnMapper<TwoWindingsTransformer> TWT_DEPHA = new BranchDephaColumnMapper<>();

    private static final ColumnMapper<TwoWindingsTransformer> TWT_RATIO_TAP = new ColumnMapper<TwoWindingsTransformer>() {

        @Override
        public String getTitle() {
            return "ratio tap";
        }

        @Override
        public void setValue(TwoWindingsTransformer twt, Cell cell) {
            RatioTapChanger rtc = twt.getRatioTapChanger();
            if (rtc != null) {
                cell.setCellValue(rtc.getTapPosition());
            }
        }
    };

    private static final ColumnMapper<TwoWindingsTransformer> TWT_PHASE_TAP = new ColumnMapper<TwoWindingsTransformer>() {

        @Override
        public String getTitle() {
            return "phase tap";
        }

        @Override
        public void setValue(TwoWindingsTransformer twt, Cell cell) {
            PhaseTapChanger ptc = twt.getPhaseTapChanger();
            if (ptc != null) {
                cell.setCellValue(ptc.getTapPosition());
            }
        }
    };

    private static class InjectionPColumnMapper<T extends Injection> implements ColumnMapper<T> {

        @Override
        public String getTitle() {
            return "p (MW)";
        }

        @Override
        public void setValue(Injection inj, Cell cell) {
            if (!Double.isNaN(inj.getTerminal().getP())) {
                cell.setCellValue(inj.getTerminal().getP());
            }
        }
    }

    private static class InjectionQColumnMapper<T extends Injection> implements ColumnMapper<T> {

        @Override
        public String getTitle() {
            return "q (MW)";
        }

        @Override
        public void setValue(Injection inj, Cell cell) {
            if (!Double.isNaN(inj.getTerminal().getQ())) {
                cell.setCellValue(inj.getTerminal().getQ());
            }
        }
    }

    private static class InjectionVColumnMapper<T extends Injection> implements ColumnMapper<T> {

        @Override
        public String getTitle() {
            return "v (KV)";
        }

        @Override
        public void setValue(Injection inj, Cell cell) {
            Bus b = inj.getTerminal().getBusView().getBus();
            if (b != null && !Double.isNaN(b.getV())) {
                cell.setCellValue(b.getV());
            }
        }
    }

    private static final InjectionPColumnMapper<Generator> GEN_P = new InjectionPColumnMapper<>();

    private static final InjectionQColumnMapper<Generator> GEN_Q = new InjectionQColumnMapper<>();

    private static final InjectionVColumnMapper<Generator> GEN_V = new InjectionVColumnMapper<>();

    private static final InjectionPColumnMapper<HvdcConverterStation> HVDC_P = new InjectionPColumnMapper<>();

    private static final InjectionQColumnMapper<HvdcConverterStation> HVDC_Q = new InjectionQColumnMapper<>();

    private static final InjectionVColumnMapper<HvdcConverterStation> HVDC_V = new InjectionVColumnMapper<>();

    private static final InjectionPColumnMapper<Load> LOAD_P = new InjectionPColumnMapper<>();

    private static final InjectionQColumnMapper<Load> LOAD_Q = new InjectionQColumnMapper<>();

    private static final ColumnMapper<ShuntCompensator> SHUNT_SECTIONS = new ColumnMapper<ShuntCompensator>() {

        @Override
        public String getTitle() {
            return "shunt sections";
        }

        @Override
        public void setValue(ShuntCompensator sc, Cell cell) {
            cell.setCellValue(sc.getCurrentSectionCount());
        }
    };

    private static final InjectionQColumnMapper<ShuntCompensator> SHUNT_Q = new InjectionQColumnMapper<>();

    private static final class DiffColumnMapper<T extends Identifiable> implements ColumnMapper<T> {

        private final String title;

        private final int column1;

        private final int column2;

        private DiffColumnMapper(String title, int column1, int column2) {
            this.title = title;
            this.column1 = column1;
            this.column2 = column2;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public void setValue(T obj, Cell cell) {
            int row = cell.getRowIndex() + 1;
            String ax = (char) ('A' + column1) + Integer.toString(row);
            String ay = (char) ('A' + column2) + Integer.toString(row);
            cell.setCellFormula("IF(OR(ISBLANK(" + ax + "), ISBLANK(" + ay + ")), \"\",  ABS(" + ax + "-" + ay + "))");
        }

    }

    private static class SheetContext<T extends Identifiable> {

        private final List<T> objs;

        private final Sheet sheet;

        private final Row rowHeader1;

        private final Row rowHeader2;

        private final List<Row> rows = new ArrayList<>();

        SheetContext(List<T> objs, Workbook wb, String sheetTitle) {
            this.objs = Objects.requireNonNull(objs);

            sheet = wb.createSheet(sheetTitle);

            rowHeader1 = sheet.createRow(0);
            rowHeader2 = sheet.createRow(1);

            for (int i = 0; i < objs.size(); i++) {
                rows.add(sheet.createRow(i + 2));
            }
        }

        List<T> getObjs() {
            return objs;
        }

        Sheet getSheet() {
            return sheet;
        }

        Row getRowHeader1() {
            return rowHeader1;
        }

        Row getRowHeader2() {
            return rowHeader2;
        }

        List<Row> getRows() {
            return rows;
        }
    }

    private static final List<ColumnMapper<Bus>> BUS_MAPPERS = ImmutableList.of(BUS_V, BUS_ANGLE);

    private static final List<ColumnMapper<Line>> LINE_MAPPERS = ImmutableList.of(LINE_P1, LINE_P2, LINE_Q1, LINE_Q2);

    private static final List<ColumnMapper<TwoWindingsTransformer>> TRANSFO_MAPPERS = ImmutableList.of(TWT_P1, TWT_P2, TWT_Q1, TWT_Q2, TWT_RATIO_TAP, TWT_PHASE_TAP, TWT_RATIO, TWT_DEPHA);

    private static final List<ColumnMapper<Generator>> GENERATOR_MAPPERS = ImmutableList.of(GEN_P, GEN_Q, GEN_V);

    private static final List<ColumnMapper<HvdcConverterStation>> HVDC_MAPPERS = ImmutableList.of(HVDC_P, HVDC_Q, HVDC_V);

    private static final List<ColumnMapper<Load>> LOAD_MAPPERS = ImmutableList.of(LOAD_P, LOAD_Q);

    private static final List<ColumnMapper<ShuntCompensator>> SHUNT_MAPPERS = ImmutableList.of(SHUNT_SECTIONS, SHUNT_Q);

    private final Network network;

    private final String otherState;

    public NetworkStateComparator(Network network, String otherState) {
        this.network = Objects.requireNonNull(network);
        this.otherState = Objects.requireNonNull(otherState);
    }

    private static Cell createTitleCell(Row row, int i, CellStyle titleCellStyle) {
        Cell c = row.createCell(i);
        c.setCellStyle(titleCellStyle);
        return c;
    }

    private static <T extends Identifiable> void createColumnHeader(SheetContext<T> sheetContext, CellStyle titleCellStyle) {
        createTitleCell(sheetContext.getRowHeader1(), 0, titleCellStyle);
        createTitleCell(sheetContext.getRowHeader2(), 0, titleCellStyle).setCellValue("id");
        createTitleCell(sheetContext.getRowHeader1(), 1, titleCellStyle);
        createTitleCell(sheetContext.getRowHeader2(), 1, titleCellStyle).setCellValue("name");
        for (int i = 0; i < sheetContext.getObjs().size(); i++) {
            sheetContext.getRows().get(i).createCell(0).setCellValue(sheetContext.getObjs().get(i).getId());
            sheetContext.getRows().get(i).createCell(1).setCellValue(sheetContext.getObjs().get(i).getNameOrId());
        }
    }

    private static <T extends Identifiable> void createColumns(SheetContext<T> sheetContext, CellStyle titleCellStyle,
                                                               int columnOffset, String columnsTitle, List<ColumnMapper<T>> mappers) {
        createTitleCell(sheetContext.getRowHeader1(), columnOffset, titleCellStyle).setCellValue(columnsTitle);
        for (int i = 0; i < mappers.size(); i++) {
            createTitleCell(sheetContext.getRowHeader1(), columnOffset + 1 + i, titleCellStyle);
        }
        for (int i = 0; i < mappers.size(); i++) {
            createTitleCell(sheetContext.getRowHeader2(), columnOffset + i, titleCellStyle).setCellValue(mappers.get(i).getTitle());
        }
        sheetContext.getSheet().addMergedRegion(new CellRangeAddress(0, 0, columnOffset, columnOffset + mappers.size() - 1));
        for (int y = 0; y < sheetContext.getObjs().size(); y++) {
            T obj = sheetContext.getObjs().get(y);
            Row row = sheetContext.getRows().get(y);
            for (int x = 0; x < mappers.size(); x++) {
                Cell cell = row.createCell(columnOffset + x);
                mappers.get(x).setValue(obj, cell);
            }
        }
    }

    private <T extends Identifiable> void createSheet(List<T> objs, Workbook wb, CellStyle titleCellStyle,
                                                      String sheetTitle, List<ColumnMapper<T>> mappers) {
        if (mappers.isEmpty()) {
            throw new IllegalArgumentException("no mappers provided");
        }

        SheetContext<T> sheetContext = new SheetContext<>(objs, wb, sheetTitle);

        // create id and name columns
        createColumnHeader(sheetContext, titleCellStyle);

        // create state columns
        int stateColumnOffset = 2;
        createColumns(sheetContext, titleCellStyle, stateColumnOffset, network.getVariantManager().getWorkingVariantId(), mappers);

        String oldState = network.getVariantManager().getWorkingVariantId();
        network.getVariantManager().setWorkingVariant(otherState);

        // create other state columns
        int otherStateColumnOffset = stateColumnOffset + mappers.size();
        createColumns(sheetContext, titleCellStyle, otherStateColumnOffset, network.getVariantManager().getWorkingVariantId(), mappers);
        network.getVariantManager().setWorkingVariant(oldState);

        // create diff columns
        int diffColumnOffset = stateColumnOffset + 2 * mappers.size();
        List<ColumnMapper<T>> diffMappers = new ArrayList<>(mappers.size());
        for (int i = 0; i < mappers.size(); i++) {
            diffMappers.add(new DiffColumnMapper<>(mappers.get(i).getTitle(), stateColumnOffset + i, otherStateColumnOffset + i));
        }
        createColumns(sheetContext, titleCellStyle, diffColumnOffset, "Diff", diffMappers);

        // auto resize ID columns
        sheetContext.getSheet().autoSizeColumn(0);
        sheetContext.getSheet().autoSizeColumn(1);

        // auto filter
        sheetContext.getSheet().setAutoFilter(new CellRangeAddress(1, 1, stateColumnOffset, stateColumnOffset + 3 * mappers.size() - 1));

        // create diff stats
        createRowFooter(sheetContext, diffColumnOffset, mappers, 0, "Max", (fromCell, toCell) -> "MAX(" + fromCell + ":" + toCell + ")");
        createRowFooter(sheetContext, diffColumnOffset, mappers, 1, "Min", (fromCell, toCell) -> "MIN(" + fromCell + ":" + toCell + ")");
        createRowFooter(sheetContext, diffColumnOffset, mappers, 2, "Average", (fromCell, toCell) -> "AVERAGE(" + fromCell + ":" + toCell + ")");
    }

    private <T extends Identifiable> void createRowFooter(SheetContext<T> sheetContext, int diffColumnOffset,
                                                          List<ColumnMapper<T>> mappers, int footerIndex, String title, BinaryOperator<String> function) {
        Row rowFooterMax = sheetContext.getSheet().createRow(sheetContext.getObjs().size() + 2 + footerIndex);
        Cell titleCell = rowFooterMax.createCell(diffColumnOffset - 1);
        titleCell.setCellValue(title);
        for (int i = 0; i < mappers.size(); i++) {
            Cell cell = rowFooterMax.createCell(diffColumnOffset + i);
            char letter = (char) ('A' + diffColumnOffset + i);
            String fromCell = letter + "3";
            String toCell = "" + letter + (sheetContext.getObjs().size() + 2);
            cell.setCellFormula(function.apply(fromCell, toCell));
        }
    }

    private void createSheets(Workbook wb, CellStyle titleCellStyle) {
        createSheet(Lists.newArrayList(network.getBusView().getBuses()), wb, titleCellStyle, "Buses", BUS_MAPPERS);
        createSheet(Lists.newArrayList(network.getLines()), wb, titleCellStyle, "Lines", LINE_MAPPERS);
        createSheet(Lists.newArrayList(network.getTwoWindingsTransformers()), wb, titleCellStyle, "Transformers", TRANSFO_MAPPERS);
        createSheet(Lists.newArrayList(network.getGenerators()), wb, titleCellStyle, "Generators", GENERATOR_MAPPERS);
        createSheet(Lists.newArrayList(network.getHvdcConverterStations()), wb, titleCellStyle, "HVDC converter stations", HVDC_MAPPERS);
        createSheet(Lists.newArrayList(network.getLoads()), wb, titleCellStyle, "Loads", LOAD_MAPPERS);
        createSheet(Lists.newArrayList(network.getShuntCompensators()), wb, titleCellStyle, "Shunts", SHUNT_MAPPERS);
    }

    public void generateXls(Path xsl) {
        try (OutputStream out = Files.newOutputStream(xsl)) {
            generateXls(out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void generateXls(OutputStream out) {
        long start = System.currentTimeMillis();
        Workbook wb = new XSSFWorkbook();
        CellStyle titleCellStyle = wb.createCellStyle();
        titleCellStyle.setAlignment(HorizontalAlignment.CENTER);
        createSheets(wb, titleCellStyle);
        try {
            wb.write(out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        LOGGER.info("XLS comparison file {}/{} generated in {} ms", network.getVariantManager().getWorkingVariantId(),
                otherState, System.currentTimeMillis() - start);
    }
}
