(
fork{
	~dict = Dictionary.newFrom([
		"cols",5,
		"data",Dictionary.newFrom(Array.fill(20,{
			arg i;
			[(i/2).asInteger,Array.fill(5,{rrand(-1.0,1.0)})]
		}).flatten)
	]);
	~ds = FluidDataSet(s).load(~dict);
	~pca = FluidPCA(s,5);
	~pca.fit(~ds);
	b = Buffer.alloc(s,5);
	e = 5.collect{rrand(-1.0,1.0)}.postln;
	s.sync;
	b.setn(0,e);
	c = Buffer.alloc(s,5);
	d = Buffer();
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

~pca.inverseTransformPoint(c,d);

(
d.loadToFloatArray(action:{
	arg fa;
	fa.postln;
	(fa - e).postln;
});
)