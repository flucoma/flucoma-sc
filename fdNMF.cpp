// FD_BufNMF, an NRT buffer NMF Processor
// A tool from the FluCoMa project, funded by the European Research Council (ERC) under the European Union’s Horizon 2020 research and innovation programme (grant agreement No 725899)

#include "SC_PlugIn.h"
#include <vector>
#include <algorithm>
#include "stft.h"
#include "nmf.h"
#include <Eigen/Dense>


//Using statements for eigenmf. These will change
using stft::STFT;
using stft::ISTFT;
using stft::Spectrogram;
using stft:: audio_buffer_t;
using stft:: magnitude_t;
using nmf::NMF;
using nmf::NMFModel;
using Eigen::MatrixXcd;
using Eigen::MatrixXd;
using std::complex;
using util::stlVecVec2Eigen;
using util::Eigen2StlVecVec;
using std::numeric_limits;

static InterfaceTable *ft;

//namespace to gaffatape
namespace gaffatape {

void BufNMF(World *world, struct SndBuf *dstBuf, struct sc_msg_iter *msg)
{
	int dstFrameCount = dstBuf->frames;
	int dstChanCount = dstBuf->channels;

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

	int srcFrameCount = srcBuf->frames;
	int srcChanCount = srcBuf->channels;

	if (dstChanCount < rank) {
		Print("fdNMF is not happy because the destination buffer has a lower channel count than the number of ranks.\n");
		return;
	}

	// check size of dstBuff
	if (dstFrameCount < srcFrameCount) {
		Print("fdNMF is not happy because the destination buffer shorter than the source buffer.\n");
		return;
	}

	// make a vector of doubles for the samples
	std::vector<double> audio_in(srcFrameCount);

	//copied as is from max source (setting up the different variables and processes)
	STFT stft(windowSize, fftSize, hopSize);
	NMF nmfProcessor(rank, iterations);
	ISTFT istft(windowSize, fftSize, hopSize);

	//for each channels
	// for (int j=0;j<srcChanCount;j++){
	// just processing the first input channel instead of iterating through each channel, yet keeping the mechanism in there.
	for (int j=0;j<1;j++){
		//copies and casts to double the source samples
		for (int i=0;i<srcFrameCount;i++){
				audio_in[i] = srcBuf->data[(i*srcChanCount)+j];
		}

		Spectrogram spec = stft.process(audio_in);
		magnitude_t mag = spec.magnitude();
		NMFModel decomposition = nmfProcessor.process(mag);
		MatrixXd W = stlVecVec2Eigen<double>(decomposition.W);
		MatrixXd H = stlVecVec2Eigen<double>(decomposition.H);

		MatrixXd V = W * H;

		for (int i = 0; i < rank; i++)
		{
				MatrixXd source = W.col(i) * H.row(i);
				MatrixXd filter = source.cwiseQuotient(V);
				MatrixXcd specMatrix = stlVecVec2Eigen(spec.mData);
				specMatrix = specMatrix.cwiseProduct(filter);
				Spectrogram resultS(Eigen2StlVecVec<complex<double>>(specMatrix));

				audio_buffer_t result = istft.process(resultS);

				//writes the output
				for (int k=0;k<srcFrameCount;k++){
						dstBuf->data[(k*rank)+i] = (float)result[k];
				}
		}
	}
}

PluginLoad(OfflineFluidDecompositionUGens) {
	ft = inTable;
	DefineBufGen("BufNMF", BufNMF);
}
}
