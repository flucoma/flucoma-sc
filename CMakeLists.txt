cmake_minimum_required(VERSION 3.11)

################################################################################
# Paths
set(CMAKE_INSTALL_PREFIX "${CMAKE_CURRENT_SOURCE_DIR}/release-packaging")
set(MAX_TARGET_NAMESPACE "" CACHE STRING "Optional namesapce to prepend to targets to prevent collision in super-builds")


set(SC_PATH "" CACHE PATH "Path to the top of SuperCollider source tree")
if(NOT SC_PATH)
  message(FATAL_ERROR "SuperCollider source path is not set")
endif()

set(FLUID_PATH "" CACHE PATH "Optional path to the Fluid Decomposition repo")
# set(FLUID_M_PATH "" CACHE PATH "Optional path to the Fluid fluid_manipulation repo")

if (APPLE)
	set(CMAKE_OSX_ARCHITECTURES x86_64)
  set(CMAKE_XCODE_GENERATE_SCHEME ON)
endif()

################################################################################
# Main project
project (fluid_decomposition_supercollider LANGUAGES CXX)
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
  fluid_decomposition
  GIT_REPOSITORY https://bitbucket.org/flucoma/fluid_decomposition.git
  GIT_PROGRESS TRUE
)

if(FLUID_PATH)
  get_filename_component(
    FETCHCONTENT_SOURCE_DIR_FLUID_DECOMPOSITION ${FLUID_PATH} ABSOLUTE
  )
endif()

FetchContent_GetProperties(fluid_decomposition)
if(NOT fluid_decomposition_POPULATED)
  FetchContent_Populate(fluid_decomposition)
  add_subdirectory(${fluid_decomposition_SOURCE_DIR} ${fluid_decomposition_BINARY_DIR})
endif()

set(CMAKE_LIBRARY_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/${CMAKE_SYSTEM_NAME}/plugins")
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

foreach(PACKAGE_DIRECTORY Classes;HelpSource;ignore)
  install(DIRECTORY "release-packaging/${PACKAGE_DIRECTORY}" DESTINATION ${SC_PACKAGE_ROOT})
endforeach()

install(DIRECTORY ${CMAKE_LIBRARY_OUTPUT_DIRECTORY} DESTINATION ${SC_PACKAGE_ROOT} PATTERN "*.ilk" EXCLUDE PATTERN "*.PDB" EXCLUDE)
install(DIRECTORY "${fluid_decomposition_SOURCE_DIR}/AudioFiles" DESTINATION ${SC_PACKAGE_ROOT})
install(FILES QuickStart.md DESTINATION .)
