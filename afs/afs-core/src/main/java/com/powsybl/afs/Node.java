/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class Node extends AbstractNodeBase<Folder> {

    protected final AppFileSystem fileSystem;

    protected final boolean folder;

    protected Node(FileCreationContext context, int codeVersion, boolean folder) {
        super(context.getInfo(), context.getStorage(), codeVersion);
        this.fileSystem = Objects.requireNonNull(context.getFileSystem());
        this.folder = folder;
    }

    public void delete() {
        storage.deleteNode(info.getId());
        storage.flush();
    }

    public void moveTo(Folder folder) {
        Objects.requireNonNull(folder);
        storage.setParentNode(info.getId(), folder.getId());
        storage.flush();
    }

    @Override
    public boolean isFolder() {
        return folder;
    }

    @Override
    public Optional<Folder> getParent() {
        return storage.getParentNode(info.getId()).map(parentInfo -> new Folder(new FileCreationContext(parentInfo, storage, fileSystem)));
    }

    private static boolean pathStop(Node node) {
        return !node.getParent().isPresent();
    }

    private static String pathToString(List<String> path) {
        StringBuilder builder = new StringBuilder();
        builder.append(path.get(0))
                .append(AppFileSystem.FS_SEPARATOR);
        for (int i = 1; i < path.size(); i++) {
            builder.append(AppFileSystem.PATH_SEPARATOR).append(path.get(i));
        }
        return builder.toString();
    }

    @Override
    public NodePath getPath() {
        return NodePath.find(this, Node::pathStop, Node::pathToString);
    }

    public AppFileSystem getFileSystem() {
        return fileSystem;
    }
}
