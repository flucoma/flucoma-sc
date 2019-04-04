//this patch requests a folder and will iterate through all accepted audiofiles and concatenate them in the destination buffer. It will also yield an array with the numFrame where files start in the new buffer.

(
var tempbuf,dest=0, fileNames;
c = [0];

FileDialog.new({|selection|
	var total, totaldur = 0, maxchans = 0;
	t = Main.elapsedTime;
	fileNames = PathName.new(selection[0])
	.entries
	.select({|f|
		[\wav, \WAV, \mp3,\aif].includes(f.extension.asSymbol);});
	total = fileNames.size();
	fileNames.do({arg fp;
		SoundFile.use(fp.asAbsolutePath , {
			arg file;
			var dur = file.numFrames;
			c = c.add(dur);
			totaldur = totaldur + dur;
			maxchans = maxchans.max(file.numChannels);
		});
	});
	Routine{
		b = Buffer.alloc(s,totaldur,maxchans);
		s.sync;
		fileNames.do{|f, i|
			f.postln;
			("Loading"+(i+1)+"of"+total).postln;
			tempbuf = Buffer.read(s, f.asAbsolutePath,action:{FluidBufCompose.process(s,tempbuf,destStartFrame:c[i],destination:b);});
			s.sync;
		};
		("loading buffers done in" + (Main.elapsedTime - t).round(0.1) + "seconds.").postln;
	}.play;
}, fileMode:2);
)

b.plot
c.postln
b.play

Buffer.freeAll