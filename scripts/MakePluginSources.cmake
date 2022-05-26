# Part of the Fluid Corpus Manipulation Project (http://www.flucoma.org/)
# Copyright 2017-2019 University of Huddersfield.
# Licensed under the BSD-3 License.
# See license.md file in the project root for full license information.
# This project has received funding from the European Research Council (ERC)
# under the European Union’s Horizon 2020 research and innovation programme
# (grant agreement No 725899).

include(FLuidClientStub)

function(make_external_name client header var)  
  set(${var} Fluid${client} PARENT_SCOPE)
endfunction()

function(add_sc_extension PLUGIN FILENAME)
  
  add_library(${PLUGIN} MODULE ${FILENAME})
  
  if(MSVC)
    target_compile_options(${PLUGIN} PRIVATE /external:W0 /W3 /bigobj)
  else()
    target_compile_options(${PLUGIN} PRIVATE 
      -Wall -Wextra -Wpedantic -Wreturn-type -Wconversion
    )
    
    #GCC doesn't have Wno-c++11-narrowing
    if (CMAKE_CXX_COMPILER_ID STREQUAL "Clang")
      target_compile_options(${PLUGIN} PRIVATE  -Wno-c++11-narrowing)
    endif()
  endif() 

  if(APPLE)
    set_target_properties(${PLUGIN} PROPERTIES
      XCODE_GENERATE_SCHEME ON
    )
    #If we target 10.7 (actually < 10.9), we have to manually include this:
    target_compile_options(${PLUGIN} PRIVATE -stdlib=libc++)
  endif()

  target_link_libraries(
    ${PLUGIN}
    PRIVATE  
    FLUID_DECOMPOSITION
    FLUID_SC_WRAPPER  
    HISSTools_FFT
  )

  target_include_directories(
    ${PLUGIN}
    PRIVATE
    "${LOCAL_INCLUDES}"
    "${FLUID_VERSION_PATH}"
  )

  file(GLOB_RECURSE FLUID_SC_HEADERS CONFIGURE_DEPENDS "${CMAKE_SOURCE_DIR}/include/wrapper/*.hpp")

  target_sources(
    ${PLUGIN} PUBLIC ${FLUID_SC_HEADERS}
  )

  target_include_directories(
    ${PLUGIN}
    SYSTEM PRIVATE
    "${SC_PATH}/include/plugin_interface"
    "${SC_PATH}/include/common"
    "${SC_PATH}/common"
    "${SC_PATH}/external_libraries/boost" #we need boost::align for deallocating buffer memory :-(
  )

  get_property(HEADERS TARGET FLUID_DECOMPOSITION PROPERTY INTERFACE_SOURCES)
  source_group(TREE "${flucoma-core_SOURCE_DIR}/include" FILES ${HEADERS})
  source_group(TREE "${CMAKE_SOURCE_DIR}/include/wrapper" PREFIX wrapper FILES ${FLUID_SC_HEADERS})

  if(MINGW)
      set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -mstackrealign")
      set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mstackrealign")
  endif()

  if(DEFINED FLUID_ARCH)
    target_compile_options(${PLUGIN} PRIVATE ${FLUID_ARCH})
  endif()

  if(MSVC)
    target_compile_options(${PLUGIN} PRIVATE -D_USE_MATH_DEFINES)
  else()
    target_compile_options(${PLUGIN} PRIVATE -fvisibility=hidden)
  endif()
  
  #optional extra build settings (e.g for /bigobj with MSVC)
  include(
    "${CMAKE_CURRENT_SOURCE_DIR}/scripts/build-settings/${PLUGIN}.cmake"  
    OPTIONAL
  )  
endfunction()

function(generate_sc_source)  
  # # Define the supported set of keywords
  set(noValues "")
  set(singleValues FILENAME EXTERNALS_OUT FILE_OUT)
  set(multiValues CLIENTS HEADERS CLASSES)
  # # Process the arguments passed in
  include(CMakeParseArguments)
  cmake_parse_arguments(ARG
  "${noValues}"
  "${singleValues}"
  "${multiValues}"
  ${ARGN})  
  
  set(CCE_WRAPPER "#include <FluidSCWrapper.hpp>")
  set(ENTRY_POINT "PluginLoad(FlucomaPlugin)")  
  set(WRAPPER_TEMPLATE [=[makeSCWrapper<${class}>("${external}", inTable);]=])

  set(EXTRA_SOURCE_FILE "${CMAKE_CURRENT_SOURCE_DIR}/src/extra/${ARG_FILENAME}.cpp.in")
  
  if(EXISTS ${EXTRA_SOURCE_FILE})  
    generate_source(${ARGN} EXTRA_SOURCE ${EXTRA_SOURCE_FILE} EXTERNALS_OUT external FILE_OUT outfile)  
  else()
    generate_source(${ARGN} EXTERNALS_OUT external FILE_OUT outfile)
  endif()
  
  if(ARG_FILENAME)
    set(external_filename ${ARG_FILENAME})
  else()
    list(GET external 0 external_filename)
  endif()
  
  message(STATUS "Generating: ${external_filename}")
  add_sc_extension(${external_filename} ${outfile})
endfunction()
