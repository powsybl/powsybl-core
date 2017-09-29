// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file master.cpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#include <mpi.h>
#include <iostream>
#include <stdexcept>
#include <memory>
#include <mutex>
#include <thread>
#include <jni.h>
#include <unistd.h>
#include <boost/lexical_cast.hpp>
#include <log4cpp/Category.hh>
#include <log4cpp/Priority.hh>
#include "jniutil.hpp"
#include "mpitags.hpp"
#include "mpiutil.hpp"
#include "ioutil.hpp"

log4cpp::Category& LOGGER = log4cpp::Category::getInstance("Master");

namespace powsybl {

namespace master {

struct SendContext {
    SendContext(const std::string& sendBuffer) 
      : _length(sendBuffer.size()),
        _lengthRequest(MPI_REQUEST_NULL),
        _buffer(sendBuffer),
        _bufferRequest(MPI_REQUEST_NULL)
    {}
    SendContext(const SendContext&) = delete;
    SendContext& operator=(const SendContext&) = delete;

    int _length;
    MPI_Request _lengthRequest;
    std::string _buffer;
    MPI_Request _bufferRequest;  
};

struct TaskContext {
    TaskContext(const std::string& sendBuffer) 
      : _send(sendBuffer),
        _receiveLengthRequest(MPI_REQUEST_NULL), 
        _receiveBufferRequest(MPI_REQUEST_NULL) 
    {}
    TaskContext(const TaskContext&) = delete;
    TaskContext& operator=(const TaskContext&) = delete;

    SendContext _send;

    int _receiveLength;
    MPI_Request _receiveLengthRequest;
    std::string _receiveBuffer;
    MPI_Request _receiveBufferRequest;  
};

class CommunicationManager {
public:
    CommunicationManager(int coresPerRank);
    CommunicationManager(const CommunicationManager&) = delete;
    CommunicationManager& operator=(const CommunicationManager&) = delete;
    ~CommunicationManager();

    void init();
    void shutdown();

    int commRank();
    int commSize();
    const std::string& mpiVersion();
    void startTasks(const powsybl::jni::JavaUtilList& tasks);
    void checkTasksCompletion(const powsybl::jni::JavaUtilList& runningTasks, const powsybl::jni::JavaUtilList& completedTasks);
    void broadcastCommonFile(const powsybl::jni::ByteArray& file);

private:
    void checkThread() const;
    void ensureStep(Step step);
    void checkStep(Step step);
    void checkStep(Step step1, Step step2);

