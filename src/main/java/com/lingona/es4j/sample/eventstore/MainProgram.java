package com.lingona.es4j.sample.eventstore;

import com.lingona.es4j.api.*;
import com.lingona.es4j.api.persistence.IPersistStreams;
import com.lingona.es4j.core.OptimisticEventStore;
import com.lingona.es4j.core.OptimisticPipelineHook;
import com.lingona.es4j.core.persistence.inmemory.InMemoryPersistenceFactory;
import java.util.*;
/*
import com.lingona.eventstore.joliver.api.*;
import com.lingona.eventstore.joliver.api.Persistence.IPersistStreams;
import com.lingona.eventstore.joliver.core.Dispatcher.DelegateMessageDispatcher;
import com.lingona.eventstore.joliver.core.Dispatcher.NullDispatcher;
import com.lingona.eventstore.joliver.core.OptimisticEventStore;
import com.lingona.eventstore.joliver.core.OptimisticEventStream;
import com.lingona.eventstore.joliver.core.OptimisticPipelineHook;
import com.lingona.eventstore.joliver.core.Persistence.InMemoryPersistence.InMemoryPersistenceEngine;
import com.lingona.eventstore.joliver.core.Persistence.InMemoryPersistence.InMemoryPersistenceFactory;
import com.lingona.eventstore.joliver.persistence.sqlpersistence.system.TransactionScope;
import com.lingona.eventstore.joliver.wireup.Wireup;
*/
// using System.Transactions;
// using Dispatcher;

public class MainProgram {

    private static final UUID streamId = UUID.randomUUID(); // aggregate identifier

    private static final byte[] encryptionKey = new byte[] {
        0x0, 0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf
    };

    private static IStoreEvents store;

    public static void main(String [ ] args) {

        IPersistStreams            persistence  = new InMemoryPersistenceFactory().build();
        IPipelineHook              pipelineHook = new OptimisticPipelineHook();
        store = new OptimisticEventStore(persistence, Arrays.asList(pipelineHook));
        //store = eventStore/*wireupEventStore()*/;

        TransactionScope scope = new TransactionScope();
        openOrCreateStream();
        appendToStream();
        takeSnapshot();
        loadFromSnapshotForwardAndAppend();
        scope.complete();

        System.out.println(Resources.PressAnyKey);
        new Scanner(System.in).nextByte();
    }

    private static IStoreEvents wireupEventStore() {

        return Wireup.init()
                     .logToOutputWindow()
                     .usingSqlPersistence("EventStore")
                         .enlistInAmbientTransaction() // two-phase commit
                         .initializeStorageEngine()
                     .usingJsonSerialization()
                         .compress()
                         .encryptWith(encryptionKey)
                     .hookIntoPipelineUsing(new AuthorizationPipelineHook())
                     //.usingAsynchronousDispatchScheduler()
                     //.dispatchTo(new DelegateMessageDispatcher(dispatchCommit))
                     .build();
    }

    private static void dispatchCommit(Commit commit) {
        // This is where we'd hook into our messaging infrastructure, such as NServiceBus,
        // MassTransit, WCF, or some other communications infrastructure.
        // This can be a class as well--just implement IDispatchCommits.
        try {
            for (EventMessage event : commit.getEvents()) {
                System.out.println(Resources.MessagesDispatched + ((SomeDomainEvent)event.getBody()).getValue());
            }
        }
        catch (Exception ex) {
            System.out.println(Resources.UnableToDispatch);
        }
    }

    private static void openOrCreateStream() {
        // we can call CreateStream(StreamId) if we know there isn't going to be any data.
        // or we can call OpenStream(StreamId, 0, int.MaxValue) to read all commits,
        // if no commits exist then it creates a new stream for us.
        IEventStream stream = store.openStream(streamId, 0, Integer.MAX_VALUE);

        SomeDomainEvent event = new SomeDomainEvent("Initial event.");

        stream.add(new EventMessage(event));
        stream.commitChanges(UUID.randomUUID());
    }

    private static void appendToStream() {
        IEventStream stream = store.openStream(streamId, Integer.MIN_VALUE, Integer.MAX_VALUE);

        SomeDomainEvent event = new SomeDomainEvent("Second event.");

        stream.add(new EventMessage(event));
        stream.commitChanges(UUID.randomUUID());
    }

    private static void takeSnapshot() {
        AggregateMemento memento = new AggregateMemento("snapshot");
        store.advanced().addSnapshot(new Snapshot(streamId, 2, memento));
    }

    private static void loadFromSnapshotForwardAndAppend() {
        Snapshot latestSnapshot = store.advanced().getSnapshot(streamId, Integer.MAX_VALUE);

	IEventStream stream = store.openStream(latestSnapshot, Integer.MAX_VALUE);

	SomeDomainEvent event = new SomeDomainEvent("Third event (first one after a snapshot).");

        stream.add(new EventMessage(event));
        stream.commitChanges(UUID.randomUUID());
    }
}
