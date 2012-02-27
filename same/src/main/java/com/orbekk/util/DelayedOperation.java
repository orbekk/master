package com.orbekk.util;

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

    private Status status;
    private boolean isDone;
    private int identifier;

    public DelayedOperation() {
    }

    public Status getStatus() {
        waitFor();
        return status;
    }

    public synchronized void waitFor() {
        while (!isDone) {
            try {
                wait();
            } catch (InterruptedException e) {
                complete(new Status(Status.ERROR, "Thread interrupted."));
            }
        }
    }

    public synchronized boolean isDone() {
        return isDone;
    }

    public synchronized void complete(Status status) {
        if (!isDone) {
            isDone = true;
            this.status = status;
            notifyAll();
        }
    }

    public synchronized int getIdentifier() {
        return identifier;
    }

    public synchronized void setIdentifier(int identifier) {
        this.identifier = identifier;
    }
}
