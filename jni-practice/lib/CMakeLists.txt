cmake_minimum_required (VERSION 3.8)

project ("demo_jni")

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include_directories(./ $ENV{JAVA_HOME}/include/  $ENV{JAVA_HOME}/include/linux/)

AUX_SOURCE_DIRECTORY(. SRC_LIST)

ADD_LIBRARY(demo_jni SHARED ${SRC_LIST})

TARGET_LINK_LIBRARIES(demo_jni)