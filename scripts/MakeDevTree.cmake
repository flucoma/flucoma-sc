# Part of the Fluid Corpus Manipulation Project (http://www.flucoma.org/)
# Copyright University of Huddersfield.
# Licensed under the BSD-3 License.
# See license.md file in the project root for full license information.
# This project has received funding from the European Research Council (ERC)
# under the European Union’s Horizon 2020 research and innovation programme
# (grant agreement No 725899).

cmake_minimum_required(VERSION 3.18)

file(GLOB helpfiles "${ROOT}/help/*")
file(GLOB abstractions "${ROOT}/abstractions/*")
file(GLOB externals "${ROOT}/pd_objects/*")

set(devfolder "${ROOT}/dev")
file(REMOVE_RECURSE ${devfolder})
file(MAKE_DIRECTORY ${devfolder})

foreach(item IN LISTS helpfiles abstractions externals) 
  get_filename_component(item_name "${item}" NAME)
  file(CREATE_LINK "${item}" "${devfolder}/${item_name}" SYMBOLIC)
endforeach()

file(CREATE_LINK "${CORE_SRC}/Resources/" "${devfolder}/Resources" SYMBOLIC)
file(CREATE_LINK "${BINARIES}/Resources/" "${devfolder}/Resources" SYMBOLIC)
file(CREATE_LINK "${CORE_SRC}/Resources/" "${devfolder}/Resources" SYMBOLIC)
file(CREATE_LINK "${CORE_SRC}/Resources/" "${devfolder}/Resources" SYMBOLIC)
file(CREATE_LINK "${CORE_SRC}/Resources/" "${devfolder}/Resources" SYMBOLIC)
