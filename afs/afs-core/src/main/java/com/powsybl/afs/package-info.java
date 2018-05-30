/**
 *
 *     AFS stands for Application FileSystem.
 *     <p>
 *     This package contains bases classes which define the concept of AFS.
 *     An AFS is meant to be used to organize your business data and store them,
 *     like a file system does for file.
 *
 *     <p>
 *     The way data is actually stored is implementation-dependent,
 *     and is defined through the implementation of the {@link com.powsybl.afs.storage.AppStorage} interface.
 *
 *     <p>
 *     The entry point of AFS is the {@linkplain com.powsybl.afs.AppData} object.
 *     It contains a list of {@link com.powsybl.afs.AppFileSystem}.
 *
 *     <p>
 *     The structure of an AFS looks like:
 *
 *     <pre>
 *         AppData
 *          |
 *          +-- FileSystem1
 *          |   +-- File1
 *          |   +-- File2
 *          |   +-- Project1
 *          |   |   +-- RootFolder
 *          |   |       +-- ProjectFile1
 *          |   |       +-- ProjectFolder1
 *          |   |       |   +-- ProjectFile2
 *          |   |       +-- ProjectFolder2
 *          |   |           +-- ProjectFile3
 *          |   +-- Project2
 *          |      ...
 *          |
 *          +-- FileSystem2
 *              ...
 *
 *
 *     </pre>
 *
 *     <p>
 *     You may add your own types of files and project files through and extension mechanism, see {@link com.powsybl.afs.FileExtension}
 *     and {@link com.powsybl.afs.ProjectFileExtension}.
 *
 *
 *     @auhor Sylvain Leclerc <sylvain.leclerc@rte-france.com>
 *
 */
package com.powsybl.afs;
