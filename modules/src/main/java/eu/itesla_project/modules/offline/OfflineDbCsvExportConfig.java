/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.offline;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OfflineDbCsvExportConfig {

    private char delimiter;

    private OfflineAttributesFilter filter;

    private boolean addSampleColumn;

    private boolean keepAllSamples;

    private boolean addHeader;

    private int startSample;

    private int maxSamples;

    private static int checkStartSampleValue(int startSample) {
        if (startSample < 0) {
            throw new RuntimeException("Invalid startSample value " + startSample);
        }
        return startSample;
    }

    private static int checkMaxSamples(int maxSamples) {
        if (maxSamples < -1 || maxSamples == 0) {
            throw new RuntimeException("Invalid maxSamples value " + maxSamples);
        }
        return maxSamples;
    }

    public OfflineDbCsvExportConfig(char delimiter, OfflineAttributesFilter filter, boolean addSampleColumn, boolean keepAllSamples, boolean addHeader, int startSample, int maxSamples) {
        this.delimiter = delimiter;
        this.filter = Objects.requireNonNull(filter);
        this.addSampleColumn = addSampleColumn;
        this.keepAllSamples = keepAllSamples;
        this.addHeader = addHeader;
        this.startSample = checkStartSampleValue(startSample);
        this.maxSamples = checkMaxSamples(maxSamples);
    }

    public OfflineDbCsvExportConfig(char delimiter, OfflineAttributesFilter filter, boolean addSampleColumn, boolean keepAllSamples) {
        this(delimiter, filter, addSampleColumn, keepAllSamples, true, 0, -1);
    }

    public boolean isAddSampleColumn() {
        return addSampleColumn;
    }

    public void setAddSampleColumn(boolean addSampleColumn) {
        this.addSampleColumn = addSampleColumn;
    }

    public char getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    public OfflineAttributesFilter getFilter() {
        return filter;
    }

    public void setFilter(OfflineAttributesFilter filter) {
        this.filter = filter;
    }

    public boolean isKeepAllSamples() {
        return keepAllSamples;
    }

    public void setKeepAllSamples(boolean keepAllSamples) {
        this.keepAllSamples = keepAllSamples;
    }

    public boolean isAddHeader() {
        return addHeader;
    }

    public void setAddHeader(boolean addHeader) {
        this.addHeader = addHeader;
    }

    public int getStartSample() {
        return startSample;
    }

    public void setStartSample(int startSample) {
        this.startSample = checkStartSampleValue(startSample);
    }

    public int getMaxSamples() {
        return maxSamples;
    }

    public void setMaxSamples(int maxSamples) {
        this.maxSamples = maxSamples;
    }
}
