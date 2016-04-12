package io.advantageous.czarmaker;

import io.advantageous.reakt.Expected;
import io.advantageous.reakt.promise.Promise;
import io.advantageous.reakt.promise.Promises;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ServiceMock {


    private final String host;
    private final int port;
    private final LeaderElector leaderElector;
    private AtomicBoolean amILeader = new AtomicBoolean();

    private AtomicReference<Endpoint> leaderEndpoint = new AtomicReference<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    public ServiceMock(String host, int port, LeaderElector leaderElector) {
        this.host = host;
        this.port = port;
        this.leaderElector = leaderElector;
    }


    public void init() {
        executorService.submit(()->{
            Promise<Endpoint> getLeaderPromise = Promises.<Endpoint>promise();
            getLeaderPromise.thenExpect(expected ->
                    expected
                            .ifEmpty(this::nominateSelf)
                            .ifPresent(endpoint -> leaderEndpoint.set(endpoint)))
                            .catchError(Throwable::printStackTrace);
            leaderElector.getLeader(getLeaderPromise);

            registerForLeadershipNotices();
        });
    }

    private void registerForLeadershipNotices() {
        leaderElector.leadershipChangeNotice(result ->
                result
                        .thenExpect(this::checkIfThisServiceIsLeader)
                        .catchError(Throwable::printStackTrace)
        );
    }

    private void checkIfThisServiceIsLeader(Expected<Endpoint> expectedEndpoint) {
        expectedEndpoint.ifEmpty(() -> nominateSelf()) //If empty then nominate this service
                .ifPresent(endpoint -> {
                    amILeader.set(endpoint.getHost().equals(host) && endpoint.getPort()==port);
                    leaderEndpoint.set(endpoint);
                });
    }

    private void nominateSelf() {
        final Promise<Boolean> selfElectPromise = Promises.<Boolean>promise();
        selfElectPromise.then((elected) -> {
            amILeader.set(elected);
        }).catchError(Throwable::printStackTrace);

        leaderElector.selfElect(new Endpoint(host, port), selfElectPromise);

    }

    public boolean isLeader() {
        return amILeader.get();
    }

    public Endpoint getLeaderEndpoint() {
        return leaderEndpoint.get();
    }
}
