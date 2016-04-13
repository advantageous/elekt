# Elekt
Elekt is a nice set of interfaces for [Leader Election](https://www.consul.io/docs/guides/leader-election.html).

There is one [Elekt Consul implementation](http://advantageous.github.io/elekt-consul/) of this interface that uses [Consul](https://www.consul.io/). You could use the interface to implement leader election with [zookeeper](https://zookeeper.apache.org/) or [etcd](https://github.com/coreos/etcd). Consul and etcd use the RAFT algorithm to present a reliable kv storage (Zookeeper uses a similar technique as Consul and etcd).

Elekt uses [Reakt](http://advantageous.github.io/reakt/), a Java reactive, [streaming](https://github.com/advantageous/reakt/wiki/Stream) API, with [callbacks](https://github.com/advantageous/reakt/wiki/Callback) and [promises](https://github.com/advantageous/reakt/wiki/Promise) that is Java 8 and Lambda friendly.

Elekt also uses [QBit microservices](http://advantageous.github.io/qbit/) as its HTTP/IO lib.

(This project was called Czar Maker, but was renamed to Elekt.)

##Getting Started
This library is just interfaces, to use Elekt on your project you will need the [Elekt Consul implementation](http://advantageous.github.io/elekt-consul/).

#### maven
```xml
<dependency>
    <groupId>io.advantageous.elekt</groupId>
    <artifactId>elekt</artifactId>
    <version>0.1.0.RELEASE</version>
</dependency>
```

#### gradle 
```java
compile 'io.advantageous.elekt:elekt:0.1.0.RELEASE'
```


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

## Related projects
* [QBit Reactive Microservices](http://advantageous.github.io/qbit/)
* [Reakt Reactive Java](http://advantageous.github.io/reakt)
* [Reakt Guava Bridge](http://advantageous.github.io/reakt-guava/)
* [QBit Extensions](https://github.com/advantageous/qbit-extensions)
* [Reactive Microservices](http://www.mammatustech.com/reactive-microservices)


    
    
