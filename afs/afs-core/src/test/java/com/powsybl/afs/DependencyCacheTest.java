/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import com.google.common.collect.ImmutableList;
import com.powsybl.afs.mapdb.storage.MapDbAppStorage;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.InMemoryEventsBus;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DependencyCacheTest extends AbstractProjectFileTest {

    class Tic extends ProjectFile {

        Tic(ProjectFileCreationContext context) {
            super(context, 0);
        }
    }

    class TicBuilder implements ProjectFileBuilder<Tic> {

        private final ProjectFileBuildContext context;

        private String name;

        TicBuilder(ProjectFileBuildContext context) {
            this.context = context;
        }

        public TicBuilder setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Tic build() {
            NodeInfo info = context.getStorage().createNode(context.getFolderInfo().getId(), name, "TIC", "", 0, new NodeGenericMetadata());
            context.getStorage().setConsistent(info.getId());
            return new Tic(new ProjectFileCreationContext(info, context.getStorage(), context.getProject()));
        }
    }

    class TicExtension implements ProjectFileExtension<Tic, TicBuilder> {

        @Override
        public Class<Tic> getProjectFileClass() {
            return Tic.class;
        }

        @Override
        public String getProjectFilePseudoClass() {
            return "TIC";
        }

        @Override
        public Class<TicBuilder> getProjectFileBuilderClass() {
            return TicBuilder.class;
        }

        @Override
        public Tic createProjectFile(ProjectFileCreationContext context) {
            return new Tic(context);
        }

        @Override
        public ProjectFileBuilder<Tic> createProjectFileBuilder(ProjectFileBuildContext context) {
            return new TicBuilder(context);
        }
    }

    class Tac extends ProjectFile {

        private static final String DEP_NAME = "dep";

        private final DependencyCache<ProjectFile> cache = new DependencyCache<>(this, DEP_NAME, ProjectFile.class);

        Tac(ProjectFileCreationContext context) {
            super(context, 0);
        }

        ProjectFile getTicDependency() {
            return cache.getFirst().orElse(null);
        }

        void setTicDependency(Tic tic) {
            setDependencies(DEP_NAME, Collections.singletonList(tic));
            cache.invalidate();
        }
    }

    class TacBuilder implements ProjectFileBuilder<Tac> {

        private final ProjectFileBuildContext context;

        TacBuilder(ProjectFileBuildContext context) {
            this.context = context;
        }

        @Override
        public Tac build() {
            NodeInfo info = context.getStorage().createNode(context.getFolderInfo().getId(), "tac", "TAC", "", 0, new NodeGenericMetadata());
            context.getStorage().setConsistent(info.getId());
            return new Tac(new ProjectFileCreationContext(info, context.getStorage(), context.getProject()));
        }
    }

    class TacExtension implements ProjectFileExtension<Tac, TacBuilder> {

        @Override
        public Class<Tac> getProjectFileClass() {
            return Tac.class;
        }

        @Override
        public String getProjectFilePseudoClass() {
            return "TAC";
        }

        @Override
        public Class<TacBuilder> getProjectFileBuilderClass() {
            return TacBuilder.class;
        }

        @Override
        public Tac createProjectFile(ProjectFileCreationContext context) {
            return new Tac(context);
        }

        @Override
        public ProjectFileBuilder<Tac> createProjectFileBuilder(ProjectFileBuildContext context) {
            return new TacBuilder(context);
        }
    }

    @Override
    protected AppStorage createStorage() {
        return MapDbAppStorage.createMem("mem", new InMemoryEventsBus());
    }

    @Override
    protected List<ProjectFileExtension> getProjectFileExtensions() {
        return ImmutableList.of(new TicExtension(), new TacExtension());
    }

    @Test
    public void test() {
        Project project = afs.getRootFolder().createProject("project");
        Tic tic = project.getRootFolder().fileBuilder(TicBuilder.class).setName("tic").build();
        Tic tic2 = project.getRootFolder().fileBuilder(TicBuilder.class).setName("tic2").build();
        Tac tac = project.getRootFolder().fileBuilder(TacBuilder.class).build();
        assertNull(tac.getTicDependency());
        tac.setTicDependency(tic);
        assertNotNull(tac.getTicDependency());
        assertEquals(tic.getId(), tac.getTicDependency().getId());
        tac.setTicDependency(tic2);
        assertNotNull(tac.getTicDependency());
        assertEquals(tic2.getId(), tac.getTicDependency().getId());
    }
}
