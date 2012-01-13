package com.orbekk.same;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterServiceImpl implements MasterService, UrlReceiver {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private State state;
  
    public MasterServiceImpl(State initialState) {
        state = initialState;
    }
    
    @Override
    public void joinNetworkRequest(String networkName, String clientUrl) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean updateStateRequest(String component, String newData, long revision) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setUrl(String url) {
        String myUrl = url + "MasterService.json";
        logger.info("Master URL is " + myUrl);
        state.update(".masterUrl", myUrl, 0);
    }
}
