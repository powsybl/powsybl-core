// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file slave.cpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#include <mpi.h>
#include <iostream>
#include <stdexcept>
#include <fstream>
#include <thread>
#include <mutex>
#include <condition_variable>
#include <chrono>
#include <cmath>
#include <jni.h>
#include <signal.h>
#include <boost/lexical_cast.hpp>
#define BOOST_NO_CXX11_SCOPED_ENUMS
#include <boost/filesystem.hpp>
#include <boost/filesystem/path.hpp>
#include <boost/filesystem/fstream.hpp>
#include <boost/program_options/parsers.hpp>
#include <boost/program_options/options_description.hpp>
#include <boost/program_options/variables_map.hpp>
#include <boost/algorithm/string/replace.hpp>
#include <google/protobuf/io/zero_copy_stream_impl.h>
#include <google/protobuf/io/coded_stream.h>
#include <log4cpp/Category.hh>
#include <log4cpp/Priority.hh>
#include <sys/stat.h>
#include "mpitags.hpp"
#include "mpiutil.hpp"
#include "ioutil.hpp"
#include "messages.pb.h"

namespace powsybl {

namespace slave {

const std::string COMMON_DIR_PREFIX = "itools_common_";
const std::string WORKING_DIR_PREFIX = "itools_work_";
const std::string JOB_DIR_PREFIX = "itools_job_";

boost::filesystem::path commonDir(const boost::filesystem::path& localDir, int rank) {
    return localDir / (COMMON_DIR_PREFIX + boost::lexical_cast<std::string>(rank));
}

boost::filesystem::path workingDir(const boost::filesystem::path& localDir, int rank, int threadNum) {
    return localDir / (WORKING_DIR_PREFIX + boost::lexical_cast<std::string>(rank) + "_" + boost::lexical_cast<std::string>(threadNum));
}

boost::filesystem::path jobDir(const boost::filesystem::path& localDir, int rank, int jobId) {
    return localDir / (JOB_DIR_PREFIX + boost::lexical_cast<std::string>(rank) + "_" + boost::lexical_cast<std::string>(jobId)); 
}

log4cpp::Category& createLogger(int rank, int threadNum, bool verbose) {
    std::string loggerName = "Slave" + boost::lexical_cast<std::string>(rank);
    if (threadNum >= 0) {
        loggerName += "_" + boost::lexical_cast<std::string>(threadNum);
    }
    log4cpp::Category& logger = log4cpp::Category::getInstance(loggerName);
    logger.setPriority(verbose ? log4cpp::Priority::DEBUG : log4cpp::Priority::INFO);
    return logger;
}

void parseProtobufMessage(const std::string& buffer, google::protobuf::Message& message) {
    std::istringstream iss(buffer, std::ios::binary);
    google::protobuf::io::IstreamInputStream isis(&iss);
    google::protobuf::io::CodedInputStream cis(&isis);
    cis.SetTotalBytesLimit(buffer.size()+1, buffer.size()+1);
    if (!message.ParseFromCodedStream(&cis)) {
        throw std::runtime_error("Failed to parse message");
    }
}

void writeInputFile(const messages::Task::InputFile& inputFile, const boost::filesystem::path& dir) {
    if (!inputFile.has_data()) {
        throw std::runtime_error("Data is expected");
    }
    switch (inputFile.preprocessor()) {
        case messages::Task_InputFile_PreProcessor_NONE:
            {
                boost::filesystem::ofstream ofs(dir / inputFile.name(), std::ios::binary);
                ofs << inputFile.data();
            }
            break;
        case messages::Task_InputFile_PreProcessor_ARCHIVE_UNZIP:
            powsybl::io::unzipMem(inputFile.data(), dir);
            break;
        case messages::Task_InputFile_PreProcessor_FILE_GUNZIP:
            powsybl::io::gunzipMem(inputFile.data(), dir / inputFile.name().substr(0, inputFile.name().size()-3));
            break;
        default:
            throw std::runtime_error("Unknown pre-processor");
    }
}

boost::filesystem::path toExplodedArchiveDir(const boost::filesystem::path& archiveFile) {
    return archiveFile.parent_path() / (archiveFile.filename().string() + ".exploded");
}

void explodeInputFileArchive(messages::Task_InputFile_PreProcessor preProcessor, const boost::filesystem::path& archiveFile) {
    switch (preProcessor) {
        case messages::Task_InputFile_PreProcessor_NONE:
            // nothing to explode...
            break;
        case messages::Task_InputFile_PreProcessor_ARCHIVE_UNZIP:
            {
                boost::filesystem::path explodedArchiveDir = toExplodedArchiveDir(archiveFile);
                boost::filesystem::remove_all(explodedArchiveDir);
                boost::filesystem::create_directory(explodedArchiveDir);
                powsybl::io::unzipFile(archiveFile, explodedArchiveDir);
            }
            break;
        case messages::Task_InputFile_PreProcessor_FILE_GUNZIP:
            {
                boost::filesystem::path explodedArchiveDir = toExplodedArchiveDir(archiveFile);
                boost::filesystem::remove_all(explodedArchiveDir);
                boost::filesystem::create_directory(explodedArchiveDir);
                powsybl::io::gunzipFile(archiveFile, explodedArchiveDir);
            }
            break;
        default:
            throw std::runtime_error("Unknown pre-processor");
    }
}

std::mutex explodedArchiveMutex;

void createSymbLinkToInputFile(messages::Task_InputFile_PreProcessor preProcessor, const boost::filesystem::path& file, const boost::filesystem::path& dir) {
    if (!boost::filesystem::exists(dir) || !boost::filesystem::is_directory(dir)) {
        throw std::runtime_error(dir.string() + " does not exist or is not a directory");
    }
    switch (preProcessor) {
        case messages::Task_InputFile_PreProcessor_NONE:
            boost::filesystem::create_symlink(file, dir / file.filename().string());
            break;
        case messages::Task_InputFile_PreProcessor_ARCHIVE_UNZIP:
        case messages::Task_InputFile_PreProcessor_FILE_GUNZIP:
            {
                boost::filesystem::path explodedArchiveDir = toExplodedArchiveDir(file);
                {
                    // lock to avoid 2 worker threads to explode the archive at the same time
                    std::lock_guard<std::mutex> lock(explodedArchiveMutex);
                    explodeInputFileArchive(preProcessor, file);
                }
                // create one symbolic link for each file of the exploded archive
                for (boost::filesystem::directory_iterator endDirIt, it(explodedArchiveDir); it != endDirIt; ++it) {
                    boost::filesystem::path p = it->path();
                    boost::filesystem::create_symlink(p, dir / p.filename().string());
                }
            }
            break;
        default:
            throw std::runtime_error("Unknown pre-processor");
    }
}

class CommunicationManager {
public:

