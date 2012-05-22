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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbekk.protobuf.RpcChannel;

public class ConnectionManagerImpl implements ConnectionManager {
    private int connectionTimeout;
    private int readTimeout;
    private ConcurrentMap<String, Future<RpcChannel>> channels =
            new ConcurrentHashMap<String, Future<RpcChannel>>();
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @param connectionTimout Timeout for establishing connection in milliseconds.
     * @param readTimeout Timeout for waiting for answer in milliseconds.
     */
    public ConnectionManagerImpl(int connectionTimout, int readTimeout) {
        this.connectionTimeout = connectionTimout;
        this.readTimeout = readTimeout;
    }

    private boolean isValidLocation(String location) {
        String[] args = location.split(":");
        if (args.length != 2) {
            logger.error("Invalid location: " + location);
            return false;
        }
        try {
            Integer.valueOf(args[1]);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid location: " + location);
            return false;
        }
        return true;
    }
    
    @Override
    public RpcChannel getChannel(String location) {
        Future<RpcChannel> channel = channels.get(location);
        if (channel == null) {
            if (!isValidLocation(location)) {
                return null;
            }
            String[] args = location.split(":");
            final String hostname = args[0];
            final int port = Integer.valueOf(args[1]);
            
            Callable<RpcChannel> channelFactory =
                    new Callable<RpcChannel>() {
                @Override public RpcChannel call() {
                    try {
                        return RpcChannel.create(hostname, port);
                    } catch (UnknownHostException e) {
                        logger.error("Could not connect: ", e);
                        return null;
                    } catch (IOException e) {
                        logger.error("Could not connect: ", e);
                        return null;
                    }
                }
            };
            FutureTask<RpcChannel> task =
                    new FutureTask<RpcChannel>(channelFactory);
            channel = channels.putIfAbsent(location, task);
            if (channel == null) {
                task.run();
                channel = task;
            }
        }
        try {
            return channel.get();
        } catch (ExecutionException e) {
            logger.error("Could not connect: ", e);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    @Override
    public Services.Master getMaster0(String location) {
        RpcChannel channel = getChannel(location);
        if (channel != null) {
            return Services.Master.newStub(channel);
        } else {
            return null;
        }
    }
    
    @Override
    public Services.Client getClient0(String location) {
        RpcChannel channel = getChannel(location);
        if (channel != null) {
            return Services.Client.newStub(channel);
        } else {
            return null;
        }
    }
    
    @Override
    public Services.Directory getDirectory(String location) {
        RpcChannel channel = getChannel(location);
        if (channel != null) {
            return Services.Directory.newStub(channel);
        } else {
            return null;
        }
    }
    
    @Override
    public Services.Paxos getPaxos0(String location) {
        RpcChannel channel = getChannel(location);
        if (channel != null) {
            return Services.Paxos.newStub(channel);
        } else {
            return null;
        }
    }
}
