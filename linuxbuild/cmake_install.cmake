# Install script for directory: /media/psf/Home/dev/flucoma-sc

# Set the install prefix
if(NOT DEFINED CMAKE_INSTALL_PREFIX)
  set(CMAKE_INSTALL_PREFIX "/media/psf/Home/dev/flucoma-sc/install")
endif()
string(REGEX REPLACE "/$" "" CMAKE_INSTALL_PREFIX "${CMAKE_INSTALL_PREFIX}")

# Set the install configuration name.
if(NOT DEFINED CMAKE_INSTALL_CONFIG_NAME)
  if(BUILD_TYPE)
    string(REGEX REPLACE "^[^A-Za-z0-9_]+" ""
           CMAKE_INSTALL_CONFIG_NAME "${BUILD_TYPE}")
  else()
    set(CMAKE_INSTALL_CONFIG_NAME "Release")
  endif()
  message(STATUS "Install configuration: \"${CMAKE_INSTALL_CONFIG_NAME}\"")
endif()

# Set the component getting installed.
if(NOT CMAKE_INSTALL_COMPONENT)
  if(COMPONENT)
    message(STATUS "Install component: \"${COMPONENT}\"")
    set(CMAKE_INSTALL_COMPONENT "${COMPONENT}")
  else()
    set(CMAKE_INSTALL_COMPONENT)
  endif()
endif()

# Install shared libraries without execute permission?
if(NOT DEFINED CMAKE_INSTALL_SO_NO_EXE)
  set(CMAKE_INSTALL_SO_NO_EXE "1")
endif()

# Is this installation the result of a crosscompile?
if(NOT DEFINED CMAKE_CROSSCOMPILING)
  set(CMAKE_CROSSCOMPILING "FALSE")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/./FluidCorpusManipulation" TYPE DIRECTORY FILES "/media/psf/Home/dev/flucoma-sc/release-packaging/Classes")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/./FluidCorpusManipulation" TYPE DIRECTORY FILES "/media/psf/Home/dev/flucoma-sc/release-packaging/HelpSource")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/./FluidCorpusManipulation" TYPE DIRECTORY FILES "/media/psf/Home/dev/flucoma-sc/release-packaging/Examples")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/./FluidCorpusManipulation/plugins" TYPE DIRECTORY FILES "/media/psf/Home/dev/flucoma-sc/release-packaging/plugins/" REGEX "/[^/]*\\.ilk$" EXCLUDE REGEX "/[^/]*\\.PDB$" EXCLUDE)
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/./FluidCorpusManipulation" TYPE DIRECTORY FILES "/home/owen/mac/dev/flucoma-core/AudioFiles")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/./FluidCorpusManipulation" TYPE FILE FILES "/media/psf/Home/dev/flucoma-sc/QuickStart.md")
endif()

if("x${CMAKE_INSTALL_COMPONENT}x" STREQUAL "xUnspecifiedx" OR NOT CMAKE_INSTALL_COMPONENT)
  file(INSTALL DESTINATION "${CMAKE_INSTALL_PREFIX}/./FluidCorpusManipulation" TYPE FILE RENAME "LICENSE.md" FILES "/home/owen/mac/dev/flucoma-core/distribution.lic")
endif()

if(NOT CMAKE_INSTALL_LOCAL_ONLY)
  # Include the install script for each subdirectory.
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/_deps/flucoma-core-build/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidAmpGate/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidAmpSlice/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufAmpGate/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufAmpSlice/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufCompose/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufFlatten/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufHPSS/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufLoudness/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufMFCC/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufMelBands/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufNMF/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufNoveltySlice/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufOnsetSlice/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufPitch/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufSines/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufSpectralShape/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufStats/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufThreadDemo/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufTransientSlice/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidBufTransients/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidGain/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidHPSS/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidLoudness/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidMFCC/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidManipulation/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidMelBands/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidMessageTest/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidNMFCross/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidNMFFilter/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidNMFMatch/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidNMFMorph/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidNoveltySlice/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidOnsetSlice/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidPitch/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidSTFTPass/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidSines/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidSpectralShape/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidSubscriberProviderTest/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidTransientSlice/cmake_install.cmake")
  include("/media/psf/Home/dev/flucoma-sc/linuxbuild/src/FluidTransients/cmake_install.cmake")

endif()

if(CMAKE_INSTALL_COMPONENT)
  set(CMAKE_INSTALL_MANIFEST "install_manifest_${CMAKE_INSTALL_COMPONENT}.txt")
else()
  set(CMAKE_INSTALL_MANIFEST "install_manifest.txt")
endif()

string(REPLACE ";" "\n" CMAKE_INSTALL_MANIFEST_CONTENT
       "${CMAKE_INSTALL_MANIFEST_FILES}")
file(WRITE "/media/psf/Home/dev/flucoma-sc/linuxbuild/${CMAKE_INSTALL_MANIFEST}"
     "${CMAKE_INSTALL_MANIFEST_CONTENT}")
