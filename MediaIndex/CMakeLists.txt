project(MediaIndex)
cmake_minimum_required(VERSION 2.6)
include(FindPkgConfig)

# Find includes in corresponding build directories
set(CMAKE_INCLUDE_CURRENT_DIR ON)
# Instruct CMake to run moc automatically when needed.
set(CMAKE_AUTOMOC ON)

find_package(Qt5Core REQUIRED)
find_package(Boost REQUIRED COMPONENTS filesystem system)

pkg_search_module(TAGLIB REQUIRED taglib>=1.7)
pkg_search_module(SQLITE REQUIRED sqlite3>=3.7)

include_directories(${QT_INCLUDES} ${Boost_INCLUDE_DIRS} ${TAGLIB_INCLUDE_DIRS} ${SQLITE_INCLUDE_DIRS} ${CMAKE_CURRENT_BINARY_DIR})

set(MediaIndex_SRCS DbCoverIterator.cpp DbPathIterator.cpp Covers.cpp Updater.cpp Database.cpp ProgressOutput.cpp Indexer.cpp DirScanner.cpp main.cpp Stats.cpp BlockingQueue.cpp)

add_executable(mediaindex ${MediaIndex_SRCS})
target_link_libraries(mediaindex Qt5::Core ${Boost_LIBRARIES} ${TAGLIB_LIBRARIES} ${SQLITE_LIBRARIES})
