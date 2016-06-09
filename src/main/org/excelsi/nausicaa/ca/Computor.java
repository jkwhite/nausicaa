package org.excelsi.nausicaa.ca;


public final class Computor<S,T> {
    private final Encoder<S> _inputEncoder;
    private final Decoder<T> _outputDecoder;
    private final CA _function;


    public Computor(Encoder<S> inputEncoder, CA function, Decoder<T> outputDecoder) {
        _inputEncoder = inputEncoder;
        _function = function;
        _outputDecoder = outputDecoder;
    }

    public T compute(S s) {
        final CA f = _function.initializer(new EncodingInitializer(_inputEncoder, s));
        final T t = _outputDecoder.decode(f.createPlane());
        return t;
    }
}
