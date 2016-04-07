/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.datasource;

import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ClassLoaderZipFileDataSource extends AbstractDataSource {

    private final String baseName;

    private final GenericArchive archive;

    public ClassLoaderZipFileDataSource(Class<?> aClass, String dirName, String baseName) {
        this.baseName = Objects.requireNonNull(baseName);
        archive = ShrinkWrap.create(ZipImporter.class).importFrom(aClass.getResourceAsStream(dirName + "/" + baseName + ".zip"))
                .as(GenericArchive.class);
    }

    @Override
    public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean exists(String suffix, String ext) {
        return exists(getFileName(baseName, suffix, ext));
    }

    @Override
    public boolean exists(String fileName) {
         return archive.get(fileName) != null;
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) {
        String fileName = getFileName(baseName, suffix, ext);
        return newInputStream(fileName);
    }

    @Override
    public InputStream newInputStream(String fileName) {
        Node node = archive.get(fileName);
        if (node == null) {
            throw new RuntimeException(fileName + " does not exist");
        }
        return node.getAsset().openStream();
    }
}
