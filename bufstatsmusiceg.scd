

(
b = Buffer.read(s,File.realpath(FluidBufStats.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/Tremblay-ASWINE-ScratchySynth-M.wav");
~slicesBuffer = Buffer.new(s);
~pitchBuffer = Buffer.new(s);
~statsBuffer = Buffer.new(s);

~windowSize = 1024;
~hopSize = 512;
)


(
//split in various chunks, collecting the indices in an array
FluidBufOnsetSlice.process(s,b, minSliceLength: 10, metric: 9, threshold: 0.4, filterSize: 7, indices: ~slicesBuffer, action:{
    ~slicesBuffer.loadToFloatArray(action: {|a|
       ~slices = a;
      if(~slices.size == 1 && ~slices[0] == -1) {"WARNING: No slices found".postln; ~slices=[];}{};
        //Make sure we have index points at the start and end of the file (inefficiently)
        ~slices = [0] ++ ~slices ++ [b.numFrames];
        //no duplciates
        ~slices.asSet.asArray.sort;
        ~slices.postln;
    })
});
)
//describe the whole input too, here using pitch, and collecting the values in an array, dismissing the (interleaved) confidence.
FluidBufPitch.process(s,b,features:~pitchBuffer,windowSize:~windowSize,hopSize:~hopSize,action:{
    ~pitchBuffer.loadToFloatArray(action: {|array|
        ~pitches = array.unlace(2)[0];
        ~pitches.postln;
    })
});

~pitches

// iterate through each slice, taking the median of the first derivative of the pitch of each
(
~stats = Array.new;
Routine({
	~slices.doAdjacentPairs({
		arg start,end;
        var duration;
        duration.postln;
        //convert slice points to frame numbers (divide by analysis hop size)
        start = (start / 512).asInt;
        duration = ((end - start)/512).max(2).asInt; //we don't want to take the stats of single frames

		FluidBufStats.process(s,~pitches,start,duration,0,1,~statsBuffer,1, action:
			{~statsBuffer.loadToFloatArray(action: {
				arg array;
				~stats = ~stats.add(array[5]);
				"% % %\n".postf(start,duration, array[5]);
		})});
	});
	"Done".postln;
}).play;
)

~sortedSlices = Array.newFrom(e).slide(2,1).clump(2)
//obtain the order of indices that would sort the stats
(
SynthDef(\help_pitchsort,
    { |buf,start,end|
        var l, sig;
        l = Line.ar(start,end,(end-start)*SampleDur.ir,doneAction:Done.freeSelf);
        sig = BufRd.ar(1,buf,l);
        Out.ar(0,sig)
}).add;
)
b.play
g.size
g.order
g[39]
h = Array.newFrom(e).slide(2,1).clump(2)[g.order]
Array.newFrom(e).slide(2,1).clump(2)[39]

~potch = Buffer.new

FluidBufPitch.process(s,b,features:~potch,


	action:{c.loadToFloatArray(action: {|array| f = array.unlace(2)[0]; f.postln;})});


(
~ps = Pseq(~sortedSlices,inf).asStream;
Routine({

	~sortedSlices.do({
		~bounds = ~ps.next;
		Synth(\help_pitchsort,[\buf,b,\start,~bounds[0],\end,~bounds[1]]);
        ~bounds.postln;
        ((~bounds[1]-~bounds[0])/b.sampleRate).wait;
	})
}).play
)