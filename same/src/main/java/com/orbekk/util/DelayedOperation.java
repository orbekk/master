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
package com.orbekk.util;

import java.util.concurrent.CountDownLatch;

public class DelayedOperation {
    public static class Status {
        public final static int OK = 1;
        public final static int CONFLICT = 2;
        public final static int ERROR = 3;

        private int status;
        private String message;

        public static Status createOk() {
            return new Status(OK, "");
        }

        public static Status createConflict(String message) {
            return new Status(CONFLICT, message);
        }

        public static Status createError(String message) {
            return new Status(ERROR, message);
        }

        public Status(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public boolean isOk() {
            return status == OK;
        }
        
        public int getStatusCode() {
            return status;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override public String toString() {
            switch(status) {
            case OK:
                return "OK";
            case CONFLICT:
                return "Conflicting update: " + message;
            case ERROR:
                return "Error: " + message;
            }
            throw new AssertionError("Unhandled case.");
        }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Status)) {
                return false;
            }
            Status o = (Status)other;
            if (o.status != this.status) {
                return false;
            }
            if (message == null) {
                return o.message == null;
            }
            return message.equals(o.message);
        }
    }

    private volatile Status status;
    private volatile int identifier;
    private final CountDownLatch done = new CountDownLatch(1);
    
    public DelayedOperation() {
    }

    public Status getStatus() {
        waitFor();
        return status;
    }

    public void waitFor() {
        try {
            done.await();
        } catch (InterruptedException e) {
            complete(new Status(Status.ERROR, "Thread interrupted."));
            Thread.currentThread().interrupt();
        }
    }

    public synchronized boolean isDone() {
        return done.getCount() <= 0;
    }

    public synchronized void complete(Status status) {
        this.status = status;
        done.countDown();
    }

    public synchronized int getIdentifier() {
        return identifier;
    }

    public synchronized void setIdentifier(int identifier) {
        this.identifier = identifier;
    }
}
