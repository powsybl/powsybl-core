// Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file jniutil.hpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#ifndef JNIUTIL_HPP
#define JNIUTIL_HPP

#include <cstring>
#include <string>
#include "jni.h"

namespace itesla {

namespace jni {

class JniWrapper {
public:
    JNIEnv* env() const { return _env; }
    jobject obj() const { return _obj; }

protected:
    JniWrapper(JNIEnv* env, jobject obj)
        : _env(env), _obj(obj) {
    }

    virtual ~JniWrapper() {
    }

    JNIEnv* _env;
    jobject _obj;
private:
    JniWrapper(const JniWrapper&);
    JniWrapper& operator=(const JniWrapper&);
};

class ObjectArray : public JniWrapper {
public:
    ObjectArray(JNIEnv* env, jobjectArray obj)
        : JniWrapper(env, obj) {
    }

    size_t length() const {
        return _env->GetArrayLength((jobjectArray) _obj);
    }

    jobject at(size_t i) const {
        return _env->GetObjectArrayElement((jobjectArray) _obj, i);
    }
};

class ByteArray : public JniWrapper {
public:
    ByteArray(JNIEnv* env, jbyteArray obj)
        : JniWrapper(env, obj),
          _ptr(_env->GetByteArrayElements(obj, &_iscopy)) {
    }

    ~ByteArray() {
        if (_iscopy == JNI_TRUE) {
            _env->ReleaseByteArrayElements((jbyteArray) _obj, _ptr, 0);
        }
    }

    size_t length() const {
        return _env->GetArrayLength((jbyteArray) _obj);
    }

    const char* get() const {
        return (const char*) _ptr;
    }

private:
    jboolean _iscopy;
    jbyte* _ptr;
};

class JavaUtilList : public JniWrapper {
public:
    JavaUtilList(JNIEnv* env, jobject obj)
       : JniWrapper(env, obj) {
    }

    static void init(JNIEnv* env);

    size_t size() const { return _env->CallIntMethod(_obj, _sizeMethodId); }
    jobject get(size_t i) const { return _env->CallObjectMethod(_obj, _getMethodId, i); }
    bool add(jobject e) const { return _env->CallBooleanMethod(_obj, _addMethodId, e); }

private:
    static jmethodID _sizeMethodId;   
    static jmethodID _getMethodId;
    static jmethodID _addMethodId; 
};

class EuIteslaProjectComputationMpiMpiTask : public JniWrapper {
public:
    EuIteslaProjectComputationMpiMpiTask(JNIEnv* env, jobject obj)
        : JniWrapper(env, obj) {
    }

    static void init(JNIEnv* env);

    int id() const;
    int rank() const;
    int thread() const;

    jbyteArray message() const;

    void resultMessage(const std::string& buffer);

private:
    static jmethodID _getIdMethodId;       
    static jmethodID _getRankMethodId;   
    static jmethodID _getThreadMethodId;   
    static jmethodID _getMessageMethodId;   
    static jmethodID _setResultMessageMethodId;   
};

void throwJavaLangRuntimeException(JNIEnv* env, const char* msg);

}

}

#endif // JNIUTIL_HPP

