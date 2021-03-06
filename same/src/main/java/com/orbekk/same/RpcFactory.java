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

import com.orbekk.protobuf.Rpc;

public class RpcFactory {
    private final long timeoutMillis;
    
    public RpcFactory(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }
    
    public Rpc create() {
        Rpc rpc = new Rpc();
        rpc.setTimeout(timeoutMillis);
        return rpc;
    }
}
