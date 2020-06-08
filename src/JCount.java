// JCount.java

/*
 Basic GUI/Threading exercise.
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class JCount extends JPanel {
	private JButton startBtn;
	private JButton stopBtn;
	private JLabel currVal;
	private JTextField input;
	private WorkerThread counter;


	private class WorkerThread extends Thread{

		private int goal;

		public WorkerThread(int goal){
			this.goal = goal;
		}

		public void run(){
			for (int i = 1;  i <= goal; i++){
				if (i % 10000 == 0){
					try {
						Thread.sleep(100);
					} catch (InterruptedException ex) {
						return ;
					}
					final String val = String.valueOf(i);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							currVal.setText(val);
						}
					});
				}
			}
		}

	};


	public JCount() {
		super();

		// Set the JCount to use Box layout
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));


		counter = null;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		input = new JTextField("100000000", 8);
		currVal = new JLabel("0");
		startBtn = new JButton("Start");
		stopBtn = new JButton("Stop");
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (counter != null)
					counter.interrupt();
				counter = new WorkerThread(Integer.parseInt(input.getText()));
				counter.start();
			}
		});
		stopBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (counter != null) {
					counter.interrupt();
					counter = null;
				}
			}
		});

		add(input);
		add(currVal);
		add(startBtn);
		add(stopBtn);
		add(Box.createRigidArea(new Dimension(0, 40)));

	}

	public static void createAndShowGUI(){
		// Creates a frame with 4 JCounts in it.
		// (provided)
		JFrame frame = new JFrame("The Count");
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

		frame.add(new JCount());
		frame.add(new JCount());
		frame.add(new JCount());
		frame.add(new JCount());

		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	static public void main(String[] args)  {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}
}

