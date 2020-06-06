// Bank.java

/*
 Creates a bunch of accounts and uses threads
 to post transactions to the accounts concurrently.
*/

import java.io.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Bank {
	public static final int ACCOUNTS = 20;	 // number of accounts
	public static final int ACCOUNT_INIT_BALANCE = 1000; //account initial balance
	private final Transaction nullTrans;
	private BlockingQueue<Transaction> queue;
	private Account []accountList;
	private CountDownLatch latch;

	private class Worker extends Thread{
		public void run(){
			int from;
			int to;
			int amount;
			while(true){
				Transaction curr = null;
				try {
					curr = queue.take();
				} catch (InterruptedException e) {
					exitErr(e);
				}
				if (curr == nullTrans)
					break;
				from  = curr.getFrom();
				to = curr.getTo();
				amount = curr.getAmount();
				accountList[from].subtract(amount);
				accountList[to].add(amount);
			}
			latch.countDown();
		}
	}

	public Bank(int numWorkers){
		int queueSize = ACCOUNTS;
		if (queueSize < numWorkers)
			queueSize =  numWorkers;
		queue = new ArrayBlockingQueue<Transaction>(queueSize);
		nullTrans = new Transaction(-1,0,0);
		accountList = new Account[ACCOUNTS];
		for (int i = 0; i < ACCOUNTS;i++){
			accountList[i] = new Account(this, i, ACCOUNT_INIT_BALANCE);
		}

	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public BlockingQueue<Transaction> getQueue() {
		return queue;
	}

	public Account[] getAccountList() {
		return accountList;
	}

	/*
         Reads transaction data (from/to/amt) from a file for processing.
         (provided code)
         */
	public void readFile(String file) throws Exception {
			BufferedReader reader = new BufferedReader(new FileReader(file));

			// Use stream tokenizer to get successive words from file
			StreamTokenizer tokenizer = new StreamTokenizer(reader);

			while (true) {
				int read = tokenizer.nextToken();
				if (read == StreamTokenizer.TT_EOF) break;  // detect EOF
				int from = (int)tokenizer.nval;

				tokenizer.nextToken();
				int to = (int)tokenizer.nval;

				tokenizer.nextToken();
				int amount = (int)tokenizer.nval;

				// Use the from/to/amount

				// YOUR CODE HERE
				queue.put(new Transaction(from, to, amount));
			}
	}

	private void createWorkers(int numWorkers){
		Worker currWorker;
		for (int i = 0; i < numWorkers; i++) {
			currWorker = new Worker();
			currWorker.start();
		}

	}

	/*
	 Processes one file of transaction data
	 -fork off workers
	 -read file into the buffer
	 -wait for the workers to finish
	*/
	public void processFile(String file, int numWorkers) {
		latch = new CountDownLatch(numWorkers);
		createWorkers(numWorkers);
		try {
		readFile(file);
			for (int i = 0; i < numWorkers; i++) {
					queue.put(nullTrans);
			}
			latch.await();
		} catch (Exception e) {
			exitErr(e);
		}

	}

	private void exitErr(Exception e){
		e.printStackTrace();
		//System.exit(1);
	}

	public void printAccounts(){
		for (int i = 0; i < ACCOUNTS; i++){
			System.out.println(accountList[i].toString());
		}
	}
	
	/*
	 Looks at commandline args and calls Bank processing.
	*/
	public static void main(String[] args) {
		// deal with command-lines args
		if (args.length == 0) {
			return;
		}
		
		String file = args[0];
		
		int numWorkers = 1;
		if (args.length >= 2) {
			numWorkers = Integer.parseInt(args[1]);
		}
		// YOUR CODE HERE
		Bank bank = new Bank(numWorkers);
		bank.processFile(file, numWorkers);
		bank.printAccounts();
	}
}

