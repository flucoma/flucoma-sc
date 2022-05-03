(
fork{
	~dict = Dictionary.newFrom([
		"cols",5,
		"data",Dictionary.newFrom(Array.fill(20,{
			arg i;
			[(i/2).asInteger,Array.fill(5,{rrand(-1.0,1.0)})]
		}).flatten)
	]);
	// ~dict.at("data").at(0).postln;
	~ds = FluidDataSet(s).load(~dict);
	~pca = FluidPCA(s,5);
	~pca.fit(~ds);
	b = Buffer.alloc(s,5);
	s.sync;
	b.setn(0,Array.fill(5,{rrand(-1.0,1.0)}).postln);
	// ~ds.getPoint(0,b);
	c = Buffer.alloc(s,5);
}
)

b.plot

~pca.transformPoint(b,c);

(
c.loadToFloatArray(action:{
	arg fa;
	fa.postln;
});
)

~pca.inverseTransformPoint(c,b);

(
b.loadToFloatArray(action:{
	arg fa;
	fa.postln;
});
)