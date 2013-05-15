package main;

import java.util.Collections;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import twitter4j.*;

public class TwitterWatcher extends DataBrake {
	
	private SortedSet<String> names;
	private AtomicInteger letterCount;
	private final ExecutorService executorPool = Executors.newFixedThreadPool(8);
	
	public TwitterWatcher() {
		names = Collections.synchronizedSortedSet(new TreeSet<String>());
		letterCount = new AtomicInteger(0);
	}

	@Override
	protected void setOrderedScreenNames(Status status) {
		String screenName = status.getUser().getScreenName();
		names.add(screenName);
	}

	@Override
	protected void setTotalLetterCount(Status status) {
		String text = status.getText();
		letterCount.addAndGet(text.length());
	}
	
	protected String getNames() {
		return names.toString();
	}
	
	public void getStream()  throws TwitterException  {
		 TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
	     StatusListener listener = new StatusListener() {
	            @Override
	            public void onStatus(Status status) {
	            	final Status fixedStatus = status;
                	executorPool.submit(new Runnable() {
        				public void run() {
        					try {
								processTweet(fixedStatus);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
        				}
        			});
	            }

	            @Override
	            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
	            @Override
	            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
	            @Override
	            public void onException(Exception ex) {
	                ex.printStackTrace();
	            }
				@Override
				public void onScrubGeo(long arg0, long arg1) {}
	        };
	        twitterStream.addListener(listener);
	        twitterStream.sample();
	}
	
	private void waitForExit() {
		executorPool.submit(new Runnable() {
			public void run() {
				Scanner sc = new Scanner(System.in);
				sc.nextLine(); //wait for enter input
				executorPool.shutdown();
				sc.close();
				System.out.println(getNames());
				System.exit(0);
			}
		});
	}
	
	private void getCountPeriodically() {
		executorPool.submit(new Runnable() {
			public void run() {
				try {
					while(!executorPool.isShutdown()) {
						Thread.sleep(1000);
						System.out.println("Current Letter Count: " + letterCount.get());
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
		});
	}
	
	public static void main(String[] args) {
		final TwitterWatcher tw = new TwitterWatcher();
		tw.waitForExit();
		try {
			tw.getStream();
			tw.getCountPeriodically();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}
}
	
