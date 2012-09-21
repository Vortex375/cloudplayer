project(MediaIndex)
cmake_minimum_required(VERSION 2.6)
include(FindPkgConfig)

find_package(Qt4 REQUIRED)
find_package(Boost REQUIRED COMPONENTS filesystem system)
pkg_search_module(TAGLIB REQUIRED taglib>=1.7.2)

include_directories(${QT_INCLUDES} ${Boost_INCLUDE_DIRS} ${TAGLIB_INCLUDE_DIRS} ${CMAKE_CURRENT_BINARY_DIR})

set(MediaIndex_SRCS ProgressOutput.cpp Indexer.cpp DirScanner.cpp MediaIndex.cpp main.cpp Stats.cpp BlockingQueue.cpp)

qt4_automoc(${MediaIndex_SRCS})

add_executable(mediaindex ${MediaIndex_SRCS})
target_link_libraries(mediaindex ${QT_QTCORE_LIBRARY} ${Boost_LIBRARIES} ${TAGLIB_LIBRARIES})