    struct WorkerThreadContext {
        WorkerThreadContext(int threadNum) 
            : _threadNum(threadNum), 
              _lengthRequest(MPI_REQUEST_NULL),
              _bufferRequest(MPI_REQUEST_NULL) {}
        WorkerThreadContext(const WorkerThreadContext&) = delete;
        WorkerThreadContext& operator=(const WorkerThreadContext&) = delete;

        const int _threadNum;

        int _length;
        MPI_Request _lengthRequest;

        std::string _buffer;
        MPI_Request _bufferRequest;

        std::mutex _mutexCond;
        std::condition_variable _cond;
    };

    struct MpiThreadContext {
        MpiThreadContext(int cores, const boost::filesystem::path& localDir, bool verbose, const boost::filesystem::path& archiveDir) 
            : _cores(cores),
              _localDir(localDir),
              _verbose(verbose),
              _archiveDir(archiveDir),
              _rank(-1),
              _abortRequested(false),
              _step(Step::COMMON_FILES_BCAST)
        {
            _stopRequested.reserve(_cores);
            _stopped.reserve(_cores);
            for (int i = 0; i < _cores; i++) {
                _stopRequested.push_back(false);
                _stopped.push_back(false);
            }
        }
        MpiThreadContext(const MpiThreadContext&) = delete;
        MpiThreadContext& operator=(const MpiThreadContext&) = delete;

        int _cores;
        boost::filesystem::path _localDir;
        bool _verbose;
        boost::filesystem::path _archiveDir;

        int _rank;
        std::mutex _startedMutex;
        std::condition_variable _startedCond;

        std::mutex _receivingContextsMutex;
        std::vector<std::shared_ptr<WorkerThreadContext> > _receivingContexts;

        std::mutex _sendingContextsMutex;
        std::vector<std::shared_ptr<WorkerThreadContext> > _sendingContexts;

        bool isWorkerThreadStopRequested(int threadNum);
        void requestWorkerThreadStop(int threadNum);
        void cancelWorkerThreadStopRequest(int threadNum);

        void workerThreadStopped(int threadNum);
        bool areAllWorkerThreadsStopped();
        void resetWorkerThreadsStopStatus();

        bool isAbortRequested();
        void requestAbort();

        void waitForJobInitialization(int jobId);
        void notifyJobInitialization(int jobId);

        bool receiveFromMaster(const std::shared_ptr<WorkerThreadContext>& receivingContext);
        void sendToMaster(const std::shared_ptr<WorkerThreadContext>& sendingContext);

        std::mutex _stopRequestMutex;
        std::vector<bool> _stopRequested;

        std::mutex _stopMutex;
        std::vector<bool> _stopped;

        std::mutex _abortMutex;
        bool _abortRequested;

        std::set<int> _initializedJobs;
        std::mutex _initializedJobsMutex;
        std::condition_variable _initializedJobsCond;

        Step _step;
    };

    CommunicationManager(int cores, const boost::filesystem::path& localDir, bool verbose, const boost::filesystem::path& archiveDir);
    CommunicationManager(const CommunicationManager&) = delete;
    CommunicationManager& operator=(const CommunicationManager&) = delete;

    void start(int& rank);
    void wait();

private:    
    static const int TIMEOUT; 

    static void mpiThread(const std::shared_ptr<MpiThreadContext>& MpiThreadContext);
    static void workerThread(const std::shared_ptr<MpiThreadContext>& mpiThreadContext, int threadNum);
    static void commonFilesBroadcastStep(const std::shared_ptr<CommunicationManager::MpiThreadContext>& mpiThreadContext, log4cpp::Category& logger);
    static void tasksExecutionStep(const std::shared_ptr<CommunicationManager::MpiThreadContext>& mpiThreadContext, log4cpp::Category& logger);

