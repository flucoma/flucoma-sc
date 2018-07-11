// FD_BufNMF, an NRT buffer NMF Processor
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.h"
#include <vector>

static InterfaceTable *ft;

void BufNMF(World *world, struct SndBuf *dstBuf, struct sc_msg_iter *msg)
{
	int dstFrameCount = dstBuf->frames;
	int dstChanCount = dstBuf->channels;

	uint32 srcBufNum = msg->geti();
	int repetitions = msg->geti();
	uint32 rank = 5;

	if (srcBufNum >= world->mNumSndBufs){
		Print("fdNMF is not happy because the source buffer does not exist.\n");
		return;
	}

	SndBuf* srcBuf = world->mSndBufs + srcBufNum;

	if (srcBuf->data == dstBuf->data){
		Print("fdNMF is not happy because the source buffer is the same as the destination buffer.\n");
		return;
	}

	int srcFrameCount = srcBuf->frames;
	int srcChanCount = srcBuf->channels;

	Print("dstChanCount = %d\n",dstChanCount);

	if (dstChanCount < rank) {
		Print("fdNMF is not happy because the destination buffer has a lower channel count than the number of ranks.\n");
		return;
	}

	// make a vector of doubles for the samples
	std::vector<double> tmp_vec(srcFrameCount);

	//for each channels
	for (int j=0;j<srcChanCount;j++){
		//copies and casts to double the source samples
		for (int i=0;i<srcFrameCount;i++){
				tmp_vec[i] = srcBuf->data[(i*srcChanCount)+j];
		}

		// //dumb vector process with c++ syntax
		// for(double value : tmp_vec){
		// 	value *= -1;
		// }

		//dumb vector process with c++ syntax
		for(int i=0;i<tmp_vec.size();i++){
			tmp_vec[i] *= -1;
		}

		//writes the output
		for (int i=0;i<srcFrameCount;i++){
				dstBuf->data[(i*srcChanCount)+j] = (float)tmp_vec[i];
		}
	}
}

PluginLoad(OfflineFluidDecompositionUGens) {
	ft = inTable;
	DefineBufGen("BufNMF", BufNMF);
}
