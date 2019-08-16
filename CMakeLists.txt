####### original SC Cmake file starts here
cmake_minimum_required(VERSION 3.3)


set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_STANDARD 14)
set(CMAKE_CXX_EXTENSIONS OFF)

if(APPLE)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -stdlib=libc++ -mavx -msse -msse2 -msse3 -msse4")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -stdlib=libc++")
SET(CMAKE_XCODE_ATTRIBUTE_CLANG_CXX_LIBRARY "libc++")
set(CMAKE_OSX_DEPLOYMENT_TARGET "10.7" CACHE STRING "Minimum OS X deployment version")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -pedantic")
endif()

project (fluid_decomposition_supercollider LANGUAGES CXX)

option(SUPERNOVA "Build plugins for supernova" OFF)

MACRO(SUBDIRLIST result curdir)
  FILE(GLOB children RELATIVE ${curdir} ${curdir}/*)
  SET(dirlist "")
  FOREACH(child ${children})
    IF(IS_DIRECTORY ${curdir}/${child})
      LIST(APPEND dirlist ${child})
    ENDIF()
  ENDFOREACH()
  SET(${result} ${dirlist})
ENDMACRO()

include(FetchContent)

set(FETCHCONTENT_QUIET FALSE)

if(DEFINED FLUID_PATH)
get_filename_component(FLUID_PATH ${FLUID_PATH} ABSOLUTE)
message(${FLUID_PATH})
FetchContent_Declare(
  fluid_decomposition
  # URL ${FLUID_PATH}
  DOWNLOAD_COMMAND ""
  SOURCE_DIR "${FLUID_PATH}/"
)
else()
FetchContent_Declare(
  fluid_decomposition
  GIT_REPOSITORY https://bitbucket.org/flucoma/fluid_decomposition.git
  GIT_PROGRESS TRUE
)
endif()

if(DEFINED FLUID_M_PATH)
get_filename_component(FLUID_M_PATH ${FLUID_M_PATH} ABSOLUTE)
FetchContent_Declare(
  fluid_manipulation
  # URL ${FLUID_M_PATH}
  DOWNLOAD_COMMAND ""
  SOURCE_DIR "${FLUID_M_PATH}/"
)
else()
FetchContent_Declare(
  fluid_manipulation
  GIT_REPOSITORY https://bitbucket.org/flucoma/fluid_manipulation.git
  GIT_PROGRESS TRUE
)
endif()

FetchContent_GetProperties(fluid_decomposition)
if(NOT fluid_decomposition_POPULATED)
  FetchContent_Populate(fluid_decomposition)
  add_subdirectory(${fluid_decomposition_SOURCE_DIR} ${fluid_decomposition_BINARY_DIR})
endif()

FetchContent_GetProperties(fluid_manipulation)
if(NOT fluid_manipulation_POPULATED)
  FetchContent_Populate(fluid_manipulation)
  add_subdirectory(${fluid_manipulation_SOURCE_DIR} ${fluid_manipulation_BINARY_DIR})
endif()

if (NOT (EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/release-packaging/AudioFiles/"))
	file(MAKE_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/release-packaging/AudioFiles/")
endif()

file(COPY "${fluid_decomposition_SOURCE_DIR}/AudioFiles/" DESTINATION "${CMAKE_CURRENT_SOURCE_DIR}/release-packaging/AudioFiles/")

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/release-packaging/plugins")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY_DEBUG "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY_RELEASE "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")

set(CMAKE_SHARED_MODULE_PREFIX "")
if(APPLE OR WIN32)
set(CMAKE_SHARED_MODULE_SUFFIX ".scx")
endif()

#needed for complaint-free static linking with GCC
if(CMAKE_COMPILER_IS_GNUCXX)
target_compile_options( HISSTools_FFT PUBLIC -fPIC )
ENDIF(CMAKE_COMPILER_IS_GNUCXX)

add_library(FLUID_SC_WRAPPER INTERFACE)
target_sources(FLUID_SC_WRAPPER
INTERFACE
"${CMAKE_CURRENT_SOURCE_DIR}/include/FluidSCWrapper.hpp"
"${CMAKE_CURRENT_SOURCE_DIR}/include/SCBufferAdaptor.hpp"
)

SUBDIRLIST(PROJECT_DIRS "${CMAKE_CURRENT_SOURCE_DIR}/src")
foreach (project_dir ${PROJECT_DIRS})
	if (EXISTS "${CMAKE_CURRENT_SOURCE_DIR}/src/${project_dir}/CMakeLists.txt")
		message("Generating: ${project_dir}")
		add_subdirectory("${CMAKE_CURRENT_SOURCE_DIR}/src/${project_dir}")
	endif ()
endforeach ()
