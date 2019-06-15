
target_compile_features(${PLUGIN} PUBLIC cxx_std_14)

if(MSVC)
  target_compile_options(${PLUGIN} PRIVATE /W4)
else()
  target_compile_options(${PLUGIN} PRIVATE -Wall -Wextra -Wpedantic -Wreturn-type -Wconversion)
endif()

set_target_properties(${PLUGIN} PROPERTIES
    CXX_STANDARD 14
    CXX_STANDARD_REQUIRED YES
    CXX_EXTENSIONS NO
)

target_link_libraries(
  ${PLUGIN}
  PUBLIC
  FLUID_DECOMPOSITION
  FLUID_SC_WRAPPER
  PRIVATE
  FFTLIB
)


target_include_directories(
  ${PLUGIN}
  PRIVATE
  ${LOCAL_INCLUDES}
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
source_group(TREE "${FLUID_PATH}/include" FILES ${HEADERS})


if (SUPERNOVA)
    target_include_directories(
      ${PLUGIN}
      SYSTEM PRIVATE
      "${SC_PATH}/external_libraries/nova-tt"
      "${SC_PATH}/external_libraries/boost_lockfree"
      "${SC_PATH}/external_libraries/boost-lockfree"
    )
endif()

if(CMAKE_COMPILER_IS_GNUCXX OR CMAKE_COMPILER_IS_CLANG)
    target_compile_options(${PLUGIN} PRIVATE -fvisibility=hidden)

    include (CheckCXXCompilerFlag)

    # CHECK_CXX_COMPILER_FLAG(-msse HAS_CXX_SSE)
    # CHECK_CXX_COMPILER_FLAG(-msse2 HAS_CXX_SSE2)
    # CHECK_CXX_COMPILER_FLAG(-mfpmath=sse HAS_CXX_FPMATH_SSE)
    # CHECK_CXX_COMPILER_FLAG(-mavx HAS_AVX)
    # CHECK_CXX_COMPILER_FLAG(-mavx2 HAS_AVX2)

    target_compile_options(
        ${PLUGIN}
        PRIVATE
        $<$<NOT:$<CONFIG:DEBUG>>: -mavx -msse -msse2 -msse3 -msse4>
    )
endif()



if(MINGW)
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -mstackrealign")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mstackrealign")
endif()

if(MSVC)
  target_compile_options(${PLUGIN} PRIVATE /arch:AVX -D_USE_MATH_DEFINES)
else(MSVC)
target_compile_options(
   ${PLUGIN} PRIVATE $<$<NOT:$<CONFIG:DEBUG>>: -mavx -msse -msse2 -msse3 -msse4>
)
endif(MSVC)

####### added the fluid_decomposition

if(SUPERNOVA)
    add_library(${PLUGIN}_supernova MODULE ${FILENAME})
    set_property(TARGET ${PROJECT}_supernova
                 PROPERTY COMPILE_DEFINITIONS SUPERNOVA)
endif()