    const int _coresPerRank;
    std::thread::id _threadId;
    int _commRank;
    int _commSize;
    std::string _mpiVersion;
    Step _step;
    std::map<int, std::shared_ptr<TaskContext> > _taskContexts;
};

CommunicationManager::CommunicationManager(int coresPerRank) 
    : _coresPerRank(coresPerRank),
      _step(Step::SHUTDOWN) {
}

CommunicationManager::~CommunicationManager() {    
}

void CommunicationManager::checkThread() const {
    // if (_threadId != std::this_thread::get_id()) {
    //     throw std::runtime_error("All MPI functions must be called from the same thread");
    // }
}

void CommunicationManager::init() {
    _threadId = std::this_thread::get_id();

    _step = Step::COMMON_FILES_BCAST;

    if (_coresPerRank > MAX_CORES_PER_RANK) {
        throw std::runtime_error("Too many cores per rank");
    }

    LOGGER.debugStream() << "starting " << log4cpp::eol;
    
    powsybl::mpi::initThreadFunneled();
    if (MPI_Comm_rank(MPI_COMM_WORLD, &_commRank) != MPI_SUCCESS) {
        throw std::runtime_error("MPI_Comm_rank error");
    }
    if (MPI_Comm_size(MPI_COMM_WORLD, &_commSize) != MPI_SUCCESS) {
        throw std::runtime_error("MPI_Comm_size error");
    }

    _mpiVersion = powsybl::mpi::version();

    LOGGER.debugStream() << "started on " << powsybl::mpi::processorName() 
                         << ", communicator size: " << _commSize << log4cpp::eol;

    LOGGER.debugStream() << "waiting for slaves to start" << log4cpp::eol;
    if (MPI_Barrier(MPI_COMM_WORLD) != MPI_SUCCESS) {
        throw std::runtime_error("MPI_Barrier error");
    }
}

void CommunicationManager::shutdown() {
    checkThread();

    LOGGER.debugStream() << "stopping" << log4cpp::eol;

    ensureStep(Step::SHUTDOWN);
    
    if (MPI_Finalize() != MPI_SUCCESS) {
        throw std::runtime_error("MPI_Finalize error");
    }

    LOGGER.debugStream() << "stopped" << log4cpp::eol;
}

int CommunicationManager::commRank() { 
    checkStep(Step::COMMON_FILES_BCAST, Step::TASKS_EXECUTION);
    return _commRank;
}

int CommunicationManager::commSize() {
    checkStep(Step::COMMON_FILES_BCAST, Step::TASKS_EXECUTION);
    return _commSize;
}

const std::string& CommunicationManager::mpiVersion() { 
    checkStep(Step::COMMON_FILES_BCAST, Step::TASKS_EXECUTION);
    return _mpiVersion;
}

void CommunicationManager::ensureStep(Step nextStep) {
    checkThread();

    if (nextStep == _step) {
        return;
    }
    
    LOGGER.debugStream() << "switch slaves to " << str(nextStep) << log4cpp::eol;

    switch (_step) {
        case Step::COMMON_FILES_BCAST:
            switch (nextStep) {
                case Step::TASKS_EXECUTION:
                case Step::SHUTDOWN: {
                        // broadcast a special length message to switch slaves state
                        int length = step2specialLength(nextStep);
                        if (MPI_Bcast(&length, 1, MPI_INT, 0, MPI_COMM_WORLD) != MPI_SUCCESS) {
                            throw std::runtime_error("MPI_Bcast error");
                        }
                    }
                    break;

                default:
                    throw std::runtime_error("Invalid next step " + str(nextStep) 
                        + ", expected " + str(Step::TASKS_EXECUTION) + "|" + str(Step::SHUTDOWN));
            }
            break;

        case Step::TASKS_EXECUTION:
            switch (nextStep) {
                case Step::COMMON_FILES_BCAST:
                case Step::SHUTDOWN: {
                        // send a special length message to switch all slaves state
                        int length = step2specialLength(nextStep);
                        for (int rank = 1; rank < _commSize; rank++) {
                            for (int thread = 0; thread < _coresPerRank; thread++) {
                                if (MPI_Send(&length, 1, MPI_INT, rank, JOB_LENGTH_TAG + thread, MPI_COMM_WORLD) != MPI_SUCCESS) {
                                    throw std::runtime_error("MPI_Send error");
                                }
                            }
                        }
                    }

                    // master/slaves synchronization
                    if (MPI_Barrier(MPI_COMM_WORLD) != MPI_SUCCESS) {
                        throw std::runtime_error("MPI_Barrier error");
                    }
                    break;

                default:
                    throw std::runtime_error("Invalid next step " + str(nextStep) 
                        + ", expected " + str(Step::COMMON_FILES_BCAST) + "|" + str(Step::SHUTDOWN));
            }
            break;

        default: // cannot move from an other step
            throw std::runtime_error("Cannot move from step " + str(_step));
    }
    _step = nextStep;
}

void CommunicationManager::checkStep(Step step) {
    if (_step != step) {
        throw std::runtime_error("Unexpected step " + str(_step));
    }
}

void CommunicationManager::checkStep(Step step1, Step step2) {
    if (_step != step1 && _step != step2) {
        throw std::runtime_error("Unexpected step " + str(_step));
    }
}

void CommunicationManager::startTasks(const powsybl::jni::JavaUtilList& tasks) {
    checkThread();

    ensureStep(Step::TASKS_EXECUTION);

    for (size_t i = 0; i < tasks.size() ; i++) {
        powsybl::jni::ComPowsyblComputationMpiMpiTask task(tasks.env(), tasks.get(i));
        int taskId = task.id();
        int rank = task.rank();
        int thread = task.thread();
        powsybl::jni::ByteArray message(tasks.env(), task.message());

        // create job mpi context
        std::shared_ptr<TaskContext> context(new TaskContext(std::string(message.get(), message.length())));
        _taskContexts[taskId] = context;

        if (MPI_Isend(&context->_send._length, 1, MPI_INT, rank, JOB_LENGTH_TAG + thread, MPI_COMM_WORLD, &context->_send._lengthRequest) != MPI_SUCCESS) {
            throw std::runtime_error("MPI_Isend error");
        }
        if (MPI_Isend(&(context->_send._buffer[0]), context->_send._length, MPI_BYTE, rank, JOB_BUFFER_TAG + thread, MPI_COMM_WORLD, &context->_send._bufferRequest) != MPI_SUCCESS) {
            throw std::runtime_error("MPI_Isend error");
        }
        if (MPI_Irecv(&context->_receiveLength, 1, MPI_INT, rank, JOB_RESULT_LENGTH_TAG + thread, MPI_COMM_WORLD, &context->_receiveLengthRequest) != MPI_SUCCESS) {
            throw std::runtime_error("MPI_Irecv error");
        }
    }
}

void CommunicationManager::checkTasksCompletion(const powsybl::jni::JavaUtilList& runningTasks, const powsybl::jni::JavaUtilList& completedTasks) {
    checkThread();

    if (runningTasks.size() == 0) {
        return;
    }

    checkStep(Step::TASKS_EXECUTION);

    for (size_t i = 0; i < runningTasks.size() ; i++) {
        powsybl::jni::ComPowsyblComputationMpiMpiTask task(runningTasks.env(), runningTasks.get(i));
        int taskId = task.id();
        std::shared_ptr<TaskContext> context = _taskContexts[taskId];
        int flag;
        MPI_Status status;
        
        if (context->_receiveLengthRequest != MPI_REQUEST_NULL) {
            if (MPI_Test(&context->_receiveLengthRequest, &flag, &status) != MPI_SUCCESS) {
                throw std::runtime_error("MPI_Test error (" + boost::lexical_cast<std::string>(status.MPI_ERROR) + ")");
            }
            if (flag) {
                // receiving the result length message means, length and buffer have been sent.
                // we must wall an MPI_Wait to avoid memory leaks
                if (MPI_Wait(&context->_send._lengthRequest, &status) != MPI_SUCCESS) {
                    throw std::runtime_error("MPI_Wait error (" + boost::lexical_cast<std::string>(status.MPI_ERROR) + ")");
                }
                if (MPI_Wait(&context->_send._bufferRequest, &status) != MPI_SUCCESS) {
                    throw std::runtime_error("MPI_Wait error (" + boost::lexical_cast<std::string>(status.MPI_ERROR) + ")");
                }
                // receive the result message
                context->_receiveBuffer.resize(context->_receiveLength);
                int rank = task.rank();
                int thread = task.thread();     
                if (MPI_Irecv(&(context->_receiveBuffer[0]), context->_receiveLength, MPI_BYTE, rank, JOB_RESULT_BUFFER_TAG + thread, MPI_COMM_WORLD, &context->_receiveBufferRequest) != MPI_SUCCESS) {
                    throw std::runtime_error("MPI_Recv error");
                }
            }                
        } else if (context->_receiveBufferRequest != MPI_REQUEST_NULL) {
            if (MPI_Test(&context->_receiveBufferRequest, &flag, &status) != MPI_SUCCESS) {
                throw std::runtime_error("MPI_Test error (" + boost::lexical_cast<std::string>(status.MPI_ERROR) + ")");
            }
            if (flag) {
                // store the result in the job
                task.resultMessage(context->_receiveBuffer);

                completedTasks.add(task.obj());

                // destroy context
                _taskContexts.erase(taskId);
            }
        } else {
            throw std::runtime_error("Unexpected state");
        }
    }
}

void CommunicationManager::broadcastCommonFile(const powsybl::jni::ByteArray& file) {
    checkThread();

    ensureStep(Step::COMMON_FILES_BCAST);

    int length = file.length();
    const char* buffer = file.get();

    // broadcast the common file message to all slaves
    if (MPI_Bcast(&length, 1, MPI_INT, 0, MPI_COMM_WORLD) != MPI_SUCCESS) {
        throw std::runtime_error("MPI_Bcast error");
    }
    if (MPI_Bcast((void*) buffer, length, MPI_BYTE, 0, MPI_COMM_WORLD) != MPI_SUCCESS) {
        throw std::runtime_error("MPI_Bcast error");
    }

    // to wait for the slave to write file on local directory
    if (MPI_Barrier(MPI_COMM_WORLD) != MPI_SUCCESS) {
        throw std::runtime_error("MPI_Barrier error");
    }
}

}

}

