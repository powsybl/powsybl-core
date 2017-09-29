// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file ioutil.cpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#include "ioutil.hpp"
#include <signal.h>
#include <sys/wait.h>
#include <stdlib.h>
#include <sstream>
#include <boost/version.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/filesystem/operations.hpp>
#include <boost/filesystem/fstream.hpp>
#include <boost/iostreams/filtering_streambuf.hpp>
#include <boost/iostreams/copy.hpp>
#include <boost/iostreams/filter/gzip.hpp>
#include <boost/algorithm/string/trim.hpp>
#include <log4cpp/OstreamAppender.hh>
#include <log4cpp/FileAppender.hh>
#include <log4cpp/PatternLayout.hh>
#include <archive.h>
#include <archive_entry.h>

#if BOOST_VERSION >= 104600
#define BOOST_FILENAME_STRING(filename) filename.string()
#else
#define BOOST_FILENAME_STRING(filename) filename
#endif

extern char** environ;

namespace powsybl {

namespace io {

const float SPACE_WARNING_THRESHOLD = 0.1f;

void checkSpace(const boost::filesystem::path& path, log4cpp::Category& logger) {
    boost::filesystem::space_info space = boost::filesystem::space(path);
    float p = (float) space.available / space.capacity;
    if (p < SPACE_WARNING_THRESHOLD) {
        logger.warnStream() << path.string() << " almost full (" << p * 100 << "% available)" << log4cpp::eol;
    }
}

log4cpp::PatternLayout* createLayout() {
    log4cpp::PatternLayout* layout = new log4cpp::PatternLayout();
    layout->setConversionPattern("%d{%Y-%m-%d_%H:%M:%S.%l} %p %c - %m%n");
    return layout;
}

log4cpp::Appender* createConsoleLogAppender() {
    log4cpp::Appender* appender = new log4cpp::OstreamAppender("console", &std::cout);
    appender->setLayout(createLayout());
    return appender;
}

log4cpp::Appender* createFileLogAppender(const std::string& file) {
    log4cpp::Appender* appender = new log4cpp::FileAppender("file", file);
    appender->setLayout(createLayout());
    return appender;
}

std::map<std::string, std::string> readProperties(const boost::filesystem::path& file) {
    if (!boost::filesystem::exists(file) || !boost::filesystem::is_regular(file)) {
        throw std::runtime_error(file.string() + " does not exist or is not a regular file");
    }
    std::map<std::string, std::string> properties;
    std::ifstream ifs(file.string());
    std::string line;
    while (std::getline(ifs, line)) {
        boost::algorithm::trim(line);
        if (line.empty()) {
            continue;
        }
        std::istringstream iss(line);
        std::string name;
        std::string value;
        if (!std::getline(iss, name, '=') || !std::getline(iss, value, '=')) {
            throw std::runtime_error("Error while reading line '" + line + "' of file " + file.string());
        }
        properties[name] = value;
    }
    if (!ifs.eof()) {
        throw std::runtime_error("Error while reading file " + file.string());
    }
    return properties;
}

std::map<std::string, std::string> getEnv() {
    std::map<std::string, std::string> env;
    for(char** v = environ; *v; v++) {
        std::istringstream iss(*v);
        std::string name;
        std::string value;
        std::getline(iss, name, '=');
        std::getline(iss, value, '=');
        env[name] = value;
    }
    return env;
}

std::string errnoMsg(const std::string& f) {
    char buferr[256];
    std::string msg = f + " : ";
    msg += strerror_r(errno, buferr, sizeof(buferr));
    return msg;
}

int systemSafe(const std::string& command, const boost::filesystem::path& workingDir, const std::map<std::string, std::string>& env, const boost::filesystem::path& outFile, int timeout, log4cpp::Category& logger) {
    std::string command2;
    if (timeout != -1) {
        command2 = "timeout -s KILL " + boost::lexical_cast<std::string>(timeout) + " ";
    }
    command2 += "/bin/sh -c '";
    if (env.size() > 0) {
        for (std::map<std::string, std::string>::const_iterator it = env.begin(); it != env.end(); it++) {
            command2 += "export ";
            command2 += it->first;
            command2 += "=";
            command2 += it->second;
            command2 += "; ";
        }
    }
    command2 += "cd " + workingDir.string() + "; " + command + " >> \"" + outFile.string() + "\" 2>&1'";
    int status = ::system(command2.c_str());
    if (WIFEXITED(status)) {
        int exitCode = WEXITSTATUS(status);
        log4cpp::CategoryStream stream = (exitCode == 0 ? logger.debugStream() : logger.warnStream());
        // log the full system command line in case of 127
        stream << "command '" << (exitCode == 127 ? command2 : command) << "' exited with code " << exitCode << log4cpp::eol;
        return exitCode;
    } else if (WIFSIGNALED(status)) {
        int signal = WTERMSIG(status);
        logger.warnStream() << "command '" << command << "' received signal " << signal << log4cpp::eol;
        return signal;
    } else {
        logger.warnStream() << "command '" << command << "' terminated with unexpected status " << status << log4cpp::eol;
        return status;
    }
}

void cleanDir(boost::filesystem::path dir) {
    for (boost::filesystem::directory_iterator end_dir_it, it(dir); it != end_dir_it; ++it) {
        boost::filesystem::remove_all(it->path());
    }
}

size_t dirSize(boost::filesystem::path dir) {
    size_t size = 0;
    for(boost::filesystem::recursive_directory_iterator it(dir);
        it != boost::filesystem::recursive_directory_iterator();
        ++it) {   
        if(!boost::filesystem::is_directory(*it)) {
            size += boost::filesystem::file_size(*it);            
        }
    }
    return size;
}

std::string readFile(const boost::filesystem::path& file) {
    boost::filesystem::ifstream ifs(file, std::ios::binary);
    return std::string(std::istreambuf_iterator<char>(ifs), std::istreambuf_iterator<char>());
}

void gunzipMem(const std::string& gzipData, const boost::filesystem::path& toFile) {
	boost::filesystem::ofstream ofs(toFile, std::ios::binary);
	std::istringstream iss(gzipData, std::ios_base::binary);
	boost::iostreams::filtering_streambuf<boost::iostreams::input> in;
	in.push(boost::iostreams::gzip_decompressor());
	in.push(iss);
	boost::iostreams::copy(in, ofs);
}

void gunzipFile(const boost::filesystem::path& file, const boost::filesystem::path& destDir) {
	if (!boost::filesystem::exists(file) || !boost::filesystem::is_regular(file)) {
        throw std::runtime_error(file.string() + " does not exist or is not a regular file");
    }
	if (!boost::filesystem::exists(destDir) || !boost::filesystem::is_directory(destDir)) {
        throw std::runtime_error(destDir.string() + " does not exist or is not a directory");
    }
	boost::filesystem::ifstream ifs(file, std::ios_base::binary);
	boost::iostreams::filtering_streambuf<boost::iostreams::input> in;
	in.push(boost::iostreams::gzip_decompressor());
	in.push(ifs);
	boost::filesystem::ofstream ofs(destDir / file.stem(), std::ios::binary);
	boost::iostreams::copy(in, ofs);
}

std::string gzipMem(const boost::filesystem::path& file, log4cpp::Category& logger) {
    if (!boost::filesystem::exists(file) || !boost::filesystem::is_regular(file)) {
        throw std::runtime_error(file.string() + " does not exist or is not a regular file");
    }
    std::ostringstream oss(std::ios_base::binary);
    try {
    	boost::filesystem::ifstream ifs(file, std::ios::binary);
    	boost::iostreams::filtering_streambuf<boost::iostreams::input> in;
    	in.push(boost::iostreams::gzip_compressor());
    	in.push(ifs);
    	boost::iostreams::copy(in, oss);
    } catch(const boost::iostreams::gzip_error& e) {
        logger.errorStream() << "Gzip error (msg='"  << e.what() << "', error=" << e.error() << ", zlib_error_code=" << e.zlib_error_code()
                             << ", file=" << file.string() << ", file_size=" << boost::filesystem::file_size(file) << ")" << log4cpp::eol;
        throw;
    }
	return oss.str();
}

void unzipMem(const std::string& zipMem, const boost::filesystem::path& dir) {
    if (!boost::filesystem::exists(dir) || !boost::filesystem::is_directory(dir)) {
        throw std::runtime_error(dir.string() + " does not exist or is not a directory");
    }
    struct archive* a = archive_read_new();
    struct archive_entry* entry;
    archive_read_support_format_zip(a);
    archive_read_open_memory(a, (void*) zipMem.c_str(), zipMem.length());
    size_t bufferSize = 1000;
    char buffer[bufferSize];
    while (archive_read_next_header(a, &entry) == ARCHIVE_OK) {
        boost::filesystem::path file = dir / archive_entry_pathname(entry);
        boost::filesystem::ofstream ofs(file, std::ios_base::binary);
        for (;;) {
        	size_t size = archive_read_data(a, buffer, bufferSize);
        	if (size < 0) {
        		throw std::runtime_error("error archive_read_data");
        	}
        	if (size == 0) {
				break;
        	}
        	ofs.write(buffer, size);
        }
    }
    archive_read_finish(a);
}

void unzipFile(const boost::filesystem::path& zipFile, const boost::filesystem::path& dir) {
    if (!boost::filesystem::exists(zipFile) || !boost::filesystem::is_regular(zipFile)) {
        throw std::runtime_error(zipFile.string() + " does not exist or is not a regular file");
    }
    if (!boost::filesystem::exists(dir) || !boost::filesystem::is_directory(dir)) {
        throw std::runtime_error(dir.string() + " does not exist or is not a directory");
    }
    struct archive* a = archive_read_new();
    struct archive_entry* entry;
    archive_read_support_format_zip(a);
    int r = archive_read_open_filename(a, zipFile.string().c_str(), 10240);
    if (r != ARCHIVE_OK) {
        throw std::runtime_error("error archive_read_open_filename");
    }
    size_t bufferSize = 1000;
    char buffer[bufferSize];
    while (archive_read_next_header(a, &entry) == ARCHIVE_OK) {
        boost::filesystem::path file = dir / archive_entry_pathname(entry);
        boost::filesystem::ofstream ofs(file, std::ios_base::binary);
		for (;;) {
        	size_t size = archive_read_data(a, buffer, bufferSize);
        	if (size < 0) {
        		throw std::runtime_error("error archive_read_data");
        	}
        	if (size == 0) {
				break;
        	}
        	ofs.write(buffer, size);
        }
    }
    archive_read_finish(a);
}

void zipDir(const boost::filesystem::path& dir, const boost::filesystem::path& zipFile, const std::string& zipDirName) {
    if (!boost::filesystem::exists(dir) || !boost::filesystem::is_directory(dir)) {
        throw std::runtime_error(dir.string() + " does not exist or is not a directory");
    }
    struct archive* a = archive_write_new();
    archive_write_set_format_zip(a);
    archive_write_open_filename(a, zipFile.string().c_str());
    for (boost::filesystem::directory_iterator endDirIt, it(dir); it != endDirIt; ++it) {
        boost::filesystem::path file = it->path();
        struct stat st;
        stat(file.string().c_str(), &st);
        struct archive_entry * entry = archive_entry_new();
        archive_entry_set_pathname(entry, (zipDirName + "/" + BOOST_FILENAME_STRING(file.filename())).c_str());
        archive_entry_set_size(entry, st.st_size);
        archive_entry_set_filetype(entry, AE_IFREG);
        archive_entry_set_perm(entry, 0644);
        archive_write_header(a, entry);
        std::string buffer = readFile(file);
        archive_write_data(a, buffer.c_str(), buffer.size());
        archive_entry_free(entry);
    }
    archive_write_close(a);
    archive_write_finish(a);
}

}

}
