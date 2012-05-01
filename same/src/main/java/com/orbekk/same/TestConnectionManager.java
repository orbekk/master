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
package com.orbekk.same;

import java.util.Map;
import java.util.HashMap;

import com.orbekk.same.Services.Directory;
import com.orbekk.same.Services.Master;
import com.orbekk.same.Services.Paxos;

/**
 * This class is used in test.
 */
public class TestConnectionManager implements ConnectionManager {
    public Map<String, Services.Directory> directoryMap =
            new HashMap<String, Services.Directory>();
    public Map<String, Services.Master> masterMap0 =
            new HashMap<String, Services.Master>();
    public Map<String, Services.Client> clientMap0 =
            new HashMap<String, Services.Client>();
    public Map<String, Services.Paxos> paxosMap0 =
            new HashMap<String, Services.Paxos>();

    public TestConnectionManager() {
    }

    @Override
    public Directory getDirectory(String location) {
        return directoryMap.get(location);
    }

    @Override
    public Master getMaster0(String location) {
        return masterMap0.get(location);
    }

    @Override
    public Services.Client getClient0(String location) {
        return clientMap0.get(location);
    }
    
    @Override
    public Services.Paxos getPaxos0(String location) {
        return paxosMap0.get(location);
    }
}
