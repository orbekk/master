package com.orbekk.same;

public interface MasterController {
    void enableMaster(State lastKnownState, int masterId);
    void disableMaster();
}
