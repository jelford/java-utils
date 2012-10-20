package com.jameselford.java.util.concurrent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UnthreadedExecutorTests {
	private ExecutorService executor;
	private int initialThreadCount;

	@Before
	public void setUp() {
		initialThreadCount = Thread.activeCount();
		this.executor = new UnthreadedExecutor();
	}
	
	@After
	public void tearDown() {
		this.executor = null;
	}
	
	@Test
	public void testAllAddedTasksAreFinishedImmediatelyAndThreadsArentPlayedWith() {
		final AtomicInteger counter = new AtomicInteger();
		final Thread currentThread = Thread.currentThread();
		
		for (int i=0; i<100; ++i) {
			final int count = i;
			assertThat(counter.get(), is(equalTo(i)));
			assertThat(Thread.activeCount(), is(equalTo(initialThreadCount)));
			executor.execute(new Runnable() {

				@Override
				public void run() {
					assertThat(Thread.activeCount(), is(equalTo(initialThreadCount)));
					assertThat(Thread.currentThread(), is(equalTo(currentThread)));
					assertThat(counter.getAndIncrement(), is(equalTo(count)));
				}
				
			});
			assertThat(counter.get(), is(equalTo(i+1)));
		}
	}
	
	@Test(expected = AssertionError.class)
	public void testExecutorsDotSingleThreadedExecutorDoesntPass() {
		this.executor = Executors.newSingleThreadExecutor();
		testAllAddedTasksAreFinishedImmediatelyAndThreadsArentPlayedWith();
	}
	
	@Test(expected = AssertionError.class)
	public void testExecutorsDotFixedDoesntPass() {
		this.executor = Executors.newFixedThreadPool(20);
		testAllAddedTasksAreFinishedImmediatelyAndThreadsArentPlayedWith();
	}
	
}
