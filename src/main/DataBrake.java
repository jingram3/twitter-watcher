package main;

import twitter4j.Status;

public abstract class DataBrake {
	private final int SLEEP = 200;

	public final void processTweet(Status status) throws InterruptedException {
		/*
		 * Assuming 45 tweets/second, on my quad core, I have to process 45/4 ~=
		 * 12 tweets per core per second. I am going to set the SLEEP field
		 * large enough so that I cannot complete all the work with less than 4
		 * processors. Lets assume 45 tweets per second and 3 processors. Then
		 * each processor would need to do 15 tweets per second, or 0.067
		 * seconds per tweet. I'll set SLEEP = 0.07 seconds; then it will be
		 * impossible to keep up without 4 processors. I may need to adjust this
		 * later empirically based on function and feel free to do so on your
		 * own setup
		 */
		Thread.sleep(SLEEP);
		setTotalLetterCount(status);
		setOrderedScreenNames(status);
	}

	protected abstract void setOrderedScreenNames(Status status);

	protected abstract void setTotalLetterCount(Status status);
}