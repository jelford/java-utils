package com.jameselford.java.util.concurrent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class UnthreadedExecutor implements ExecutorService {
	private boolean isShutdown = false;

	@Override
	public synchronized void execute(Runnable runnable) {
		runnable.run();
	}

	@Override
	public synchronized boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		return true;
	}

	@Override
	public synchronized <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
			throws InterruptedException {
		List<Future<T>> retList = new ArrayList<>();
		try {
			for (final Callable<T> callable : tasks) {
				final T result = callable.call();
				retList.add(futureFrom(result));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return retList;
	}

	@Override
	public synchronized <T> List<Future<T>> invokeAll(
			Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		if (isShutdown())
			throw new RejectedExecutionException();
		else if (tasks.isEmpty())
			throw new IllegalArgumentException();
		
		boolean atLeastOneSuccess = false;
		T result = null;
		Exception oneOfTheCauses = null;
		for (Callable<T> t: tasks) {
			if (t == null)
				throw new NullPointerException();
			
			try {
				result = t.call();
				atLeastOneSuccess = true;
			} catch (Exception e) {
				// That's okay; just process the next one
				oneOfTheCauses = e;
			}
		}
		if (!atLeastOneSuccess)
			throw new ExecutionException(oneOfTheCauses);
		
		return result;
	}

	@Override
	public synchronized <T> T invokeAny(Collection<? extends Callable<T>> tasks,
			long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized boolean isShutdown() {
		return isShutdown;
	}

	@Override
	public synchronized boolean isTerminated() {
		return isShutdown;
	}

	@Override
	public synchronized void shutdown() {
		isShutdown = true;
	}

	@Override
	public List<Runnable> shutdownNow() {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized <T> Future<T> submit(Callable<T> task) {
		try {
			return futureFrom(task.call());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public synchronized Future<?> submit(Runnable task) {
		task.run();
		return futureFrom(null);
	}

	@Override
	public synchronized <T> Future<T> submit(Runnable task, T result) {
		task.run();
		return futureFrom(result);
	}
	
	private <T> Future<T> futureFrom(final T result) {
		return new Future<T>() {

			@Override
			public boolean cancel(boolean arg0) {
				return false;
			}

			@Override
			public T get() throws InterruptedException,
					ExecutionException {
				return result;
			}

			@Override
			public T get(long arg0, TimeUnit arg1)
					throws InterruptedException, ExecutionException,
					TimeoutException {
				return result;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return true;
			}
		};
	}

}
