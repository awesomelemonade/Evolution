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
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.text.DefaultCaret;

public class LauncherGui {
	private JFrame frame;
	private List<Process> processes;
	private List<LoggerGui> loggers;
	private JTabbedPane tabbedPane;
	public LauncherGui(String title, int closeOperation, ProcessLauncher processLauncher){
		processes = new ArrayList<Process>();
		loggers = new ArrayList<LoggerGui>();
		frame = new JFrame(title);
		frame.setDefaultCloseOperation(closeOperation);
		frame.setSize(800, 600);
		frame.setLayout(new BorderLayout());
		frame.setResizable(true);
		frame.setVisible(true);
		tabbedPane = new JTabbedPane();
		JButton playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				processLauncher.launchProcess();
			}
		});
		frame.add(playButton, BorderLayout.SOUTH);
		frame.add(tabbedPane, BorderLayout.CENTER);
	}
	public void startProcess(ProcessBuilder builder) throws IOException {
		Process process = builder.start();
		LoggerGui logger = new LoggerGui(process.getErrorStream());
		logger.setCaretPolicy(DefaultCaret.ALWAYS_UPDATE);
		new Thread(logger).start();
		JPanel panel = new JPanel(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(logger.getJTextArea());
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(scrollPane, BorderLayout.CENTER);
		tabbedPane.addTab("Process "+(processes.size()+1), panel);
		tabbedPane.setSelectedComponent(panel);
		processes.add(process);
		loggers.add(logger);
	}
	public JFrame getFrame(){
		return frame;
	}
}
