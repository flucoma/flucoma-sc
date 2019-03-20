//destination buffer
b = Buffer.alloc(s,1);
c = Array.new();

//this patch requests a folder and will iterate through all accepted audiofiles and concatenate them in the destination buffer. It will also yield an array with the numFrame where files start in the new buffer.

(
var tempbuf,dest=0, fileNames;

FileDialog.new({|selection|
    var total;
    fileNames = PathName.new(selection[0])
    .entries
    .select({|f|
        [\wav, \WAV, \mp3,\aif].includes(f.extension.asSymbol);});
    total = fileNames.size() - 1;
    Routine{
        fileNames.do{|f, i|
            f.postln;
            ("Loading"+i+"of"+total).postln;
            tempbuf = Buffer.read(s,f.asAbsolutePath);
            s.sync;
            c = c.add(dest);
            FluidBufCompose.process(s,tempbuf.bufnum,dstStartAtA:dest,srcBufNumB:b.bufnum,dstBufNum:b.bufnum);
            s.sync;
            b.updateInfo();
            s.sync;
            dest = b.numFrames;
        };
        "load buffers done".postln;
    }.play;
}, fileMode:2);
)

b.plot
c.postln
b.play
