package com.orbekk.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class DelayedOperationTest {
	@Test public void initialOperationStatus() {
		DelayedOperation operation = new DelayedOperation();
		assertFalse(operation.isDone());
	}
	
	@Test public void completedOperationReturns() {
		DelayedOperation operation = new DelayedOperation();
		operation.complete(DelayedOperation.Status.createOk());
		assertTrue(operation.isDone());
		assertEquals(DelayedOperation.Status.createOk(), operation.getStatus());
	}

	@Test public void concurrentTest() throws Exception {
		final DelayedOperation operation = new DelayedOperation();
		
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				operation.waitFor();
			}
		});
		t.start();
		operation.complete(DelayedOperation.Status.createError("Fail."));
		t.join();
		assertTrue(operation.isDone());
	}
}
