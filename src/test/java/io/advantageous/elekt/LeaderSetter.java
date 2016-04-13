package io.advantageous.elekt;


import io.advantageous.reakt.Callback;

public interface LeaderSetter {
    void setLeader(Endpoint endpoint, Callback<Boolean> callback);
}
