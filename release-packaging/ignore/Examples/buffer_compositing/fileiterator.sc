//destination buffer
(
b = Buffer.new();
c = Array.new();
)

//this patch requests a folder and will iterate through all accepted audiofiles and concatenate them in the destination buffer. It will also yield an array with the numFrame where files start in the new buffer.

(
var tempbuf,dest=0, fileNames;

FileDialog.new({|selection|
    var total;
	t = Main.elapsedTime;
    fileNames = PathName.new(selection[0])
    .entries
    .select({|f|
        [\wav, \WAV, \mp3,\aif].includes(f.extension.asSymbol);});
    total = fileNames.size();
    Routine{
        fileNames.do{|f, i|
            f.postln;
			("Loading"+(i+1)+"of"+total).postln;
            tempbuf = Buffer.read(s,f.asAbsolutePath);
            s.sync;
            c = c.add(dest);
            FluidBufCompose.process(s,tempbuf,destStartFrame:dest,destination:b);
            s.sync;
            dest = b.numFrames;
        };
		("loading buffers done in" + (Main.elapsedTime - t).round(0.1) + "seconds.").postln;
    }.play;
}, fileMode:2);
)

b.plot
c.postln
b.play