#ifdef __cplusplus
extern "C" {
#endif

std::shared_ptr<powsybl::master::CommunicationManager> MANAGER;

/*
 * Class:     com.powsybl_computation_mpi_JniMpiNativeServices
 * Method:    initMpi
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_powsybl_computation_mpi_JniMpiNativeServices_initMpi(JNIEnv * env, jobject, jint coresPerRank, jboolean verbose) {
    try {
        LOGGER.setPriority(verbose ? log4cpp::Priority::DEBUG : log4cpp::Priority::ERROR);
        LOGGER.addAppender(powsybl::io::createConsoleLogAppender());

        MANAGER.reset(new powsybl::master::CommunicationManager(coresPerRank));
        MANAGER->init();
        if (MANAGER->commRank() != 0) {
            throw std::runtime_error("CommunicationManager (data manager) must have MPI rank 0");
        }

        powsybl::jni::JavaUtilList::init(env);
        powsybl::jni::ComPowsyblComputationMpiMpiTask::init(env);
    } catch (const std::exception& e) {
        LOGGER.fatalStream() << e.what() << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, e.what());
    } catch (...) {
        LOGGER.fatalStream() << "Unknown exception" << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, "Unknown exception");
    }
}

/*
 * Class:     com.powsybl_computation_mpi_JniMpiNativeServices
 * Method:    terminateMpi
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_powsybl_computation_mpi_JniMpiNativeServices_terminateMpi(JNIEnv * env, jobject) {
    try {
        MANAGER->shutdown();
        MANAGER.reset();
    } catch (const std::exception& e) {
        LOGGER.fatalStream() << e.what() << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, e.what());
    } catch (...) {
        LOGGER.fatalStream() << "Unknown exception" << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, "Unknown exception");
    }
}

/*
 * Class:     com.powsybl_computation_mpi_JniMpiNativeServices
 * Method:    getMpiVersion
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_powsybl_computation_mpi_JniMpiNativeServices_getMpiVersion(JNIEnv * env, jobject) {
    try {
        std::string version = MANAGER->mpiVersion();
        return env->NewStringUTF(version.c_str());
    } catch (const std::exception& e) {
        LOGGER.fatalStream() << e.what() << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, e.what());
    } catch (...) {
        LOGGER.fatalStream() << "Unknown exception" << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, "Unknown exception");
    }
    return NULL;
}

/*
 * Class:     com.powsybl_computation_mpi_JniMpiNativeServices
 * Method:    getMpiCommSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_com_powsybl_computation_mpi_JniMpiNativeServices_getMpiCommSize(JNIEnv * env, jobject) {
    try {
        return MANAGER->commSize();
    } catch (const std::exception& e) {
        LOGGER.fatalStream() << e.what() << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, e.what());
    } catch (...) {
        LOGGER.fatalStream() << "Unknown exception" << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, "Unknown exception");
    }
    return -1;
}

/*
 * Class:     com.powsybl_computation_mpi_JniMpiNativeServices
 * Method:    startTasks
 * Signature: (Ljava/util/List;)V
 */
JNIEXPORT void JNICALL Java_com_powsybl_computation_mpi_JniMpiNativeServices_startTasks(JNIEnv * env, jobject, jobject jtask) {
    try {
        powsybl::jni::JavaUtilList tasks(env, jtask);

        // send job messages
        MANAGER->startTasks(tasks);

    } catch (const std::exception& e) {
        LOGGER.fatalStream() << e.what() << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, e.what());
    } catch (...) {
        LOGGER.fatalStream() << "Unknown exception" << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, "Unknown exception");
    }
}

