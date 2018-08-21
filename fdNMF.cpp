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

	size_t dstBufNum = msg->geti();
	size_t rank = msg->geti();
	size_t iterations = msg->geti();
	size_t fftSize = msg->geti();
	size_t windowSize = msg->geti();
	size_t hopSize = msg->geti();

	if (dstBufNum >= world->mNumSndBufs){
		Print("fdNMF is not happy because the destination buffer does not exist.\n");
		return;
	}

	SndBuf* dstBuf = world->mSndBufs + dstBufNum;

	if (srcBuf->data == dstBuf->data){
		Print("fdNMF is not happy because the destination buffer is the same as the source buffer.\n");
		return;
	}

	size_t dstFrameCount = dstBuf->frames;
	size_t dstChanCount = dstBuf->channels;

	if (dstChanCount < rank) {
		Print("fdNMF is not happy because the destination buffer has a lower channel count than the number of ranks.\n");
		return;
	}

	// check size of dstBuff
	if (dstFrameCount < srcFrameCount) {
		Print("fdNMF is not happy because the destination buffer shorter than the source buffer.\n");
		return;
	}

	// make fuildtensorviewers of my SC interleaved buffers
	FluidTensorView<float,2> in_view ({0,{srcFrameCount, srcChanCount}},srcBuf->data);
	FluidTensorView<float,2> out_view ({0,{dstFrameCount, dstChanCount}},dstBuf->data);

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
		for (int i = 0; i < rank; ++i)
		{
			out_view.col(i) = nmf.source(i);
		}
	}
}

PluginLoad(OfflineFluidDecompositionUGens) {
	ft = inTable;
	DefineBufGen("BufNMF", BufNMF);
}
