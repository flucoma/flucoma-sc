s.reboot

//Quickie test of buffer allocation working
//Read a sound file
a = Buffer.read(s,"/Users/owen/Desktop/denoise_stn/sources/01-mix.wav");
//Pass buffer to this, along with a rank. It will allocate a new buffer,
//size it appropriately (in our server code) and return the new object
f = FDBufferExperiments.allocMatch(s,a,rank:5);
//Make sure everything is kosher:
a.query
f.query
