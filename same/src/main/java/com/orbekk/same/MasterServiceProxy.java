package com.orbekk.same;

public class MasterServiceProxy implements MasterService {
    public static class MasterDeactivatedException extends Exception {
        public MasterDeactivatedException() {
        }
    }
    
    private MasterService masterService = null;
    
    public MasterServiceProxy() {
    }

    public MasterServiceProxy(MasterService masterService) {
        this.masterService = masterService;
    }
    
    public MasterService getService() {
        return masterService;
    }
    
    public void setService(MasterService masterService) {
        this.masterService = masterService;
    }
    
    @Override
    public void joinNetworkRequest(String clientUrl) throws Exception {
        if (masterService != null) {
            masterService.joinNetworkRequest(clientUrl);
        } else {
            throw new MasterDeactivatedException();
        }
    }

    @Override
    public boolean updateStateRequest(String component, String newData, long revision)
            throws Exception {
        if (masterService != null) {
            return masterService.updateStateRequest(component, newData, revision);
        } else {
            throw new MasterDeactivatedException();
        }
    }
    
}
