# Instructions for the PureData version of the fluid.* toolbox

## How to start:

1) move the following items to their respective relevant pd search path
- externals are in /pd_objects
- help files are in /help
- audio used in the help files are in /media, which is expected to be parallel to /help
- detailed help is provided as a website in /docs

2) The PureData document 'Fluid_Decomposition_Overview.pd' shows the toolbox at a glance.

5) Parameters can be set by message (as in max with the same names) OR by option in the object box which work similarly to the [sigmund~] options.

6) Most objects working on arrays/buffers are multichannel. The toolbox uses the following convention: a named array is expected to have a name, followed by -x where x is the 'channel' number, 0-indexed. For instance, a stereo source buffer defined as 'mybuf' will expect an array named 'mybuf-0' for the left channel, and an array named 'mybuf-1' for the right channel. A utility [multiarray.pd] is used throughout the helpfiles in conjonction with the [clone] to programmatically generate such 'multichannel' buffers.

#### Enjoy!


## Known issues:
- pd is single threaded so doing buffer ops will do bad things to realtime audio.
- providing 'multichannel' arrays not enough in numbers (aka channels) will crash Pd.
