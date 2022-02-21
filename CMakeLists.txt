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

set(FLUID_PATH "" CACHE PATH "Optional path to the Fluid Decomposition repo")

if (APPLE)
  set(CMAKE_XCODE_GENERATE_SCHEME ON)
	set(CMAKE_OSX_ARCHITECTURES "x86_64" CACHE STRING "")
  set(CMAKE_OSX_DEPLOYMENT_TARGET "10.8" CACHE STRING "")
  #A consequence of targetting 10.8. Needs to be set globally from 10.15 onwards in order for the test program to compile successfully during configure 
  string(APPEND CMAKE_CXX_FLAGS " -stdlib=libc++")
endif()

option(SYSTEM_BOOST  "Use boost libraries from system. If your SC was built with this enabled, so should FluCoMa-sc" OFF)

################################################################################
# Main project
project (flucoma-sc LANGUAGES CXX)

if(NOT MSVC)
  add_compile_options(-fdiagnostics-color=always)
endif() 

#set correct std lib linking for Windows (in CMake 3.15 this has a native function)
if(MSVC) #needs to be after project()
  foreach(flag_var
      CMAKE_CXX_FLAGS CMAKE_CXX_FLAGS_DEBUG CMAKE_CXX_FLAGS_RELEASE
      CMAKE_CXX_FLAGS_MINSIZEREL CMAKE_CXX_FLAGS_RELWITHDEBINFO)
    if(${flag_var} MATCHES "/MD")
      string(REGEX REPLACE "/MD" "/MT" ${flag_var} "${${flag_var}}")
    endif()
  endforeach()
endif()

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
  GIT_TAG origin/main
)

