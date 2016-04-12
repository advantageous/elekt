package io.advantageous.czarmaker;

import io.advantageous.reakt.Callback;
import io.advantageous.reakt.Stream;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class LeaderElectorMock implements LeaderElector, LeaderSetter {


    private final AtomicReference<Endpoint> leader = new AtomicReference<>();
    private final CopyOnWriteArrayList<Stream<Endpoint>> listeners = new CopyOnWriteArrayList<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void selfElect(final Endpoint leader, final Callback<Boolean> callback) {

        Objects.requireNonNull(leader, "Leader can't be null");

        executorService.submit(() -> {

            final Endpoint oldEndpoint = this.leader.get();
            boolean changed;

            if (oldEndpoint == null) {
                changed = this.leader.compareAndSet(null, leader);
            } else {
                changed = false;
            }

            if (oldEndpoint == null || changed) {
                listeners.stream().forEach(endpointStream ->
                        endpointStream.reply(leader, false,
                                () -> listeners.remove(endpointStream)));
            }
            callback.reply(changed);

        });
    }

    @Override
    public void leadershipChangeNotice(Stream<Endpoint> stream) {
        executorService.submit(() -> {
            listeners.add(stream);
        });
    }


    @Override
    public void getLeader(Callback<Endpoint> callback) {
        executorService.submit(() -> {
            callback.reply(leader.get());
        });
    }

    /**
     * This is for testing only.
     *
     * @param leader   endpoint new leader
     * @param callback callback
     */
    @Override
    public void setLeader(final Endpoint leader, final Callback<Boolean> callback) {
        executorService.submit(() -> {
            this.leader.set(leader);
            listeners.stream().forEach(endpointStream ->
                    endpointStream.reply(leader, false,
                            () -> listeners.remove(endpointStream)));
            callback.reply(true);
        });
    }
}
