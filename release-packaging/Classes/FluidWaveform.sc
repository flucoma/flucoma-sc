FluidWaveform {

	*new {
		arg audio_buf, slices_buf, bounds;
		^super.new.init(audio_buf,slices_buf, bounds);
	}

	init {
		arg audio_buf, slices_buf, bounds;
		Task{
			var path = "%%_%_FluidWaveform.wav".format(PathName.tmp,Date.localtime.stamp,UniqueID.next);
			var sfv, win, userView;

			bounds = bounds ? Rect(0,0,800,200);
			win = Window("FluidWaveform",bounds);
			audio_buf.write(path,"wav");

			audio_buf.server.sync;

			sfv = SoundFileView(win,Rect(0,0,bounds.width,bounds.height));
			sfv.readFile(SoundFile(path));
			sfv.gridOn_(false);

			File.delete(path);

			if(slices_buf.notNil,{
				slices_buf.loadToFloatArray(action:{
					arg slices_fa;

					userView = UserView(win,Rect(0,0,bounds.width,bounds.height))
					.drawFunc_({
						slices_fa.do{
							arg start_samp;
							var x = start_samp.linlin(0,audio_buf.numFrames,0,bounds.width);
							Pen.line(Point(x,0),Point(x,bounds.height));
							Pen.color_(Color.red);
							Pen.stroke;
						};
					});
				});
			});

			win.front;
		}.play(AppClock);
	}
}

