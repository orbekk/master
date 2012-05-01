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

/**
 * An interface that returns a connection for a participant.
 *
 * When testing, this interface can be mocked to use local participants only.
 */
public interface ConnectionManager {
    Services.Master getMaster0(String location);
    Services.Client getClient0(String location);
    Services.Directory getDirectory(String location);
    Services.Paxos getPaxos0(String location);
}
