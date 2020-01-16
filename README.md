# Fluid Corpus Manipulation: SuperCollider Objects Library

This repository hosts code for generating the SC objects and documentation resources for the Fluid Corpus Manipulation Project. Much of the actual code that does the exciting stuff lives in this repository's principal dependency,  the [Fluid Corpus Manipulation Library](). What lives here are: 

* A wrapper from our code to the SC API, that allows us to generate SC objects from a generic class. 
* Stubs for producing SC objects for each 'client' in the Fluid Corpus Manipulation Library. 
* CMake code for managing dependencies, building and packaging. 


## I'm in a Hurry...

...and you already have a development environment set up, understand CMake, and have the SC source available? 

Cool: 

```
cmake -DSC_PATH=<location of your SC source> ..
make 
```
You can either symbolically link the `release-packaging` directory into your SC extensions folder, or use CMake to generate a 'clean package' with `make install`. 

## Pre-requisites

* [CMake](http://cmake.org) >= 3.11
* A C++ 14 compliant compiler for Mac or Windows (via XCode tools on Mac, and Visual Studio 17 >= 15.9 on Windows)

## Dependencies 

* [SC Source Code](https://github.com/supercollider/supercollider): this is the only dependency we don't (optionally) manage for you, so there must be a version available to point to when you run, using the CMake Variable `SC_PATH` (see below). It can live anywhere on your file system. 

These will be downloaded and configured automatically, unless you pass CMake a source code location on disk for each (see below): 
* [Fluid Corpus Manipulation Library]()
* [Eigen](https://gitlab.com/libeigen/eigen) (3.3.5)
* [HISSTools Library](https://github.com/AlexHarker/HISSTools_Library)

Unless you are simultaneously committing changes to these dependencies (or are *seriously* worried about disk space), there is nothing to be gained by pointing to external copies, and the easiest thing to is let CMake handle it all. 

## Building 

Simplest possible build: 
1. Download the SuperCollider source (>= 3.10.0) if you haven't already 
2. Clone this repo
```
git clone <whereis this>
```
3. Change to the directory for this repo, make a build directory, and run CMake, passing in the location for the Max SDK 
```
mkdir -p build && cd build 
cmake -DSC_PATH=<location of your SC source> ..
```
At this point, CMake will set up your tool chain (i.e. look for compilers), and download all the dependencies. 

An alternative to setting up / running CMake directly on the command line is to install the CMake GUI, or use to use the curses gui `ccmake`.

With CMake you have a choice of which build system you use. 
* The default on Mac is `Unix Makefiles`, but you can use Xcode by passing `-GXcode` to CMake when you first run it. 
* The default on Windows is the latest version of Visual Studio installed. However, Visual Studio can open CMake files directly as projects, which has some upsides. When used this way, CMake variables have to be set via a JSON file that MSVC will use to configure CMake. 

When using `make`, then
```
make
```
will compile all objects ('targets', in CMake-ish). Alternatively, in Xcode or Visual Studio, running 'build' will (by default) build all objects. Note that these IDEs allow you to build both for debugging and for release, whereas with Makefiles, you need to re-run CMake and pass a `CMAKE_CONFIG` variable for different build types.


```
make install 
```
Will assemble a clean package in `release-packaging/FluidCorpusManipulation`. 


## Structure: 

The top-level folders mostly correspond to those you would find in a typical Max package. Some more will appear when you compile (such as `externals` and possibly `docs`).

The main action is in `source`:
```
source
├── include
├── src
└── scripts
``` 
* `include` contains the header files for the FluCoMa-SC wrapper 
* `src` contains a subfolder for each object to be generated; each of these subfolders has a `.cpp` stub and a `CMakeLists.txt`. The `.cpp` file name needs to match its parent folder. 
* `scripts` contains CMake scripts (most significantly, `target_post.cmake`, which sets behaviours for all the objects).

## Development: Manual Dependencies 

If you're making changes to the Fluid Corpus Manipulation Library and testing against Max (and, hopefully, our other deployment environments), then it makes sense to have the source for this cloned somewhere else on your disk, so that you can commit and push changes, and ensure that they work in all environments. This would be the case, e.g., if you're developing a new client. 

To bypass the automatic cloning of the Fluid Corpus Manipulation Library, we pass in the cache variable `FLUID_PATH`

```
cmake -DSC_PATH=<location of your SC source> -DFLUID_PATH=<location of Fluid Corpus Manipulation Library> ..
```
Note 
1. You don't need to run CMake on the Fluid Corpus Manipulation Library first (well, you can, but it doesn't make any difference!). CMake's FetchContent facility will grab the targets from there, but won't look in its CMakeCache, so there should never be a conflict between the state of a build tree at `FLUID_PATH` and your build-tree here. 
2. It is **up to you** to make sure the commits you have checked out in each repository make sense together. We use tags against release versions on the `master` branch, so – at the very least – these should line up (unless you're tracking down some regression bug or similar). In general, there is no guarantee, or likelihood, that mismatched tags will build or run, as architectural changes can, do, will happen...

### Dependencies of dependencies! 
The same steps and considerations apply to manually managing the dependencies of the Fluid Corpus Manipulation Library itself. If these aren't explicitly passed whilst running CMake against this build tree, then CMake will download them itself against the tags / commits we develop against. Nevertheless, if you are in the process of making changes to these libraries and running against this (which is much less likely than above), then the CMake variables of interest are: 
* `EIGEN_PATH` pointing to the location of Eigen on disk 
* `HISSTOOLS_PATH` pointing to the location of the HISSTools Library 

To find out which branches / tags / commits of these we use, look in the top level `CMakeLists.txt` of the  Fluid Corpus Manipulation Library for the `FetchContent_Declare` statements for each dependency. 

> This project has received funding from the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899).
