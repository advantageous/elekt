package io.advantageous.czarmaker;


import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LeaderElectorTest {


    @Test
    public void test() throws Exception {
        final LeaderElectorMock leaderElectorMock = new LeaderElectorMock();

        final ServiceMock service1 = new ServiceMock("server1", 8001, leaderElectorMock);
        final ServiceMock service2 = new ServiceMock("server2", 8002, leaderElectorMock);
        final ServiceMock service3 = new ServiceMock("server3", 8003, leaderElectorMock);

        final List<ServiceMock> services = Arrays.asList(service1, service2, service3);


        final ExecutorService executorService = Executors.newFixedThreadPool(2);

        services.forEach(serviceMock -> executorService.submit(serviceMock::init));

        Thread.sleep(1000);


        services.forEach(serviceMock -> System.out.println(serviceMock.getLeaderEndpoint()));

        final Endpoint leaderEndpoint = service1.getLeaderEndpoint();

        assertNotNull("The leader endpoint should be set", leaderEndpoint);

        services.forEach(serviceMock -> assertEquals("All nodes have the same leader endpoint",
                leaderEndpoint, serviceMock.getLeaderEndpoint()));

        final List<ServiceMock> leaders =
                services.stream().filter(ServiceMock::isLeader).collect(Collectors.toList());

        assertEquals("There should only be one leader", 1, leaders.size());

    }
}