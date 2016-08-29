package lemon.launcher;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class LauncherGui {
	private JFrame frame;
	private List<Process> processes;
	public LauncherGui(String title, int closeOperation, ProcessLauncher processLauncher){
		processes = new ArrayList<Process>();
		frame = new JFrame(title);
		frame.setDefaultCloseOperation(closeOperation);
		frame.setSize(800, 600);
		frame.setLayout(new BorderLayout());
		frame.setResizable(true);
		frame.setVisible(true);
		JTabbedPane tabbedPane = new JTabbedPane();
		JPanel panel = new JPanel(new BorderLayout());
		JButton playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				processLauncher.launchProcess();
			}
		});
		panel.add(playButton, BorderLayout.SOUTH);
		tabbedPane.addTab("Main", panel);
		frame.add(tabbedPane, BorderLayout.CENTER);
	}
	public void startProcess(ProcessBuilder builder) throws IOException {
		processes.add(builder.start());
	}
	public JFrame getFrame(){
		return frame;
	}
}
