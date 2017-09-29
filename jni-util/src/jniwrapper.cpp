// Copyright (c) 2017, RTE (http://www.rte-france.com)
// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at http://mozilla.org/MPL/2.0/.

/**
 * @file jniwrapper.cpp
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */

#include "jniwrapper.hpp"

namespace powsybl {

namespace jni {

jmethodID JavaUtilList::_sizeMethodId = 0;
jmethodID JavaUtilList::_getMethodId = 0;
jmethodID JavaUtilList::_addMethodId = 0;

void JavaUtilList::init(JNIEnv* env) {
    jclass _class = (jclass) env->FindClass("java/util/List");
    _sizeMethodId = env->GetMethodID(_class, "size", "()I"); 
    _getMethodId = env->GetMethodID(_class, "get", "(I)Ljava/lang/Object;");
    _addMethodId = env->GetMethodID(_class, "add", "(Ljava/lang/Object;)Z");
}

void throwJavaLangRuntimeException(JNIEnv* env, const char* msg) {
    jclass clazz = env->FindClass("java/lang/RuntimeException");
    env->ThrowNew(clazz, msg);
}

}

}

