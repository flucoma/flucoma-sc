//this patch requests a folder and will iterate through all accepted audiofiles and concatenate them in the destination buffer. It will also yield an array with the numFrame where files start in the new buffer.

(
var fileNames;
c = [];

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
			c = c.add(totaldur);
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
			Buffer.read(s, f.asAbsolutePath,action:{arg tempbuf; FluidBufCompose.process(s,tempbuf,destination:b,destStartFrame:c[i],action:{tempbuf.free});});
		};
			s.sync;
		("loading buffers done in" + (Main.elapsedTime - t).round(0.1) + "seconds.").postln;
	}.play;
}, fileMode:2);
)

b.plot
c.postln
b.play


{PlayBuf.ar(1,b.bufnum,startPos:c[15])}.play

Buffer.freeAll