# Czar Maker
Czar Maker is a nice set of interfaces for [Leader Election](https://www.consul.io/docs/guides/leader-election.html).

There is one [Czar Maker Consul implementation](http://advantageous.github.io/czar-maker-consul/) of this interaface that use Consul.

Czar uses [Reakt](https://github.com/advantageous/reakt), a reactive streaming, reactive API with callbacks and promises.


#### Example usage
```java
public class MyService {

    private final String host;
    private final int port;
    private final LeaderElector leaderElector;
    private AtomicBoolean amILeader = new AtomicBoolean();
    private AtomicReference<Endpoint> leaderEndpoint = new AtomicReference<>();
    
    public void init() {
            Promise<Endpoint> getLeaderPromise = Promises.<Endpoint>promise();
            
            /* Call elect new leader. */
            getLeaderPromise.thenExpect(expected ->
                    expected
                            .ifEmpty(this::nominateSelf)
                            .ifPresent(endpoint -> leaderEndpoint.set(endpoint)))
                            .catchError(Throwable::printStackTrace);
            leaderElector.getLeader(getLeaderPromise);
            
            /* Register for stream of leadership changes. */
            registerForLeadershipNotices();
      
    }
    
    
  private void registerForLeadershipNotices() {
        leaderElector.leadershipChangeNotice(result ->
                result
                        .thenExpect(this::checkIfThisServiceIsLeader)
                        .catchError(Throwable::printStackTrace)
        );
    }

    //Handles leadership change stream. 
    private void checkIfThisServiceIsLeader(Expected<Endpoint> expectedEndpoint) {
        expectedEndpoint.ifEmpty(() -> nominateSelf()) //If empty then nominate this service
                .ifPresent(endpoint -> {
                    amILeader.set(endpoint.getHost().equals(host) && endpoint.getPort()==port);
                    leaderEndpoint.set(endpoint);
                });
    }

    //Attempt to Nominate self if there is no leader
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
```

    
    
