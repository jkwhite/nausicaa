package org.excelsi.nausicaa.ca;


import java.util.Random;


public class RetryingMutationStrategy implements MutationStrategy {
    private final MutationStrategy _delegate;
    private final MutationStrategy _fallback;
    private final int _retries;


    public RetryingMutationStrategy(MutationStrategy delegate, MutationStrategy fallback, int retries) {
        _delegate = delegate;
        _fallback = fallback;
        _retries = retries;
    }

    @Override public CA mutate(final CA ca, final Random rand, final MutationFactor f) {
        for(int i=0;i<_retries;i++) {
            try {
                return _delegate.mutate(ca, rand, f);
            }
            catch(MutationFailedException e) {
                //e.printStackTrace();
            }
        }
        //throw new MutationFailedException("failed after "+_retries+" tries: "+e.getMessage(), e);
        return _fallback.mutate(ca, rand, f);
    }
}
