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
 *     and is defined through the implementation of the {@linkplain com.powsybl.afs.storage.AppStorage AppStorage} interface.
 *
 *     <p>
 *     The entry point of AFS is the {@linkplain com.powsybl.afs.AppData AppData} object.
 *     It contains a list of {@linkplain com.powsybl.afs.AppFileSystem AppFileSystem}s.
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
 *     You may add your own types of files and project files through and extension mechanism, see {@linkplain com.powsybl.afs.FileExtension FileExtension}
 *     and {@linkplain com.powsybl.afs.ProjectFileExtension ProjectFileExtension}.
 *
 *
 *     @auhor Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 *
 */
package com.powsybl.afs;
