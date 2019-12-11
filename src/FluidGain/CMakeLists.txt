cmake_minimum_required(VERSION 3.11)
get_filename_component(PLUGIN ${CMAKE_CURRENT_LIST_DIR} NAME_WE)
message("Configuring ${PLUGIN}")
set(FILENAME ${PLUGIN}.cpp)

add_library(
  ${PLUGIN}
  MODULE
  ${FILENAME}
)

target_include_directories(
  ${PLUGIN} PRIVATE ${CMAKE_CURRENT_LIST_DIR}/../../include
)

target_link_libraries(
  ${PLUGIN} PRIVATE FLUID_DECOMPOSITION FLUID_SC_WRAPPER
)

include(${CMAKE_CURRENT_LIST_DIR}/../../scripts/target_post.cmake)
