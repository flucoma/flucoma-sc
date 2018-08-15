// FD_BufNMF, an NRT buffer NMF Processor
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.h"
#include "STFT.hpp"
#include "FluidTensor.hpp"
#include "fluid_client_nmf.h"
#include "fluid_nmf_tilde_util.h"

//Using statements for fluidtensor
using fluid::FluidTensor;
using fluid::FluidTensorView;
using fluid::nmf::error_strings;
using fluid::nmf::NMFClient;

static InterfaceTable *ft;

//namespace to gaffatape
namespace gaffatape {

void BufNMF(World *world, struct SndBuf *dstBuf, struct sc_msg_iter *msg)
{
	size_t dstFrameCount = dstBuf->frames;
	size_t dstChanCount = dstBuf->channels;

	uint32 srcBufNum = msg->geti();
	long rank = msg->geti();
	long iterations = msg->geti();
	long fftSize = msg->geti();
	long windowSize = msg->geti();
	long hopSize = msg->geti();

	if (srcBufNum >= world->mNumSndBufs){
		Print("fdNMF is not happy because the source buffer does not exist.\n");
		return;
	}

	SndBuf* srcBuf = world->mSndBufs + srcBufNum;

	if (srcBuf->data == dstBuf->data){
		Print("fdNMF is not happy because the source buffer is the same as the destination buffer.\n");
		return;
	}

	size_t srcFrameCount = srcBuf->frames;
	size_t srcChanCount = srcBuf->channels;

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
}