/*
 * Class:     com.powsybl_computation_mpi_JniMpiNativeServices
 * Method:    checkTasksCompletion
 * Signature: (Ljava/util/List;Ljava/util/List;)V
 */
JNIEXPORT void JNICALL Java_com_powsybl_computation_mpi_JniMpiNativeServices_checkTasksCompletion(JNIEnv * env, jobject, jobject jrunningtasks, jobject jcompletedtasks) {
    try {
        powsybl::jni::JavaUtilList runningTasks(env, jrunningtasks);
        powsybl::jni::JavaUtilList completedTasks(env, jcompletedtasks);

        // check tasks completion
        MANAGER->checkTasksCompletion(runningTasks, completedTasks);

    } catch (const std::exception& e) {
        LOGGER.fatalStream() << e.what() << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, e.what());
    } catch (...) {
        LOGGER.fatalStream() << "Unknown exception" << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, "Unknown exception");
    }
}

/*
 * Class:     com.powsybl_computation_mpi_JniMpiNativeServices
 * Method:    sendCommonFile
 * Signature: ([B)V
 */
JNIEXPORT void JNICALL Java_com_powsybl_computation_mpi_JniMpiNativeServices_sendCommonFile(JNIEnv * env, jobject, jbyteArray jfile) {
    try {
        powsybl::jni::ByteArray file(env, jfile);

        // broadcast the common file message to all slaves
        MANAGER->broadcastCommonFile(file);

    } catch (const std::exception& e) {
        LOGGER.fatalStream() << e.what() << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, e.what());
    } catch (...) {
        LOGGER.fatalStream() << "Unknown exception" << log4cpp::eol;
        powsybl::jni::throwJavaLangRuntimeException(env, "Unknown exception");
    }
}

#ifdef __cplusplus
}
#endif
