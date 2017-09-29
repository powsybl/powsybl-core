// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file ioutil.hpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#include <string>
#include <map>
#include <boost/filesystem/path.hpp>
#include <boost/filesystem/fstream.hpp>
#include <log4cpp/Appender.hh>
#include <log4cpp/Category.hh>

namespace powsybl {

namespace io {

void checkSpace(const boost::filesystem::path& path, log4cpp::Category& logger);
log4cpp::Appender* createConsoleLogAppender();
log4cpp::Appender* createFileLogAppender(const std::string& file);
std::map<std::string, std::string> readProperties(const boost::filesystem::path& file);
std::map<std::string, std::string> getEnv();
int systemSafe(const std::string& command, const boost::filesystem::path& workingDir, const std::map<std::string, std::string>& env, const boost::filesystem::path& outFile, int timeout, log4cpp::Category& logger);
void cleanDir(boost::filesystem::path dir);
size_t dirSize(boost::filesystem::path dir);
std::string readFile(const boost::filesystem::path& file);
void gunzipMem(const std::string& gzipData, const boost::filesystem::path& toFile);
void gunzipFile(const boost::filesystem::path& file, const boost::filesystem::path& destDir);
std::string gzipMem(const boost::filesystem::path& file, log4cpp::Category& logger);
void unzipMem(const std::string& zipMem, const boost::filesystem::path& dir);
void unzipFile(const boost::filesystem::path& zipFile, const boost::filesystem::path& dir);
void zipDir(const boost::filesystem::path& dir, const boost::filesystem::path& zipFile, const std::string& zipDirName);

}

}