FetchContent_Declare(
  flucoma-docs
  GIT_REPOSITORY https://github.com/flucoma/flucoma-docs.git
  GIT_PROGRESS TRUE
  GIT_TAG origin/main
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

option(DOCS "Generate scdocs" OFF)
set(FLUID_DOCS_PATH "" CACHE PATH "Optional path to flucoma-docs (needed for docs); will download if absent")

if(DOCS)
  
  set(${SC_DOC_OUT} "${CMAKE_SOURCE_DIR}/HelpSource/" CACHE PATH "")
  
  if(FLUID_DOCS_PATH)
    get_filename_component(
      FETCHCONTENT_SOURCE_DIR_FLUCOMA-DOCS ${FLUID_DOCS_PATH} ABSOLUTE
    )
  endif()

  FetchContent_GetProperties(flucoma-docs)
  if(NOT flucoma-docs_POPULATED)
    FetchContent_Populate(flucoma-docs)
    file(GLOB_RECURSE DOC_SOURCE RELATIVE "${CMAKE_CURRENT_SOURCE_DIR}" "${flucoma-docs_SOURCE_DIR}/**/*.cpp" )
    source_group("\\SC Doc Gen" FILES ${DOC_SOURCE})
    add_subdirectory(${flucoma-docs_SOURCE_DIR} ${flucoma-docs_BINARY_DIR})
  endif()

  add_custom_target(SC_MAKE_DOCS ALL DEPENDS MAKE_SC_REF)
endif()

set_if_toplevel(VAR CMAKE_LIBRARY_OUTPUT_DIRECTORY 
                TOPLEVEL "${CMAKE_CURRENT_SOURCE_DIR}/release-packaging/Plugins"
                SUPERBUILD "${CMAKE_SOURCE_DIR}/sc_plugins/${CMAKE_HOST_SYSTEM_NAME}/${CMAKE_HOST_SYSTEM_PROCESSOR}")

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY_DEBUG "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY_RELEASE "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY_RELWITHDEBINFO "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY_TEST "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")
set(CMAKE_SHARED_MODULE_PREFIX "")

if(APPLE OR WIN32)
  set(CMAKE_SHARED_MODULE_SUFFIX ".scx")
endif()

#needed for complaint-free static linking with GCC
if(CMAKE_COMPILER_IS_GNUCXX)
  target_compile_options( HISSTools_FFT PUBLIC -fPIC )
endif()

#sandbox regrettable dependency on SC internals for SendReply()

if(SYSTEM_BOOST)
  find_package(Threads REQUIRED) 
  set(Boost_NO_BOOST_CMAKE ON)
  set(Boost_USE_MULTITHREADED ON)
  find_package(Boost 1.50.0 COMPONENTS thread system REQUIRED) 
  add_library(boost_thread_lib SHARED IMPORTED)
  set_property(TARGET boost_thread_lib PROPERTY IMPORTED_LOCATION ${Boost_THREAD_LIBRARY})
  add_library(boost_system_lib SHARED IMPORTED)
  set_property(TARGET boost_system_lib PROPERTY IMPORTED_LOCATION ${Boost_SYSTEM_LIBRARY})
endif()

add_library(FLUID_SC_COPYREPLYADDR STATIC
  "${CMAKE_SOURCE_DIR}/include/wrapper/CopyReplyAddress.cpp"
  "${SC_PATH}/common/SC_Reply.cpp"
)

target_include_directories(FLUID_SC_COPYREPLYADDR SYSTEM PRIVATE 
  "${SC_PATH}/include/plugin_interface"
  "${SC_PATH}/include/common"
  "${SC_PATH}/common"
)

if(SYSTEM_BOOST)
  target_link_libraries(FLUID_SC_COPYREPLYADDR ${boost_system_lib})
else()
  target_sources(FLUID_SC_COPYREPLYADDR PRIVATE 
    "${SC_PATH}/external_libraries/boost/libs/system/src/error_code.cpp"
  )
  target_include_directories(FLUID_SC_COPYREPLYADDR SYSTEM PRIVATE 
    "${SC_PATH}/external_libraries/boost"
  )    
endif()


set_target_properties(FLUID_SC_COPYREPLYADDR PROPERTIES
    CXX_STANDARD 14
    CXX_STANDARD_REQUIRED YES
    CXX_EXTENSIONS NO
)

if(CMAKE_COMPILER_IS_GNUCXX)
  target_compile_options(FLUID_SC_COPYREPLYADDR PUBLIC -fPIC )
endif()

if(APPLE)
  target_compile_options(FLUID_SC_COPYREPLYADDR PRIVATE -stdlib=libc++)
endif()

target_compile_definitions(FLUID_SC_COPYREPLYADDR PRIVATE BOOST_ALL_NO_LIB  BOOST_CONFIG_SUPPRESS_OUTDATED_MESSAGE)

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

target_link_libraries(FLUID_SC_WRAPPER INTERFACE FLUID_SC_COPYREPLYADDR)

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

foreach(PACKAGE_DIRECTORY Classes;HelpSource;Examples;)
  install(DIRECTORY "release-packaging/${PACKAGE_DIRECTORY}" 
          DESTINATION ${SC_PACKAGE_ROOT})
endforeach()

install(DIRECTORY "sc-resources/"
        DESTINATION ${SC_PACKAGE_ROOT}/Resources)

install(DIRECTORY ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/ 
        DESTINATION ${SC_PACKAGE_ROOT}/Plugins
        PATTERN "*.ilk" EXCLUDE 
        PATTERN "*.PDB" EXCLUDE)
install(DIRECTORY "${flucoma-core_SOURCE_DIR}/Resources" 
        DESTINATION ${SC_PACKAGE_ROOT})
install(FILES QuickStart.md 
        DESTINATION ${SC_PACKAGE_ROOT})
install(FILES ${flucoma-core_SOURCE_DIR}/distribution.lic 
        DESTINATION ${SC_PACKAGE_ROOT} 
        RENAME LICENSE.md)

if(DOCS)
  install(DIRECTORY "${SC_DOC_OUT}" 
          DESTINATION "${SC_PACKAGE_ROOT}/HelpSource")
endif()
