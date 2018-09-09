target_include_directories(
  ${PLUGIN}
  PRIVATE
  ${SC_PATH}/include/plugin_interface
  ${SC_PATH}/include/common
  ${SC_PATH}/common
  ${SC_PATH}/external_libraries/boost #we need boost::align for deallocating buffer memory :-(
)

if (SUPERNOVA)
    target_include_directories(
      ${PLUGIN}
      PRIVATE
      ${SC_PATH}/external_libraries/nova-tt

      ${SC_PATH}/external_libraries/boost_lockfree
      ${SC_PATH}/external_libraries/boost-lockfree
    )
endif()

if(CMAKE_COMPILER_IS_GNUCXX OR CMAKE_COMPILER_IS_CLANG)
    target_add_definitions(${PLUGIN} -fvisibility=hidden)

    include (CheckCXXCompilerFlag)

    CHECK_CXX_COMPILER_FLAG(-msse HAS_CXX_SSE)
    CHECK_CXX_COMPILER_FLAG(-msse2 HAS_CXX_SSE2)
    CHECK_CXX_COMPILER_FLAG(-mfpmath=sse HAS_CXX_FPMATH_SSE)
    CHECK_CXX_COMPILER_FLAG(-mavx HAS_AVX)
    CHECK_CXX_COMPILER_FLAG(-mavx2 HAS_AVX2)
    target_compile_definitions(
        ${PLUGIN}
        PRIVATE
        "$<$<NOT:$<CONFIG:DEBUG>>:-O3"
        "-mavx"
    )

    #     set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -msse")
    # endif()
    #
    # CHECK_C_COMPILER_FLAG(-msse2 HAS_SSE2)
    #
    #
    # if (HAS_SSE2)
    #     set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -msse2")
    # endif()
    # if (HAS_CXX_SSE2)
    #     set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -msse2")
    # endif()
    #
    #
    #
    # if (HAS_FPMATH_SSE)
    #     set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -mfpmath=sse")
    # endif()
    # if (HAS_CXX_FPMATH_SSE)
    #     set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mfpmath=sse")
    # endif()
    #
    # if(NATIVE)
    #     add_definitions(-march=native)
    # endif()
    #

endif()
if(MINGW)
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -mstackrealign")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -mstackrealign")
endif()

####### added the fluid_decomposition

if(SUPERNOVA)
    add_library(${PLUGIN}_supernova MODULE ${FILENAME})
    set_property(TARGET ${PROJECT}_supernova
                 PROPERTY COMPILE_DEFINITIONS SUPERNOVA)
endif()
