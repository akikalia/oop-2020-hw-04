// Account.java

/*
 Simple, thread-safe Account class encapsulates
 a balance and a transaction count.
*/
public class Account {
	private int id;
	private int balance;
	private int transactions;
	
	// It may work out to be handy for the account to
	// have a pointer to its Bank.
	// (a suggestion, not a requirement)
	private Bank bank;  
	
	public Account(Bank bank, int id, int balance) {
		this.bank = bank;
		this.id = id;
		this.balance = balance;
		transactions = 0;
	}

	public int getBalance() {
		return balance;
	}

	public synchronized void subtract(int amount){
		transactions++;
		balance -= amount;
	}

	public synchronized void add(int amount){
		transactions++;
		balance += amount;
	}
	public String toString(){
		return "acct:"+ id +" bal:" + balance + " trans:" + transactions;
	}
}
