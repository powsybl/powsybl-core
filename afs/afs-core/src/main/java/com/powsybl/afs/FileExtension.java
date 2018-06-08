/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

/**
 *
 * Interface to add a new type of file to the application file system.
 *
 * <p>
 * In order to add a new type of file to your {@link AppData} instance,
 * you need to implement that interface and declare it to the runtime using the
 * {@link com.google.auto.service.AutoService} annotation.
 *
 * <p>
 * The instance needs to define the new class, a name identifying that class ("pseudo class"),
 * and to provide a creation method for those new file objects. For instance:
 *
 * <pre>
 * {@literal @}AutoService
 * public class MyFileExtension implements FileExtension&lt;MyFile&gt; {
 *
 *    {@literal @}Override
 *    public Class<T> getFileClass() { return MyFile.class; }
 *
 *    {@literal @}Override
 *    public String getFilePseudoClass() { return "myFile"; }
 *
 *    {@literal @}Override
 *    public T createFile(FileCreationContext context) { return new MyFileExtension(context); }
 * }
 * </pre>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface FileExtension<T extends File> {

    /**
     * The new type of object you want to add to your application file system.
     */
    Class<T> getFileClass();

    /**
     * A "pseudo class" name for the new type.
     */
    String getFilePseudoClass();

    /**
     * Creates an actual instance of the new type of file.
     */
    T createFile(FileCreationContext context);
}
