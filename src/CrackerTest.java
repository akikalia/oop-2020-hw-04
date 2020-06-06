import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class CrackerTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    @Test
    public void crackerTestModeOne(){
        assertEquals(Cracker.getHashed("molly"), "4181eecbd7a755d19fdf73887c54837cbecf63fd");
        assertEquals(Cracker.getHashed("flomo"), "886ffd41c568469795a19f52486bdde64f5f5bcc");
    }

    private void captureOut(ByteArrayOutputStream outContent) {
        System.setOut( new PrintStream(outContent) );
    }

    private String getOut() {
        System.setOut( new PrintStream( new FileOutputStream( FileDescriptor.out ) ) );
        return outContent.toString().replaceAll( "\n", "" );
    }

    //hashes, decripts hash(and captures stdout)
    private String hashRetrieve(String src, int maxLen, int numThreads){
        captureOut(outContent);
        Cracker crkr = new Cracker(Cracker.getHashed(src), maxLen);
        crkr.beginHashing(numThreads);
        return getOut();
    }

    private void singleEdgeTester(String src, int maxLen, int numThreads){
        hashRetrieve(src, maxLen, numThreads);
        assertEquals(src, getOut());
        outContent.reset();
    }

    private void testerEmpty(String src, int maxLen, int numThreads){
        hashRetrieve(src, maxLen, numThreads);
        assertEquals("", getOut());
        outContent.reset();
    }

    @Test
    public void crackerTestEdgeSymbols(){
        singleEdgeTester("a!", 3, 7);
        singleEdgeTester("ae", 3, 7);
        singleEdgeTester("af", 3, 7);
        singleEdgeTester("ag", 3, 7);
        singleEdgeTester("!e", 3, 7);
        singleEdgeTester("!f", 3, 7);
        singleEdgeTester("!g", 3, 7);
        singleEdgeTester("!a", 3, 7);

        singleEdgeTester("a!", 2, 8);
        singleEdgeTester("ae", 2, 8);
        singleEdgeTester("af", 2, 8);
        singleEdgeTester("ag", 2, 8);
        singleEdgeTester("!e", 2, 8);
        singleEdgeTester("!f", 2, 8);
        singleEdgeTester("!g", 2, 8);
        singleEdgeTester("!a", 2, 8);

    }

    @Test
    public void crackerAlternateHash(){
        captureOut(outContent);
        Cracker.setALGORITHM("SHA-256");
        Cracker crkr = new Cracker(
                "87f633634cc4b02f628685651f0a29b7bfa22a0bd841f725c6772dd00a58d489", 2);
        crkr.beginHashing(7);
        assertEquals("oi", getOut());
        outContent.reset();

        //throws exception
        captureOut(outContent);
        Cracker.setALGORITHM("SHA-257");
        Cracker crkr2 = new Cracker(
                "87f633634cc4b02f628685651f0a29b7bfa22a0bd841f725c6772dd00a58d489", 1);
        crkr.beginHashing(7);
        assertEquals("", getOut());
        outContent.reset();
        Cracker.setALGORITHM("SHA-1");

    }


    @Test
    public void crackerTestThreadCount(){
        Cracker crkr = new Cracker(Cracker.getHashed("hello"), 2);
        crkr.beginHashing(7);
        assertEquals(crkr.getLatch().getCount(), 0);
    }

    @Test
    public void crackerTestDepth(){
        testerEmpty("hello", 1, 7);
        testerEmpty("hello", 2, 100);
    }

    @Test
    public void runMain(){
        String []argv = {"66b27417d37e024c46526c2f6d358a754fc552f3","3","7"};
        String []argv1 = {"molly"};
        String []argv2 = new String[0] ;

        captureOut(outContent);
        Cracker.main(argv);
        assertEquals(getOut(),"xyz");
        outContent.reset();

        captureOut(outContent);
        Cracker.main(argv1);
        assertEquals(getOut(),"4181eecbd7a755d19fdf73887c54837cbecf63fd");
        outContent.reset();


        captureOut(outContent);
        Cracker.main(argv2);
        assertEquals(getOut(),"Args: target length [workers]");
        outContent.reset();

    }

}