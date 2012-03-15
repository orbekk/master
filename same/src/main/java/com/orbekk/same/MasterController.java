package com.orbekk.same;

public interface MasterController {
    void enableMaster(State lastKnownState);
    void disableMaster();
}
