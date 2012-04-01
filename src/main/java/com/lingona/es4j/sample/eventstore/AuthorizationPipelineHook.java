package com.lingona.es4j.sample.eventstore;

import com.lingona.es4j.api.Commit;
import com.lingona.es4j.api.IPipelineHook;

/*
import com.lingona.eventstore.joliver.api.Commit;
import com.lingona.eventstore.joliver.api.IPipelineHook;
import com.lingona.eventstore.joliver.core.system.GC;
*/

public class AuthorizationPipelineHook implements IPipelineHook {

    @Override
    public void close() {
        this.dispose(true);
        //GC.suppressFinalize(this);
    }

    // virtual
    protected void dispose(boolean disposing) {
        // no op
    }

    @Override
    public Commit select(Commit committed) {
        // return null if the user isn't authorized to see this commit
        return committed;
    }

    @Override
    public boolean preCommit(Commit attempt) {
        // Can easily do logging or other such activities here
        return true; // true == allow commit to continue, false = stop.
    }

    @Override
    public void postCommit(Commit committed) {
        // anything to do after the commit has been persisted.
    }
}
