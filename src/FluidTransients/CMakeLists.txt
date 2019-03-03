cmake_minimum_required(VERSION 3.3)
get_filename_component(PLUGIN ${CMAKE_CURRENT_LIST_DIR} NAME_WE)
message("Configuring ${PLUGIN}")
set(FILENAME ${PLUGIN}.cpp)

add_library(
  ${PLUGIN}
  MODULE
  ${FILENAME}
)

include(${CMAKE_CURRENT_LIST_DIR}/../../scripts/target_post.cmake)