    std::shared_ptr<MpiThreadContext> _mpiThreadContext;
    std::shared_ptr<std::thread> _mpiThread;
};
        
const int CommunicationManager::TIMEOUT = 10; 

bool CommunicationManager::MpiThreadContext::isWorkerThreadStopRequested(int threadNum) {
    std::lock_guard<std::mutex> lock(_stopRequestMutex);
    return _stopRequested[threadNum];
}

void CommunicationManager::MpiThreadContext::requestWorkerThreadStop(int threadNum) {
    std::lock_guard<std::mutex> lock(_stopRequestMutex);
    _stopRequested[threadNum] = true;
}

void CommunicationManager::MpiThreadContext::cancelWorkerThreadStopRequest(int threadNum) {
    std::lock_guard<std::mutex> lock(_stopRequestMutex);
    _stopRequested[threadNum] = false;
}      

void CommunicationManager::MpiThreadContext::workerThreadStopped(int threadNum) {
    std::lock_guard<std::mutex> lock(_stopMutex);
    _stopped[threadNum] = true;
}

bool CommunicationManager::MpiThreadContext::areAllWorkerThreadsStopped() {
    std::lock_guard<std::mutex> lock(_stopMutex);
    for (std::vector<bool>::const_iterator it = _stopped.begin(); it != _stopped.end(); it++) {
        if (!*it) {
            return false;
        }
    }
    return true;
}

void CommunicationManager::MpiThreadContext::resetWorkerThreadsStopStatus() {
    std::lock_guard<std::mutex> lock(_stopMutex);
    for (size_t i = 0; i < _stopped.size(); i++) {
        _stopped[i] = false;
    }
}

bool CommunicationManager::MpiThreadContext::isAbortRequested() {
    std::lock_guard<std::mutex> lock(_abortMutex);
    return _abortRequested;
}
        

void CommunicationManager::MpiThreadContext::requestAbort() {
    std::lock_guard<std::mutex> lock(_abortMutex);
    _abortRequested = true;
}

void CommunicationManager::MpiThreadContext::waitForJobInitialization(int jobId) {
    while (true) {
        std::unique_lock<std::mutex> initializedJobsLock(_initializedJobsMutex);
        if (_initializedJobs.count(jobId)) {
            break;
        }
        _initializedJobsCond.wait(initializedJobsLock);
    }
}

void CommunicationManager::MpiThreadContext::notifyJobInitialization(int jobId) {
    std::unique_lock<std::mutex> initializedJobsLock(_initializedJobsMutex);
    _initializedJobs.insert(jobId);
    _initializedJobsCond.notify_all();
}

bool CommunicationManager::MpiThreadContext::receiveFromMaster(const std::shared_ptr<WorkerThreadContext>& receivingContext) {
    std::unique_lock<std::mutex> lockCond(receivingContext->_mutexCond);
    {
        std::lock_guard<std::mutex> receivingContextsLock(_receivingContextsMutex);
        _receivingContexts.push_back(receivingContext);        
    }
    receivingContext->_cond.wait(lockCond);
    if (isWorkerThreadStopRequested(receivingContext->_threadNum)) {
        cancelWorkerThreadStopRequest(receivingContext->_threadNum);
        return false;
    } else {
        return true;
    }
}

void CommunicationManager::MpiThreadContext::sendToMaster(const std::shared_ptr<WorkerThreadContext>& sendingContext) {
    std::unique_lock<std::mutex> lockCond(sendingContext->_mutexCond);
    {
        std::lock_guard<std::mutex> sendingContextsLock(_sendingContextsMutex);
        _sendingContexts.push_back(sendingContext);        
    }
    sendingContext->_cond.wait(lockCond);
}

CommunicationManager::CommunicationManager(int cores, const boost::filesystem::path& localDir, bool verbose, const boost::filesystem::path& archiveDir)
    : _mpiThreadContext(new MpiThreadContext(cores, localDir, verbose, archiveDir))
{}

void CommunicationManager::start(int& rank) {
    _mpiThread.reset(new std::thread(&CommunicationManager::mpiThread, _mpiThreadContext));
    std::unique_lock<std::mutex> startedLock(_mpiThreadContext->_startedMutex);
    _mpiThreadContext->_startedCond.wait(startedLock);
    rank = _mpiThreadContext->_rank;
}

void CommunicationManager::wait() {
    _mpiThread->join();
    _mpiThread.reset();
}

std::string chunkFileName(const std::string& fileName, int chunk) {
   return fileName + ".chunk" + boost::lexical_cast<std::string>(chunk); 
}

void CommunicationManager::commonFilesBroadcastStep(const std::shared_ptr<CommunicationManager::MpiThreadContext>& mpiThreadContext, log4cpp::Category& logger) {

    boost::filesystem::path commonDir = powsybl::slave::commonDir(mpiThreadContext->_localDir, mpiThreadContext->_rank);

    while (true) {
        int length;
        if (MPI_Bcast(&length, 1, MPI_INT, 0, MPI_COMM_WORLD) != MPI_SUCCESS) {
            throw std::runtime_error("MPI_Bcast error");
        }
        // a special length message means the slave must change its state
        if (isSpecialLength(length)) {
            mpiThreadContext->_step = specialLength2step(length);
            break;
        }
        std::string buffer;
        buffer.resize(length);
        if (MPI_Bcast(&buffer[0], buffer.size(), MPI_BYTE, 0, MPI_COMM_WORLD) != MPI_SUCCESS) {
            throw std::runtime_error("MPI_Bcast error");
        }

        // decode the common file message
        messages::CommonFile commonFile;
        parseProtobufMessage(buffer, commonFile);

        // write common file into the common directory
        logger.debugStream() << "receiving chunk " << commonFile.chunk() << " of run scoped file '" << commonFile.name() << "'" << log4cpp::eol;

        {
            boost::filesystem::ofstream ofs(commonDir / chunkFileName(commonFile.name(), commonFile.chunk()) , std::ios::binary);
            ofs << commonFile.data();
        }

        // if last chunk concatenate everything
        if (commonFile.last()) {
            logger.debugStream() << "assembling the " << commonFile.chunk() << " chunks of run scoped file '" << commonFile.name() << "'" << log4cpp::eol;
            boost::filesystem::path concFile = commonDir / commonFile.name();
            {
                boost::filesystem::ofstream ofsConc(concFile, std::ios::binary);
                for (int i = 0; i <= commonFile.chunk(); i++) {
                    boost::filesystem::path chunkFile = commonDir / chunkFileName(commonFile.name(), i);
                    boost::filesystem::ifstream ifsChunk(chunkFile, std::ios::binary);    
                    ofsConc << ifsChunk.rdbuf();
                    boost::filesystem::remove(chunkFile);
                }                
            }
        }

        // unblock the master
        if (MPI_Barrier(MPI_COMM_WORLD) != MPI_SUCCESS) {
            throw std::runtime_error("MPI_Barrier error");
        }
    }
}

void CommunicationManager::tasksExecutionStep(const std::shared_ptr<CommunicationManager::MpiThreadContext>& mpiThreadContext, log4cpp::Category& logger) {

    mpiThreadContext->resetWorkerThreadsStopStatus();

    // create one worker thread per core
    std::thread workers[mpiThreadContext->_cores];
    for (int threadNum = 0; threadNum < mpiThreadContext->_cores; threadNum++) {
        workers[threadNum] = std::thread(CommunicationManager::workerThread, mpiThreadContext, threadNum);
    }
            
    std::chrono::system_clock::time_point t1 = std::chrono::system_clock::now();

    // listen to workers threads requests
    while (!mpiThreadContext->areAllWorkerThreadsStopped()) {
        bool sleep = true;

        if (mpiThreadContext->isAbortRequested()) {
            throw std::runtime_error("Abort requested by a worker thread");
        }

        {
            std::lock_guard<std::mutex> receivingContextsLock(mpiThreadContext->_receivingContextsMutex);

            if (mpiThreadContext->_receivingContexts.size() > 0) {
                for (std::vector<std::shared_ptr<CommunicationManager::WorkerThreadContext> >::iterator it = mpiThreadContext->_receivingContexts.begin();
                     it != mpiThreadContext->_receivingContexts.end();) {
                    const std::shared_ptr<CommunicationManager::WorkerThreadContext>& receivingContext = *it;
                    
                    bool remove = false;

                    if (receivingContext->_bufferRequest != MPI_REQUEST_NULL) {
                        // check if the data message has been received
                        int flag;
                        MPI_Status status;
                        if (MPI_Test(&receivingContext->_bufferRequest, &flag, &status) != MPI_SUCCESS) {
                            throw std::runtime_error("MPI_Test error (" + boost::lexical_cast<std::string>(status.MPI_ERROR) + ")");
                        }
                        if (flag) {
                            // if yes notify the worker thread...
                            {
                                std::unique_lock<std::mutex> lockCond(receivingContext->_mutexCond);
                                receivingContext->_cond.notify_one();
                            }
                            remove = true;
                        }
                    } else {
                        // receive length message
                        if (receivingContext->_lengthRequest == MPI_REQUEST_NULL) {
                            if (MPI_Irecv(&receivingContext->_length, 1, MPI_INT, 0, JOB_LENGTH_TAG + receivingContext->_threadNum, MPI_COMM_WORLD, &receivingContext->_lengthRequest) != MPI_SUCCESS) {
                                throw std::runtime_error("MPI_Irecv error");
                            }
                        }

                        // check if the length message has been received
                        int flag;
                        MPI_Status status;
                        if (MPI_Test(&receivingContext->_lengthRequest, &flag, &status) != MPI_SUCCESS) {
                            throw std::runtime_error("MPI_Test error (" + boost::lexical_cast<std::string>(status.MPI_ERROR) + ")");
                        }
                        if (flag) {
                            // length message has been received
                            if (isSpecialLength(receivingContext->_length)) {
                                
                                mpiThreadContext->_step = specialLength2step(receivingContext->_length);

                                // a special length message means slave must change its state, so stop worker threads
                                mpiThreadContext->requestWorkerThreadStop(receivingContext->_threadNum);

                                // notify the worker thread
                                {
                                    std::unique_lock<std::mutex> lockCond(receivingContext->_mutexCond);
                                    receivingContext->_cond.notify_one();
                                }
                                remove = true;
                            } else {
                                // receive the data message
                                receivingContext->_buffer.resize(receivingContext->_length);
                                if (MPI_Irecv(&(receivingContext->_buffer[0]), receivingContext->_length, MPI_BYTE, 0, JOB_BUFFER_TAG + receivingContext->_threadNum, MPI_COMM_WORLD, &receivingContext->_bufferRequest) != MPI_SUCCESS) {
                                    throw std::runtime_error("MPI_Recv error");
                                }
                            }
                        }
                    }

                    if (remove) {
                        it = mpiThreadContext->_receivingContexts.erase(it);
                    } else {
                        it++;
                    }
                }

                sleep = false;
            }
        }

        {
            std::lock_guard<std::mutex> sendingContextsLock(mpiThreadContext->_sendingContextsMutex);

            if (mpiThreadContext->_sendingContexts.size() > 0) {
                for (std::vector<std::shared_ptr<CommunicationManager::WorkerThreadContext> >::iterator it = mpiThreadContext->_sendingContexts.begin();
                     it != mpiThreadContext->_sendingContexts.end();) {
                    const std::shared_ptr<CommunicationManager::WorkerThreadContext>& sendingContext = *it;

                    bool remove = false;

                    // send the length and data message
                    if (sendingContext->_bufferRequest == MPI_REQUEST_NULL) {
                        sendingContext->_length = sendingContext->_buffer.size();
                        if (MPI_Isend(&sendingContext->_length, 1, MPI_INT, 0, JOB_RESULT_LENGTH_TAG + sendingContext->_threadNum, MPI_COMM_WORLD, &sendingContext->_lengthRequest) != MPI_SUCCESS) {
                            throw std::runtime_error("MPI_Isend error");
                        }
                        if (MPI_Isend((void*) &(sendingContext->_buffer[0]), sendingContext->_buffer.size(), MPI_BYTE, 0, JOB_RESULT_BUFFER_TAG + sendingContext->_threadNum, MPI_COMM_WORLD, &sendingContext->_bufferRequest) != MPI_SUCCESS) {
                            throw std::runtime_error("MPI_Isend error");
                        }
                    } else {
                        int flag;
                        MPI_Status status;
                        // check that data message sent is complete
                        if (MPI_Test(&sendingContext->_bufferRequest, &flag, &status) != MPI_SUCCESS) {
                            throw std::runtime_error("MPI_Test error (" + boost::lexical_cast<std::string>(status.MPI_ERROR) + ")");
                        }
                        if (flag) {
                            // if data message sent is complete, length message is too, call MPI_Wait to avoid memory leak
                            if (MPI_Wait(&sendingContext->_lengthRequest, &status) != MPI_SUCCESS) {
                                throw std::runtime_error("MPI_Wait error (" + boost::lexical_cast<std::string>(status.MPI_ERROR) + ")");
                            }
                            // notify the worker thread
                            {
                                std::unique_lock<std::mutex> lockCond(sendingContext->_mutexCond);
                                sendingContext->_cond.notify_one();
                            }
                            remove = true;
                        }
                    }

                    if (remove) {
                        it = mpiThreadContext->_sendingContexts.erase(it);
                    } else {
                        it++;
                    }
                }
                
                sleep = false;
            }
        }

        if (sleep) {
            std::chrono::system_clock::time_point t2 = std::chrono::system_clock::now();
            float durationInS = std::chrono::duration_cast<std::chrono::seconds>(t2 - t1).count();
            if (durationInS > 30 * 60) { // every 30 minutes
                t1 = std::chrono::system_clock::now();
                boost::filesystem::space_info space = boost::filesystem::space(mpiThreadContext->_localDir);
                logger.infoStream() << space.available / 1024 << "k/" << space.capacity / 1024 
                                    << "k available on " << mpiThreadContext->_localDir.string() 
                                    << log4cpp::eol;                
            }
            usleep(1000* TIMEOUT);
        }
    }

    // master/slaves synchronization
    if (MPI_Barrier(MPI_COMM_WORLD) != MPI_SUCCESS) {
        throw std::runtime_error("MPI_Barrier error");
    }

    // remove job directories
    {
        std::lock_guard<std::mutex> initializedJobsLock(mpiThreadContext->_initializedJobsMutex);
        for (std::set<int>::const_iterator it = mpiThreadContext->_initializedJobs.begin();
             it != mpiThreadContext->_initializedJobs.end();
             it++) {
            int jobId = *it;
            boost::filesystem::path jobDir = powsybl::slave::jobDir(mpiThreadContext->_localDir, mpiThreadContext->_rank, jobId); 
            boost::filesystem::remove_all(jobDir);            
        }
        mpiThreadContext->_initializedJobs.clear();
    }

    for (int threadNum = 0; threadNum < mpiThreadContext->_cores; threadNum++) {
        workers[threadNum].join();
    }
}

void CommunicationManager::mpiThread(const std::shared_ptr<CommunicationManager::MpiThreadContext>& mpiThreadContext) {
    try {
        powsybl::mpi::initThreadFunneled();

        // OpenMPI bug... unset SIGCHLD handler
        signal(SIGCHLD, SIG_DFL);

        if (MPI_Comm_rank(MPI_COMM_WORLD, &(mpiThreadContext->_rank)) != MPI_SUCCESS) {
            throw std::runtime_error("MPI_Comm_rank error");
        }
        if (mpiThreadContext->_rank == 0) {
            throw std::runtime_error("Slave (computation manager) must have MPI rank != 0");
        }

        log4cpp::Category& logger = createLogger(mpiThreadContext->_rank, -1, mpiThreadContext->_verbose);

        boost::filesystem::space_info space = boost::filesystem::space(mpiThreadContext->_localDir); 
        logger.infoStream() << "started on " << powsybl::mpi::processorName() << " (" << space.available / 1024 << "k/" << space.capacity / 1024 
                            << "k available on " << mpiThreadContext->_localDir.string() << ")" << log4cpp::eol;

        // start synchronization
        if (MPI_Barrier(MPI_COMM_WORLD) != MPI_SUCCESS) {
            throw std::runtime_error("MPI_Barrier error");
        }

        {
            std::unique_lock<std::mutex> startedLock(mpiThreadContext->_startedMutex);
            mpiThreadContext->_startedCond.notify_one();            
        }

        while (mpiThreadContext->_step != Step::SHUTDOWN) {
            switch (mpiThreadContext->_step) {
                case Step::COMMON_FILES_BCAST:
                    logger.debugStream() << "switch to " << str(Step::COMMON_FILES_BCAST) << log4cpp::eol;
                    // start common files broadcast step
                    commonFilesBroadcastStep(mpiThreadContext, logger);
                    break;
                case Step::TASKS_EXECUTION:
                    logger.debugStream() << "switch to " << str(Step::TASKS_EXECUTION) << log4cpp::eol;
                    // start tasks execution step
                    tasksExecutionStep(mpiThreadContext, logger);
                    break;
                case Step::SHUTDOWN:
                    // nothing to do
                    break;
                default:
                    throw std::runtime_error("Unknown step");
            } 
        }

        logger.debugStream() << "switch to " << str(Step::SHUTDOWN) << log4cpp::eol;

        if (MPI_Finalize() != MPI_SUCCESS) {
            throw std::runtime_error("MPI_Finalize error");
        }

        logger.debugStream() << "stopped" << log4cpp::eol;

    } catch (const std::exception& e) {
        log4cpp::Category::getRoot().fatalStream() << e.what() << log4cpp::eol;
        MPI_Abort(MPI_COMM_WORLD, -1);
    } catch (...) {
        log4cpp::Category::getRoot().fatalStream() << "Unknown exception" << log4cpp::eol;
        MPI_Abort(MPI_COMM_WORLD, -1);
    }
}

void CommunicationManager::workerThread(const std::shared_ptr<CommunicationManager::MpiThreadContext>& mpiThreadContext, int threadNum) {
    try {
        log4cpp::Category& logger = createLogger(mpiThreadContext->_rank, threadNum, mpiThreadContext->_verbose);

        boost::filesystem::path commonDir = powsybl::slave::commonDir(mpiThreadContext->_localDir, mpiThreadContext->_rank);

        boost::filesystem::path workingDir = powsybl::slave::workingDir(mpiThreadContext->_localDir, mpiThreadContext->_rank, threadNum);
        boost::filesystem::remove_all(workingDir);
        boost::filesystem::create_directory(workingDir);

        std::shared_ptr<CommunicationManager::WorkerThreadContext> receivingContext(new CommunicationManager::WorkerThreadContext(threadNum));
        std::shared_ptr<CommunicationManager::WorkerThreadContext> sendingContext(new CommunicationManager::WorkerThreadContext(threadNum)); 
        while (mpiThreadContext->receiveFromMaster(receivingContext)) {
            std::chrono::system_clock::time_point t1 = std::chrono::system_clock::now();

            // decode the job message
            messages::Task task;
            parseProtobufMessage(receivingContext->_buffer, task);

            // remove completed job directories
            for (int i = 0; i < task.completedjobid_size(); i++) {
                int jobId = task.completedjobid(i);
                boost::filesystem::path jobDir = powsybl::slave::jobDir(mpiThreadContext->_localDir, mpiThreadContext->_rank, jobId); 
                boost::filesystem::remove_all(jobDir);
            }

            // create a directory to store job scoped files
            boost::filesystem::path jobDir = powsybl::slave::jobDir(mpiThreadContext->_localDir, mpiThreadContext->_rank, task.jobid()); 
            if (task.initjob()) {
                boost::filesystem::remove_all(jobDir);
                boost::filesystem::create_directory(jobDir);
                for (int i = 0; i < task.inputfile_size(); i++) {
                    messages::Task_InputFile inputFile = task.inputfile(i);
                    if (inputFile.scope() == messages::Task_InputFile_Scope_JOB) {
                        logger.debugStream() << "receiving job scoped file '" << inputFile.name() << "'" << log4cpp::eol;
                        boost::filesystem::path jobLevelFile = jobDir / inputFile.name();
                        {
                            boost::filesystem::ofstream ofs(jobLevelFile, std::ios::binary);
                            ofs << inputFile.data();
                        }
                    }
                }
                mpiThreadContext->notifyJobInitialization(task.jobid());
            } else {
                mpiThreadContext->waitForJobInitialization(task.jobid());                
            }

            // write input files into the working directory
            for (int i = 0; i < task.inputfile_size(); i++) {
                messages::Task::InputFile inputFile = task.inputfile(i);
                switch (inputFile.scope()) {
                    case messages::Task_InputFile_Scope_RUN:
                        {
                            boost::filesystem::path path = commonDir / inputFile.name();
                            if (boost::filesystem::exists(path)) {
                                createSymbLinkToInputFile(inputFile.preprocessor(), path, workingDir);
                            } else {
                                logger.errorStream() << "Run scoped file '" << path.string() << "' not found" << log4cpp::eol;
                            }
                        }
                        break;

                    case  messages::Task_InputFile_Scope_JOB:
                        {
                            boost::filesystem::path path = jobDir / inputFile.name();
                            if (boost::filesystem::exists(path)) {
                                createSymbLinkToInputFile(inputFile.preprocessor(), path, workingDir);
                            } else {
                                logger.errorStream() << "Job scoped file '" << path.string() << "' not found" << log4cpp::eol;
                            }
                        }
                        break;

                    case messages::Task_InputFile_Scope_TASK:
                        logger.debugStream() << "receiving task scoped file '" << inputFile.name() << "'" << log4cpp::eol;
                        writeInputFile(inputFile, workingDir);
                        break;

                    default:
                        throw std::runtime_error("Unexpected input file scope");
                }
            }

            // update environment variables
            std::map<std::string, std::string> env = powsybl::io::getEnv();
            std::map<std::string, std::string> env2;
            env2["PATH"] = env["PATH"];
            env2["LD_LIBRARY_PATH"] = env["LD_LIBRARY_PATH"];
            for (int i = 0; i < task.env().variable_size(); i++) {
                messages::Task::Variable variable = task.env().variable(i);
                if (variable.name() == "PATH" || variable.name() == "LD_LIBRARY_PATH") {
                    env2[variable.name()] = variable.value() + ":" + env2[variable.name()];
                } else {
                    env2[variable.name()] = variable.value();
                }
            }

            // because some programs rely on it and in case if it not defined used /tmp by default
            env2["TMPDIR"] = workingDir.string();

            // run commands
            int exitCode = 0;
            std::vector<float> commandsDuration;
            commandsDuration.reserve(task.command_size());
            std::string stdOutFileName = task.cmdid() + "_" + boost::lexical_cast<std::string>(task.index()) + ".out";
            boost::filesystem::path stdOutPath = workingDir / stdOutFileName;
            for (int i = 0; i < task.command_size(); i++) {
                messages::Task::Command command = task.command(i);
                std::string cmdStr = command.program();
                for (int j = 0; j < command.argument_size(); j++) {
                    cmdStr += " ";
                    cmdStr += command.argument(j);
                }
                int timeout = command.has_timeout() ? command.timeout() : -1;
                logger.debugStream() << "executing '" << cmdStr << "' with a " << timeout << "s timeout" << log4cpp::eol;
                std::chrono::system_clock::time_point t3 = std::chrono::system_clock::now();
                exitCode = powsybl::io::systemSafe(cmdStr, workingDir, env2, stdOutPath, timeout, logger);
                std::chrono::system_clock::time_point t4 = std::chrono::system_clock::now();
                std::chrono::duration<float> commandDuration = std::chrono::duration_cast<std::chrono::duration<float>>(t4 - t3);
                commandsDuration.push_back(commandDuration.count());
                if (exitCode != 0) {
                    break;
                }
            }

            if (exitCode != 0 && !mpiThreadContext->_archiveDir.empty()) {
                std::string archiveName = "job-" + boost::lexical_cast<std::string>(task.jobid());
                boost::filesystem::path archiveZipFile = mpiThreadContext->_archiveDir / (archiveName + ".zip");
                powsybl::io::zipDir(workingDir, archiveZipFile, archiveName);
            }

            // create a result message
            messages::TaskResult result;
            result.set_exitcode(exitCode);
            
            // compute working directory size
            size_t workingDataSize = powsybl::io::dirSize(workingDir);
            result.set_workingdatasize(workingDataSize);

            // pack standard output in the result message
            if (boost::filesystem::exists(stdOutPath)) {
                std::string stdOutGzFileName = stdOutFileName + ".gz";
                logger.debugStream() << "sending file '" << stdOutGzFileName << "'" << log4cpp::eol;
                messages::TaskResult::OutputFile* stdOutFile = result.add_outputfile();
                stdOutFile->set_name(stdOutGzFileName);
                stdOutFile->set_data(powsybl::io::gzipMem(stdOutPath, logger));
            }
            // pack output files in the result message
            for (int i = 0; i < task.outputfile_size(); i++) {
                const messages::Task::OutputFile& outputFile = task.outputfile(i);
                boost::filesystem::path path = workingDir / outputFile.name();
                if (boost::filesystem::exists(path)) {
                    std::string outputFileName;
                    std::string outputFileData;
                    switch (outputFile.postprocessor()) {
                        case messages::Task_OutputFile_PostProcessor_NONE:
                            outputFileName = outputFile.name();
                            outputFileData = powsybl::io::readFile(path);
                            break;
                        case messages::Task_OutputFile_PostProcessor_FILE_GZIP:
                            outputFileName = outputFile.name() + ".gz";
                            outputFileData = powsybl::io::gzipMem(path, logger);
                            break;
                        default:
                            throw std::runtime_error("Unknown pre-processor");
                    }

                    logger.debugStream() << "sending file '" << outputFileName << "'" << log4cpp::eol;

                    messages::TaskResult::OutputFile* resultOutputFile = result.add_outputfile();
                    resultOutputFile->set_name(outputFileName);
                    resultOutputFile->set_data(outputFileData);
                } else {
                    if (exitCode == 0) {
                        logger.errorStream() << "output file '" << outputFile.name() << "' not found" << log4cpp::eol;
                    }
                }
            }
            for (std::vector<float>::const_iterator it = commandsDuration.begin();
                 it != commandsDuration.end();
                 it++) {
                result.add_commandduration((long) (*it * std::pow(10, 3))); // in ms
            }
            std::chrono::system_clock::time_point t2 = std::chrono::system_clock::now();
            std::chrono::duration<float> taskDuration = std::chrono::duration_cast<std::chrono::duration<float>>(t2 - t1);
            result.set_taskduration((long) (taskDuration.count() * std::pow(10, 3))); // in ms

            // encode the result message
            std::ostringstream output(std::ios::binary);
            if (!result.SerializeToOstream(&output)) {
                throw std::runtime_error("Failed to serialize result");
            }

            // send back the result message to the master
            sendingContext->_buffer = output.str();
            mpiThreadContext->sendToMaster(sendingContext);

            // clean working dir
            powsybl::io::cleanDir(workingDir);
        }

        boost::filesystem::remove_all(workingDir);

        mpiThreadContext->workerThreadStopped(threadNum);

    } catch (const std::exception& e) {
        log4cpp::Category::getRoot().fatalStream() << e.what() << log4cpp::eol;
        mpiThreadContext->requestAbort();
    } catch (...) {
        log4cpp::Category::getRoot().fatalStream() << "Unknown exception" << log4cpp::eol;
        mpiThreadContext->requestAbort();
    }
}

}

}

