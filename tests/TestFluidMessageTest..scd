~messageTest
a = {~messageTest = FluidMessageTest.kr}.play
~messageTest.testReturnStrings(nil,a.nodeID,{|msg| msg.postln});
~messageTest.testReturnNumbers(nil,a.nodeID,{|msg| msg.postln});
~messageTest.testReturnOneString(nil,a.nodeID,{|msg| msg.postln});
~messageTest.testReturnOneNumber(nil,a.nodeID,{|msg| msg.postln});
b = Buffer.read(s,File.realpath(FluidMessageTest.class.filenameSymbol).dirname.withTrailingSlash ++ "../AudioFiles/Nicol-LoopE-M.wav");
~messageTest.testAccessBuffer(nil,a.nodeID,b,{|msg| msg.postln});
b.numFrames
~messageTest.testPassString(nil,a.nodeID,'hello, you big lovely server',1,2,3,{'testPassString Done'.postln});

// OSCFunc.trace(true)
// OSCFunc.trace(false)