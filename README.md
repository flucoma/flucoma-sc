# Fluid Corpus Manipulation: SuperCollider Objects Library

This repository hosts code for generating the SC objects and documentation resources for the Fluid Corpus Manipulation Project. Much of the actual code that does the exciting stuff lives in this repository's principal dependency, the [Fluid Corpus Manipulation Library](https://github.com/flucoma/flucoma-core).

# Minimal Quick Build

Minimal build steps below. For detailed guidance see https://github.com/flucoma/flucoma-sc/wiki/Compiling

## Prerequisites 

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

This will assemble a package in `release-packaging`.

## Credits 
#### FluCoMa core development team (in alphabetical order)
Owen Green, Gerard Roma, Pierre Alexandre Tremblay

#### Other contributors:
Alex Harker, Francesco Cameli

--

> This project has received funding from the European Research Council (ERC) under the European Union's Horizon 2020 research and innovation programme (grant agreement No 725899).
