project(mediaconvert)
cmake_minimum_required(VERSION 2.6)
include(FindPkgConfig)

find_package(Qt4 REQUIRED)
pkg_search_module(GST REQUIRED gstreamer-1.0)

include_directories(${QT_INCLUDES} ${GST_INCLUDE_DIRS})

set(MediaConvert_SRCS main.cpp)

qt4_automoc(${MediaIndex_SRCS})

add_executable(mediaconvert ${MediaConvert_SRCS})
target_link_libraries(mediaconvert ${QT_QTCORE_LIBRARY} ${GST_LIBRARIES})