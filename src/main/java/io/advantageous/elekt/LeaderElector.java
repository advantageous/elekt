package io.advantageous.elekt;

import io.advantageous.reakt.Callback;
import io.advantageous.reakt.Stream;


/**
 * Leadership election works like this.
 *
 *
 * A service comes online, it calls {@code getLeader} to see if it is the leader.
 * If the service is not the leader, then service then calls {@code selfElect} to try to make itself the leader.
 *
 * Once leadership has been established, {@code leadershipChangeNotice} is  called to see if any changes in leadership
 * has occurred.
 *
 * {@code LeaderElector} tracks the name of the leaders. There is a LeaderElector per service name.
 *
 */
public interface LeaderElector {

    /**
     * Attempt to elect this service as leader.
     * Returns true if successful, and false if not successful.
     * @param endpoint endpoint describes the host and port of the leader.
     * @param callback callback
     */
    void selfElect(final Endpoint endpoint, final Callback<Boolean> callback);

    /**
     *
     * This will send leadership changes as the occur over the stream.
     *
     * @param callback callback returns new leader.
     */
    void leadershipChangeNotice(final Stream<Endpoint> callback);

    /**
     *
     * This will come back quickly with a new Leader.
     * If no Endpoint is returned in the callback then there is no leader.
     *
     * @param callback callback returns new leader.
     */
    void getLeader(final Callback<Endpoint> callback);


}
