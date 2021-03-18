# Copyright 2017-2019 University of Huddersfield.
# Licensed under the BSD-3 License.
# See license.md file in the project root for full license information.
# This project has received funding from the European Research Council (ERC)
# under the European Union’s Horizon 2020 research and innovation programme
# (grant agreement No 725899).

target_compile_features(${PLUGIN} PRIVATE cxx_std_14)

if(MSVC)
  target_compile_options(${PLUGIN} PRIVATE /W3)
else()
  target_compile_options(${PLUGIN} PRIVATE 
    -Wall -Wextra -Wpedantic -Wreturn-type -Wconversion
  )
  
  #GCC doesn't have Wno-c++11-narrowing
  if (CMAKE_CXX_COMPILER_ID STREQUAL "Clang")
    target_compile_options(${PLUGIN} PRIVATE  -Wno-c++11-narrowing)
  endif()
endif() 

set_target_properties(${PLUGIN} PROPERTIES
    CXX_STANDARD 14
    CXX_STANDARD_REQUIRED YES
    CXX_EXTENSIONS NO
)

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
  "${FLUID_M_PATH}/include/"
  "${FLUID_M_PATH}/thirdparty"
)

file(GLOB_RECURSE FLUID_MANIPULATION_HEADERS CONFIGURE_DEPENDS "${FLUID_M_PATH}/include/**/*.hpp")

file(GLOB_RECURSE FLUID_SC_HEADERS CONFIGURE_DEPENDS "${CMAKE_SOURCE_DIR}/include/wrapper/*.hpp")

target_sources(
  ${PLUGIN} PUBLIC ${FLUID_MANIPULATION_HEADERS}
  ${FLUID_SC_HEADERS}
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
source_group(TREE "${FLUID_M_PATH}/include" FILES ${FLUID_MANIPULATION_HEADERS})
source_group(TREE "${CMAKE_SOURCE_DIR}/include/wrapper" PREFIX wrapper FILES ${FLUID_SC_HEADERS})

# get_property(HEADERS TARGET FLUID_MANIP PROPERTY INTERFACE_SOURCES)
# source_group(TREE "${fluid_manipulation_SOURCE_DIR}/include" FILES ${HEADERS})
# 
# if (SUPERNOVA)
#     target_include_directories(
#       ${PLUGIN}
#       SYSTEM PRIVATE
#       "${SC_PATH}/external_libraries/nova-tt"
#       "${SC_PATH}/external_libraries/boost_lockfree"
#       "${SC_PATH}/external_libraries/boost-lockfree"
#     )
# endif()

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
