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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.same.config.Configuration;

public class App {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Services.MasterState getMasterInfo(Configuration configuration) {
        return Services.MasterState.newBuilder()
                .setMasterUrl(configuration.get("masterUrl"))
                .setMasterLocation(configuration.get("masterLocation"))
                .build();
    }
    
    public void run(String[] args) {
        Configuration configuration = Configuration.loadOrDie();
        SameController controller = SameController.create(configuration);
        try {
            controller.start();
            if ("true".equals(configuration.get("isMaster"))) {
                controller.createNetwork(configuration.get("networkName"));
            } else {
                controller.joinNetwork(getMasterInfo(configuration));
            }
            controller.join();
        } catch (Exception e) {
            logger.error("Error in App.", e);
        }
    }

    public static void main(String[] args) {
        new App().run(args);
    }
}
