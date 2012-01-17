package com.orbekk.same;

public interface MasterService {
    void joinNetworkRequest(String clientUrl) throws Exception;
    boolean updateStateRequest(String component, String newData, long revision)
            throws Exception;
}
