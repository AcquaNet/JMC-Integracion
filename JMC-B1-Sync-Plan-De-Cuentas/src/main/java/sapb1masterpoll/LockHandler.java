package sapb1masterpoll;

import java.util.concurrent.locks.ReentrantLock;

public class LockHandler {
	public static ReentrantLock Fetchlock = new ReentrantLock();
	public static void FetchLock() {
		LockHandler.Fetchlock.lock();
	}

	public static void FetchUnLock() {
		LockHandler.Fetchlock.unlock();
	}
	
	public static ReentrantLock Distlock = new ReentrantLock();
	public static void DistLock() {
		LockHandler.Distlock.lock();
	}

	public static void DistUnLock() {
		LockHandler.Distlock.unlock();
	}
}
