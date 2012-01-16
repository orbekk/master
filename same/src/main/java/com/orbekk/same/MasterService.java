package com.orbekk.same;

public interface MasterService {
    void joinNetworkRequest(String clientUrl);
    boolean updateStateRequest(String component, String newData, long revision);
}
