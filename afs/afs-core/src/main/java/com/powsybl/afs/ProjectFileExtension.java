/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;


/**
 * Interface to add a new type of project file to the application file system.
 *
 * <p>
 * In order to add a new type of project file to your {@link AppData} instance,
 * you need to implement that interface and declare it to the runtime using the
 * {@link com.google.auto.service.AutoService} annotation.
 *
 * <p>
 * The instance needs to define the new class, a name identifying that class ("pseudo class"),
 * and to provide a builder object to create an actual instance of the new class. For instance:
 *
 * <pre>
 *      {@literal @}AutoService(ProjectFileExtension.class)
 *      public class FooFileExtension implements ProjectFileExtension<FooFile, FooFileBuilder> {
 *
 *      {@literal @}Override
 *      public Class<FooFile> getProjectFileClass() {
 *          return FooFile.class;
 *      }
 *
 *      {@literal @}Override
 *      public String getProjectFilePseudoClass() {
 *          return "foo";
 *      }
 *
 *      {@literal @}Override
 *      public Class<FooFileBuilder> getProjectFileBuilderClass() {
 *          return FooFileBuilder.class;
 *      }
 *
 *      {@literal @}Override
 *      public FooFile createProjectFile(ProjectFileCreationContext context) {
 *          return new FooFile(context);
 *      }
 *
 *      {@literal @}Override
 *      public FooFileBuilder createProjectFileBuilder(ProjectFileBuildContext context) {
 *          return new FooFileBuilder(context);
 *      }
 *  }
 * </pre>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ProjectFileExtension<T extends ProjectFile, U extends ProjectFileBuilder<T>> {

    /**
     * The new project file type to be injected.
     */
    Class<T> getProjectFileClass();

    /**
     * A "pseudo class" name for the new type.
     */
    String getProjectFilePseudoClass();

    /**
     * The builder class for the new type. Builders will be in charge of creating actual instances of the new type.
     */
    Class<U> getProjectFileBuilderClass();

    /**
     * Creates an object with default constructor.
     */
    T createProjectFile(ProjectFileCreationContext context);

    /**
     * Creates a builder object, to build an instance of the new type with additional parameters passed to the builder.
     */
    ProjectFileBuilder<T> createProjectFileBuilder(ProjectFileBuildContext context);
}
