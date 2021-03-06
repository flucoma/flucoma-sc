# Copyright 2017-2019 University of Huddersfield.
# Licensed under the BSD-3 License.
# See license.md file in the project root for full license information.
# This project has received funding from the European Research Council (ERC)
# under the European Union’s Horizon 2020 research and innovation programme
# (grant agreement No 725899).

cmake_minimum_required(VERSION 3.11)

################################################################################
# Paths
set(CMAKE_INSTALL_PREFIX "${CMAKE_CURRENT_SOURCE_DIR}/install" CACHE PATH "")

set(SC_LIBRARY_OUTPUT_PREFIX "release-packaging" CACHE STRING "Where in the hierarchy to write sc plugins")

set(SC_PATH "" CACHE PATH "Path to the top of SuperCollider source tree")
if(NOT SC_PATH)
  message(FATAL_ERROR "SuperCollider source path is not set")
endif()

set(FLUID_PATH "" CACHE PATH "Optional path to the flucoma-core repo")

if (APPLE)
	set(CMAKE_OSX_ARCHITECTURES x86_64)
  set(CMAKE_XCODE_GENERATE_SCHEME ON)
  set(CMAKE_OSX_DEPLOYMENT_TARGET 10.8)
endif()

################################################################################
# Main project
project (flucoma-sc LANGUAGES CXX)
set_property(GLOBAL PROPERTY USE_FOLDERS ON)
set(CMAKE_XCODE_GENERATE_TOP_LEVEL_PROJECT_ONLY ON)

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

FetchContent_Declare(
  flucoma-core
  GIT_REPOSITORY https://github.com/flucoma/flucoma-core.git
  GIT_PROGRESS TRUE
)

if(FLUID_PATH)
  get_filename_component(
    FETCHCONTENT_SOURCE_DIR_FLUCOMA-CORE ${FLUID_PATH} ABSOLUTE
  )
endif()

FetchContent_GetProperties(flucoma-core)
if(NOT flucoma-core_POPULATED)
  FetchContent_Populate(flucoma-core)
  add_subdirectory(${flucoma-core_SOURCE_DIR} ${flucoma-core_BINARY_DIR})
  include(flucoma_version)
  include(flucoma-buildtools)
  include(flucoma-buildtype)
endif()

set_if_toplevel(VAR CMAKE_LIBRARY_OUTPUT_DIRECTORY 
                TOPLEVEL "${CMAKE_CURRENT_SOURCE_DIR}/release-packaging/plugins"
                SUPERBUILD "${CMAKE_SOURCE_DIR}/sc_plugins/${CMAKE_HOST_SYSTEM_NAME}/${CMAKE_HOST_SYSTEM_PROCESSOR}")

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY_DEBUG "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY_RELEASE "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")
set(CMAKE_SHARED_MODULE_PREFIX "")

if(APPLE OR WIN32)
  set(CMAKE_SHARED_MODULE_SUFFIX ".scx")
endif()

#needed for complaint-free static linking with GCC
if(CMAKE_COMPILER_IS_GNUCXX)
  target_compile_options( HISSTools_FFT PUBLIC -fPIC )
ENDIF()

add_library(FLUID_SC_WRAPPER INTERFACE)
target_include_directories(FLUID_SC_WRAPPER 
  INTERFACE
  "${CMAKE_CURRENT_SOURCE_DIR}/include/"
)
  
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

#install bits. 

set(SC_INSTALL_PREFIX "." CACHE PATH "Prefix for assembling SC packages")
set(FLUID_PACKAGE_NAME FluidCorpusManipulation CACHE STRING "Name for published package")
set(SC_PACKAGE_ROOT ${SC_INSTALL_PREFIX}/${FLUID_PACKAGE_NAME})

foreach(PACKAGE_DIRECTORY Classes;HelpSource;Examples)
  install(DIRECTORY "release-packaging/${PACKAGE_DIRECTORY}" 
          DESTINATION ${SC_PACKAGE_ROOT})
endforeach()

install(DIRECTORY ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/ 
        DESTINATION ${SC_PACKAGE_ROOT}/plugins 
        PATTERN "*.ilk" EXCLUDE 
        PATTERN "*.PDB" EXCLUDE)
install(DIRECTORY "${flucoma-core_SOURCE_DIR}/AudioFiles" 
        DESTINATION ${SC_PACKAGE_ROOT})
install(FILES QuickStart.md 
        DESTINATION ${SC_PACKAGE_ROOT})
install(FILES ${flucoma-core_SOURCE_DIR}/distribution.lic 
        DESTINATION ${SC_PACKAGE_ROOT} 
        RENAME LICENSE.md)
