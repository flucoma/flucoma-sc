# Fluid Corpus Manipulation: SuperCollider Objects Library

This repository hosts code for generating the SC objects and documentation resources for the Fluid Corpus Manipulation Project. Much of the actual code that does the exciting stuff lives in this repository's principal dependency, the [Fluid Corpus Manipulation Library](https://github.com/flucoma/flucoma-core).


You can also download the most [recent release](https://learn.flucoma.org/installation/sc) or the most recent [nightly build](https://github.com/flucoma/flucoma-sc/releases/tag/nightly).

Note that on macOS you may need to [dequarantine](https://learn.flucoma.org/installation/sc#step-3-dequarantine-scx-extensions) the binary files.

## Pre-requisites


* C++14 compliant compiler (clang, GCC or MSVC)
* cmake 
* make (or Ninja or XCode or VisualStudio)
* git 
* an internet connection 
* [SuperCollider Source Code](https://github.com/supercollider/supercollider)

CMake will automatically download the other dependencies needed

```bash
mkdir -p build && cd build
cmake -DSC_PATH=</path/to/sc> ..
make install
```

This will assemble a clean package in `release-packaging/FluidCorpusManipulation`.

An alternative to setting up / running CMake directly on the command line is to install the CMake GUI, or use to use the curses GUI `ccmake`.

Also, with CMake you have a choice of which build system you use.

- The default on macOS and Linux is `Unix Makefiles`. On macOS you can also use Xcode by passing `-GXcode` to CMake when you first run it.
- The default on Windows is the latest version of Visual Studio installed. However, Visual Studio can open CMake files directly as projects, which has some upsides. When used this way, CMake variables have to be set via a JSON file that MSVC will use to configure CMake.

## Using Manual Dependencies

In some cases you may want to use your own copies of the required libraries. Unless specified, the build system will download these automatically. To bypass this behaviour, use the following cache variables:

- `FLUID_PATH`: location of the Fluid Corpus Manipulation Library
- `FLUID_DOCS_PATH`: location of `fluid-docs` repository (e.g. for debugging documentation generation)
- `EIGEN_PATH` location of the Eigen library
- `HISS_PATH` location of the HISSTools library

For example, use this to us your own copy of the Fluid Corpus Manipulation Library:

```
cmake -DSC_PATH=<location of your SC source> -DFLUID_PATH=<location of Fluid Corpus Manipulation Library> ..
```

To find out which branches / tags / commits of these we use, look in the top level `CMakeLists.txt` of the Fluid Corpus Manipulation Library for the `FetchContent_Declare` statements for each dependency.

## Compiling for different CPUs

The build system generally assumes an x86 cpu with AVX instructions (most modern x86 CPUs). To build on another kind of CPU (e.g. older than 2012) you can use the `FLUID_ARCH` cache variable to pass specific flags to your compiler. For example use `-DFLUID_ARCH=-mcpu=native` to optimize for your particular CPU.

For ARM, we use the following default set of flags (with the Bela in mind):

```
-march=armv7-a -mtune=cortex-a8 -mfloat-abi=hard -mfpu=neon
```
=======
This will assemble a package in `release-packaging`.

## Credits
#### FluCoMa core development team (in alphabetical order)
Owen Green, Gerard Roma, Pierre Alexandre Tremblay

#### Other contributors (in alphabetical order):
James Bradbury, Francesco Cameli, Alex Harker, Ted Moore

--

> This project has received funding from the European Research Council (ERC) under the European Union's Horizon 2020 research and innovation programme (grant agreement No 725899).
