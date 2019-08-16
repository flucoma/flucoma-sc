
~provider
~subscriber
a = {~subscriber = FluidSubscriberTest.kr(\bollocks)}.play
b = {~provider = FluidProviderTest.kr(\bollocks)}.play
~provider.addPoint(Server.default,b,['foo', 1,2],{'Added'.postln})
~subscriber.providerLookup(Server.default,a,['foo'],{|msg| msg.postln})
~provider.updatePoint(Server.default,b,['foo', 3,4],{'Upadted'.postln})
~subscriber.providerLookup(Server.default,a,['foo'],{|msg| msg.postln})
~provider.deletePoint(Server.default,b,['foo'],{'Deleted'.postln})
~subscriber.providerLookup(Server.default,a,['foo'],{|msg| msg.postln})

