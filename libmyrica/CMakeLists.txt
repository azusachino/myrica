cmake_minimum_required (VERSION 3.16)

project ("myrica")

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include_directories(./ $ENV{JAVA_HOME}/include/  $ENV{JAVA_HOME}/include/linux/)

AUX_SOURCE_DIRECTORY(. SRC_LIST)

ADD_LIBRARY(myrica SHARED ${SRC_LIST})

# build target library
TARGET_LINK_LIBRARIES(myrica)