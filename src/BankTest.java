import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class BankTest {
    @Test
    public void bankTestMainZeroThreads(){

        String [] argv1={"small.txt"};
        String [] argv2={"small.txt","0"};
        String [] argv3=new String[0];

        Bank.main(argv1);
        Bank.main(argv2);
        Bank.main(argv3);

    }

    @Test
    public void bankTestOneThread(){
        int numWorkers = 1;
        Bank bank = new Bank(numWorkers);
        bank.processFile("100k.txt", numWorkers);
    }

    @Test
    public void bankTestFiveThreads(){
        int numWorkers = 5;
        Bank bank = new Bank(numWorkers);
        bank.processFile("100k.txt", numWorkers);
    }

    @Test
    public void bankTestCheckSum(){
        int beforeSum = 0;
        int afterSum = 0;
        Account[] accnts;
        int numWorkers = 100;
        Bank bank = new Bank(numWorkers);
        accnts = bank.getAccountList();
        for (int i = 0; i < Bank.ACCOUNTS; i++){
            beforeSum += accnts[i].getBalance();
        }
        bank.processFile("small.txt", numWorkers);
        accnts = bank.getAccountList();
        for (int i = 0; i < Bank.ACCOUNTS; i++){
            afterSum += accnts[i].getBalance();
        }
        assertEquals(beforeSum, afterSum);
    }

    @Test
    public void bankTestCheckFinal(){
        Account[] accnts;
        int numWorkers = 100;
        Bank bank = new Bank(numWorkers);
        bank.processFile("100k.txt", numWorkers);
        accnts = bank.getAccountList();
        for (int i = 0; i < Bank.ACCOUNTS; i++){
            assertEquals(1000, accnts[i].getBalance());
        }
    }

    @Test
    public void bankTestCheckWrongName(){
        Bank bank = new Bank(5);
        bank.processFile("1000k.txt", 5);
    }

    @Test
    public void bankTestCheckQueueEmpty(){
        Bank bank = new Bank(5);
        assertThrows(Exception.class, ()->bank.getQueue().remove());
        bank.processFile("100k.txt", 5);
        assertThrows(Exception.class, ()->bank.getQueue().remove());
    }
    @Test
    public void bankTestCheckLatchEmpty(){
        Bank bank = new Bank(5);
        bank.processFile("100k.txt", 5);
        assertEquals(0, bank.getLatch().getCount());
    }
}