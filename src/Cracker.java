// Cracker.java
/*
 Generates SHA hashes of short strings in parallel.
*/

import java.security.*;
import java.util.concurrent.CountDownLatch;

public class Cracker {
	// Array of chars used to produce strings
	public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789.,-!".toCharArray();
	private static String ALGORITHM = "SHA-1";
	private int wordMaxLen;
	private String target;
	private CountDownLatch latch;
	private Boolean cracked;
	private class Hasher implements Runnable{
		private int start;
		private int end;

		public Hasher(int start, int end){
			this.start = start;
			if (end > CHARS.length)
				end = CHARS.length;
			this.end = end;
		}
		private boolean generate(String word, int depth){
			if (getHashed(word).equals(target)){
				System.out.println(word);
				synchronized (Cracker.this){
					cracked = true;
				}
				return true;
			}
			if (depth == 0)
				return false;
			for (int i = 0; i < CHARS.length; i++){
				if (cracked)
					return true;
				if (generate(word + CHARS[i],depth-1)){
					return true;
				}
			}
			return false;
		}

		public void run(){
			for (int i = start;  i < end; i++){
				if (generate(String.valueOf(CHARS[i]),wordMaxLen-1))
					break;
			}
			latch.countDown();
		}
	}

	public Cracker(String target,int maxLen){
		this.target = target;
		wordMaxLen = maxLen;
		cracked = false;
	}

	/*
	 Given a byte[] array, produces a hex String,
	 such as "234a6f". with 2 chars for each byte in the array.
	 (provided code)
	*/
	public static String hexToString(byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (int i=0; i<bytes.length; i++) {
			int val = bytes[i];
			val = val & 0xff;  // remove higher bits, sign
			if (val<16) buff.append('0'); // leading 0
			buff.append(Integer.toString(val, 16));
		}
		return buff.toString();
	}
	
	/*
	 Given a string of hex byte values such as "24a26f", creates
	 a byte[] array of those values, one byte value -128..127
	 for each 2 chars.
	 (provided code)
	*/
//	public static byte[] hexToArray(String hex) {
//		byte[] result = new byte[hex.length()/2];
//		for (int i=0; i<hex.length(); i+=2) {
//			result[i/2] = (byte) Integer.parseInt(hex.substring(i, i+2), 16);
//		}
//		return result;
//	}

	public static String getHashed(String word){
		MessageDigest md;
		String res = "";
		try {
			md = MessageDigest.getInstance(ALGORITHM);
			res = hexToString(md.digest(word.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return res;
	}

	public static void setALGORITHM(String ALGORITHM) {
		Cracker.ALGORITHM = ALGORITHM;
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public void beginHashing(int numThreads){
		latch = new CountDownLatch(numThreads);
		Runnable curr;
		int increment = CHARS.length/numThreads;
		if (CHARS.length%numThreads != 0){
			increment++;
		}
		if (increment <= 1)
			increment = 1;
		int beginIndex = 0;
		for (int i = 0; i < numThreads; i++){
			curr = new Hasher(beginIndex, beginIndex + increment);
			new Thread(curr).start();
			beginIndex += increment;
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Args: target length [workers]");
			return;
		}
		if (args.length == 1){
			System.out.println(getHashed(args[0]));
			return ;
		}

		// args: targ len [num]
		String targ = args[0];
		int len = Integer.parseInt(args[1]);
		int num = 1;
		if (args.length>2) {
			num = Integer.parseInt(args[2]);
			Cracker crkr = new Cracker(targ, len);
			if (num > 0)
				crkr.beginHashing(num);
		}
		// a! 34800e15707fae815d7c90d49de44aca97e2d759
		// xyz 66b27417d37e024c46526c2f6d358a754fc552f3
		
		// YOUR CODE HERE
	}
}
