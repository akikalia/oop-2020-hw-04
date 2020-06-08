import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class WebFrame extends JFrame {
    private DefaultTableModel model;
    private JTable table;

    private JButton singleFetch;
    private JButton concurrentFetch;
    private JTextField threadInput;
    private JLabel running;
    private JLabel completed;
    private JLabel elapsed;

    private JProgressBar progress;
    private JButton stop;
    public Launcher launcher;

    private List<String> urlList;

    private int numWorkers;
    private Semaphore workerLimit;

    public AtomicInteger runningThreads;
    public AtomicInteger completedThreads;

    public class Launcher extends Thread{
        private int maxThreads;
        private List<WebWorker> workerList;


        public Launcher(int maxThreads){
            this.maxThreads = maxThreads;
            workerList = new ArrayList<>();
            workerLimit = new Semaphore(maxThreads);
            runningThreads = new AtomicInteger(0);
            completedThreads = new AtomicInteger(0);


        }

        public void run(){
            updateRunning(runningThreads.incrementAndGet());
            for (int i = 0; i< numWorkers; i++){
                try {
                    workerLimit.acquire();
                    WebWorker worker = new WebWorker(urlList.get(i), i, WebFrame.this);
                    workerList.add(worker);
                    worker.start();
                } catch (InterruptedException e) {
                    break ;
                }

            }
            updateRunning(runningThreads.decrementAndGet());
        }

        public void releaseWorker() {
            workerLimit.release();
        }
        public void stopWorkers(){
            for (int i = 0; i< numWorkers; i++)
                workerList.get(i).interrupt();
        }
    }

    public void updateRunning(int num){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                running.setText("Running:" + num);
            }
        });
    }

    public void acquireWorker(){
        updateRunning(runningThreads.incrementAndGet());
    }
    public void releaseWorker(String status, int workerId){
        runningThreads.decrementAndGet();
        completedThreads.incrementAndGet();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                running.setText("Running:" + runningThreads.get());
                completed.setText("Completed:" + completedThreads.get());
                progress.setValue(completedThreads.get());
                model.setValueAt(status, workerId, 1);
            }
        });
        launcher.releaseWorker();
    }

    public WebFrame(String label){
        super(label);

        urlList = new ArrayList<>();
        launcher = null;
        JPanel panel = (JPanel) getContentPane();
        setLayout(new BoxLayout(panel , BoxLayout.Y_AXIS));


        model = new DefaultTableModel(new String[] {"url", "status"}, 0);
        populateFiles();
        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 300));
        panel.add(scrollPane);


        singleFetch = new JButton("Single Thread Fetch");
        concurrentFetch = new JButton("Concurrent Fetch");

        singleFetch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                launcher = new Launcher(1);
                setLaunchedState();
            }
        });

        concurrentFetch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                launcher = new Launcher(Integer.parseInt(threadInput.getText()));
                setLaunchedState();
            }
        });

        threadInput =  new JTextField("4", 5);
        threadInput.setMaximumSize(threadInput.getPreferredSize());
        running = new JLabel("Running: ");
        completed = new JLabel("Completed: ");
        elapsed = new JLabel("Elapsed: ");
        progress = new JProgressBar(0, urlList.size());
        progress.setValue(0);
        stop = new JButton("Stop");
        stop.setEnabled(false);
        stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                launcher.stopWorkers();
                launcher.interrupt();
                setStoppedState();
            }
        });

        add(singleFetch);
        add(concurrentFetch);
        add(threadInput);
        add(running);
        add(completed);
        add(elapsed);
        add(progress);
        add(stop);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void populateFiles(){
        try {
            BufferedReader in = new BufferedReader(new FileReader("links2.txt"));
            String curr;
            while ((curr = in.readLine()) != null) {
                model.addRow(new String[]{curr, ""});
                urlList.add(curr);
                numWorkers++;
            }
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setStoppedState(){
        singleFetch.setEnabled(true);
        concurrentFetch.setEnabled(true);
        stop.setEnabled(false);
        progress.setValue(0);
    }

    private void setLaunchedState(){
        int numUrl = urlList.size();
        singleFetch.setEnabled(false);
        concurrentFetch.setEnabled(false);
        running.setText("Running: ");
        completed.setText("Completed: ");
        elapsed.setText("Elapsed: ");
        stop.setEnabled(true);
        launcher.start();

        for (int i = 0; i < numUrl; i++)
            model.setValueAt("", i, 1);
    }


    static public void main(String[] args)  {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new WebFrame("WebLoader");
            }
        });
    }
}
