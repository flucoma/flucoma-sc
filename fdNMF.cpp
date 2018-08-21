// FD_BufNMF, an NRT buffer NMF Processor
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.h"
#include "algorithms/STFT.hpp"
#include "data/FluidTensor.hpp"
#include "clients/nrt/NMFClient.hpp"

//Using statements for fluidtensor
using fluid::FluidTensor;
using fluid::FluidTensorView;
using fluid::nmf::NMFClient;

static InterfaceTable *ft;

void BufNMF(World *world, struct SndBuf *srcBuf, struct sc_msg_iter *msg)
{
	size_t srcFrameCount = srcBuf->frames;
	size_t srcChanCount = srcBuf->channels;

	long dstBufNum = msg->geti();
	long dictBufNum = msg->geti();
	long actBufNum = msg->geti();
	size_t rank = msg->geti();
	size_t iterations = msg->geti();
	size_t fftSize = msg->geti();
	size_t windowSize = msg->geti();
	size_t hopSize = msg->geti();

	SndBuf* dstBuf;
	size_t dstFrameCount;
	size_t dstChanCount;

	if (dstBufNum == -1 && dictBufNum == -1 && actBufNum == -1) {
		Print("fdNMF is not happy because there are no output buffer specified.\n");
		return;
	}

	// check sanity of audio destination buffer
	if (dstBufNum != -1){
		if (dstBufNum >= world->mNumSndBufs){
			Print("fdNMF is not happy because the destination buffer does not exist.\n");
			return;
		}

		dstBuf = world->mSndBufs + dstBufNum;

		if (srcBuf->data == dstBuf->data){
			Print("fdNMF is not happy because the destination buffer is the same as the source buffer.\n");
			return;
		}

		dstFrameCount = dstBuf->frames;
		dstChanCount = dstBuf->channels;

		if (dstChanCount < rank) {
			Print("fdNMF is not happy because the destination buffer has a lower channel count than the number of ranks.\n");
			return;
		}

		if (dstFrameCount < srcFrameCount) {
			Print("fdNMF is not happy because the destination buffer shorter than the source buffer.\n");
			return;
		}
	}

	// make fuildtensorviewers of the SC interleaved input buffer
	FluidTensorView<float,2> in_view ({0,{srcFrameCount, srcChanCount}},srcBuf->data);

	//setup the nmf
	NMFClient nmf(rank ,iterations, fftSize, windowSize, hopSize);

	//for each channels
	// for (int j=0;j<srcChanCount;j++){
	// just processing the first input channel instead of iterating through each channel, yet keeping the mechanism in there.
	for (int j=0;j<1;j++){
		//copies and casts to double the source samples
		FluidTensor<double,1> audio_in(in_view.col(j));
		//Process, with resynthesis
		nmf.process(audio_in,true);
		//Copy output
		if (dstBufNum != -1){
			FluidTensorView<float,2> out_view ({0,{dstFrameCount, dstChanCount}},dstBuf->data);

			for (int i = 0; i < rank; ++i)
			{
				out_view.col(i) = nmf.source(i);
			}
		}
	}
}

PluginLoad(OfflineFluidDecompositionUGens) {
	ft = inTable;
	DefineBufGen("BufNMF", BufNMF);
}
