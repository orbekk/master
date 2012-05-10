/**
 * Copyright 2012 Kjetil Ørbekk <kjetil.orbekk@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.orbekk.same.directory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.orbekk.same.Services;
import com.orbekk.same.Services.Empty;
import com.orbekk.same.Services.MasterState;
import com.orbekk.same.Services.NetworkDirectory;

public class DirectoryServiceImpl extends Services.Directory {
    private Logger logger = LoggerFactory.getLogger(getClass());
    public final static long EXPIRE_TIME = 15 * 60l * 1000;  // 15 minutes
    Map<String, NetworkEntry> networkMap = new HashMap<String, NetworkEntry>();
    
    synchronized void cleanNetworkList() {
        long expiredTime = System.currentTimeMillis() - EXPIRE_TIME;
        for (NetworkEntry entry : networkMap.values()) {
            if (entry.hasExpired(expiredTime)) {
                networkMap.remove(entry.networkName);
            }
        }
        logger.info("Cleaned network list. Networks: {}", networkMap);
    }

    public void registerNetwork(String networkName, String masterUrl) {
        NetworkEntry entry = new NetworkEntry(networkName, masterUrl);
        entry.register(System.currentTimeMillis());
        networkMap.put(networkName, entry);
        cleanNetworkList();
    }

    @Override
    public void registerNetwork(RpcController controller, MasterState request,
            RpcCallback<Empty> done) {
        registerNetwork(request.getNetworkName(), request.getMasterLocation());
        done.run(Empty.getDefaultInstance());
    }

    @Override
    public void getNetworks(RpcController controller, Empty request,
            RpcCallback<NetworkDirectory> done) {
        cleanNetworkList();
        NetworkDirectory.Builder directory = NetworkDirectory.newBuilder();
        for (NetworkEntry e : networkMap.values()) {
            directory.addNetwork(MasterState.newBuilder()
                    .setMasterLocation(e.masterUrl)
                    .setNetworkName(e.networkName)
                    .build());
        }
        done.run(directory.build());
    }
}