int main(int argc, char *argv[]) {
    try {     
        GOOGLE_PROTOBUF_VERIFY_VERSION;

        log4cpp::Category::getRoot().setPriority(log4cpp::Priority::ERROR);
        log4cpp::Appender* consoleAppender = powsybl::io::createConsoleLogAppender();
        consoleAppender->setThreshold(log4cpp::Priority::ERROR);
        log4cpp::Category::getRoot().addAppender(consoleAppender);

        boost::program_options::options_description desc("Allowed options");
        desc.add_options()
            ("tmp-dir", boost::program_options::value<std::string>(), "local temporary directory")
            ("archive-dir", boost::program_options::value<std::string>(), "archive directory to store failing jobs")
            ("verbose", "verbose mode")
            ("log-file", boost::program_options::value<std::string>(), "log file")
            ("cores", boost::program_options::value<int>(), "number of cores");

        boost::program_options::variables_map vm;
        boost::program_options::store(boost::program_options::parse_command_line(argc, argv, desc), vm);
        boost::program_options::notify(vm);    
        if (!vm.count("tmp-dir")) {
            throw std::runtime_error("tmp-dir option is not set");
        }
        boost::filesystem::path localDir(vm["tmp-dir"].as<std::string>());
        if (!boost::filesystem::exists(localDir) || !boost::filesystem::is_directory(localDir)) {
            throw std::runtime_error("local directory " + localDir.string() + " does not exist or is not a directory");
        }

        boost::filesystem::path archiveDir;
        if (vm.count("archive-dir")) {
            archiveDir = boost::filesystem::path(vm["archive-dir"].as<std::string>());
            if (!boost::filesystem::exists(archiveDir) || !boost::filesystem::is_directory(archiveDir)) {
                throw std::runtime_error("archive directory " + archiveDir.string() + " does not exist or is not a directory");
            }
        }

        bool verbose = vm.count("verbose");
        if (verbose) {
            log4cpp::Category::getRoot().setPriority(log4cpp::Priority::DEBUG);
        }

        if (!vm.count("cores")) {
            throw std::runtime_error("cores option is not set");
        }
        int cores = vm["cores"].as<int>();

        if (vm.count("log-file")) {
            std::string logFile = vm["log-file"].as<std::string>();
            log4cpp::Category::getRoot().addAppender(powsybl::io::createFileLogAppender(logFile));
        }

        std::shared_ptr<powsybl::slave::CommunicationManager> manager(new powsybl::slave::CommunicationManager(cores, localDir, verbose, archiveDir));
        int rank;
        manager->start(rank);

        boost::filesystem::path commonDir = powsybl::slave::commonDir(localDir, rank);
        boost::filesystem::remove_all(commonDir);
        boost::filesystem::create_directory(commonDir);

        manager->wait();

        boost::filesystem::remove_all(commonDir);

        google::protobuf::ShutdownProtobufLibrary();

    } catch (const std::exception& e) {
        log4cpp::Category::getRoot().fatalStream() << e.what() << log4cpp::eol;
        return -1;
    } catch (...) {
        log4cpp::Category::getRoot().fatalStream() << "Unknown exception" << log4cpp::eol;
        return -1;
    }

    return 0;
}

