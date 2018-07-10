// FD_BufNMF, an NRT buffer NMF Processor
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.h"

static InterfaceTable *ft;

void BufNMF(World *world, struct SndBuf *buf, struct sc_msg_iter *msg)
{
	int frames1 = buf->frames;
	int channels1 = buf->channels;

	uint32 bufnum2 = msg->geti();
	int repetitions = msg->geti();

	if (bufnum2 >= world->mNumSndBufs){
		Print("waveSetCopyTo is not happy because the source buffer does not exist.\n");
		return;
	}

	SndBuf* buf2 = world->mSndBufs + bufnum2;

	if (buf2->data == buf->data){
		Print("waveSetCopyTo is not happy because the source buffer is the same as the destination buffer.\n");
		return;
	}

	int frames2 = buf2->frames;
	int channels2 = buf2->channels;

	if (channels1 != channels2) {
		Print("waveSetCopyTo is not happy because the source buffer has a different channel count than the destination buffer.\n");
		return;
	}

	// checks if default value (0) or error in quantity and sets to a filling behaviour or at least one
	if (repetitions < 1){
		repetitions = int(frames1 / frames2);
		if (repetitions < 1)
		repetitions = 1;
	}

	//for each channels
	for (int j=0;j<channels2;j++){
		long lastXadd = -1; //set start frame as invalid address flag
		short prevpol = (buf2->data[j] > 0); //set the previous sample polarity as the first sample
		long writeindex = 0; // set the writing index at the start of the buffer
		long wavesetlenght;

		//interates through the source samples
		for (int i=0;i<frames2;i++){
			//if previously positive...
			if (prevpol){
				// Print("in1\n");
				//... and currently negative ...
				if (buf2->data[(i*channels2)+j] < 0.0) {
					// Print("in1-1\n");
					// ... flag as being now in negativeland and exit.
					prevpol = 0;
				}
			} else {
				// if previously in negativeland...
				// Print("in2\n");
				if (buf2->data[(i*channels2)+j] >= 0.0) {
					// ...and now being zero or up, so we write
					// Print("in2-2\n");
					// check it is not the first zero crossing
					if (lastXadd >=0) {
						// check if the lenght will be too long for all repetitions
						wavesetlenght = i - lastXadd;
						if (((wavesetlenght*repetitions)+ writeindex) > frames1) break;

						// write if enough place
						for (int k = 0; k < repetitions; k++){
							for (int l = 0; l < wavesetlenght; l++) {
								buf->data[(writeindex*channels2)+j] = buf2->data[((lastXadd+l)*channels2)+j];
								writeindex++;
							}
						}
					}
					// setting the flag and the new past
					prevpol = 1;
					lastXadd = i;
				}
			}
		}
	}
}

PluginLoad(OfflineFluidDecompositionUGens) {
	ft = inTable;
	DefineBufGen("BufNMF", BufNMF);
}
