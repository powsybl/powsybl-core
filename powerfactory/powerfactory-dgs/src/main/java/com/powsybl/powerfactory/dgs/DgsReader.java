/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.dgs;

import com.google.common.base.Stopwatch;
import com.powsybl.powerfactory.model.*;
import org.apache.commons.math3.linear.RealMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DgsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DgsReader.class);

    private final DataObjectIndex index = new DataObjectIndex();

    public static StudyCase read(Path dgsFile) {
        return read(dgsFile, StandardCharsets.ISO_8859_1);
    }

    public static StudyCase read(Path dgsFile, Charset charset) {
        try (Reader reader = Files.newBufferedReader(dgsFile, charset)) {
            return new DgsReader().read(dgsFile.getFileName().toString(), reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void buildObjectTree() {
        for (DataObject obj : index.getDataObjects()) {
            obj.findObjectAttributeValue(DataAttribute.FOLD_ID)
                    .flatMap(DataObjectRef::resolve)
                    .ifPresent(obj::setParent);
        }
    }

    private class DgsHandlerImpl implements DgsHandler {

        private DataClass clazz;

        private DataObject object;

        @Override
        public void onGeneralAttribute(String descr, String val) {
            // nothing to do
        }

        @Override
        public void onObjectTableHeader(String tableName) {
            clazz = new DataClass(tableName);
        }

        @Override
        public void onAttributeDescription(String attributeName, DataAttributeType attributeType) {
            clazz.addAttribute(new DataAttribute(attributeName, attributeType, ""));
        }

        @Override
        public void onID(long id) {
            object = new DataObject(id, clazz, index);
        }

        @Override
        public void onStringValue(String attributeName, String value) {
            object.setStringAttributeValue(attributeName, value);
        }

        @Override
        public void onIntegerValue(String attributeName, int value) {
            object.setIntAttributeValue(attributeName, value);
        }

        @Override
        public void onRealValue(String attributeName, float value) {
            object.setFloatAttributeValue(attributeName, value);
        }

        @Override
        public void onObjectValue(String attributeName, long id) {
            object.setObjectAttributeValue(attributeName, id);
        }

        @Override
        public void onDoubleMatrixValue(String attributeName, RealMatrix value) {
            object.setDoubleMatrixAttributeValue(attributeName, value);
        }

        @Override
        public void onStringVectorValue(String attributeName, List<String> values) {
            object.setStringVectorAttributeValue(attributeName, values);
        }

        @Override
        public void onIntVectorValue(String attributeName, List<Integer> values) {
            object.setIntVectorAttributeValue(attributeName, values);
        }

        @Override
        public void onDoubleVectorValue(String attributeName, List<Double> values) {
            object.setDoubleVectorAttributeValue(attributeName, values);
        }

        @Override
        public void onObjectVectorValue(String attributeName, List<Long> ids) {
            object.setObjectVectorAttributeValue(attributeName, ids);
        }
    }

    public StudyCase read(String studyCaseName, Reader reader) {
        Objects.requireNonNull(studyCaseName);
        Objects.requireNonNull(reader);
        Stopwatch stopwatch = Stopwatch.createStarted();

        new DgsParser().read(reader, new DgsHandlerImpl());

        // build object tree (so resolve parents / children links)
        buildObjectTree();

        stopwatch.stop();
        LOGGER.info("DGS file read in {} ms: {} data objects", stopwatch.elapsed(TimeUnit.MILLISECONDS), index.getDataObjects().size());

        List<DataObject> elmNets = index.getDataObjectsByClass("ElmNet");
        Instant studyTime = Instant.ofEpochMilli(0); // FIXME get from head comment?
        return new StudyCase(studyCaseName, studyTime, elmNets, index);
    }
}
