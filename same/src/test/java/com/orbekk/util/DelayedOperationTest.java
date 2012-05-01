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
