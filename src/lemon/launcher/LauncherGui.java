package lemon.launcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;
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
		tabbedPane = new JTabbedPane();
		JButton playButton = new JButton("Play");
		playButton.addActionListener(x->processLauncher.launchProcess(JOptionPane.showInputDialog("Args")));
		frame.add(playButton, BorderLayout.SOUTH);
		frame.add(tabbedPane, BorderLayout.CENTER);
		frame.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent event){
				for(Process process: processes){
					process.destroyForcibly();
					try{
						System.out.println("Process Exit Value: "+process.waitFor());
					}catch(InterruptedException ex){
						ex.printStackTrace();
					}
				}
			}
		});
		frame.setVisible(true);
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
		new Thread(()->onClose(process, panel, processes.size()-1)).start();
	}
	public void onClose(Process process, JPanel tab, int tabNumber){
		try{
			System.out.println("Process Exit Value: "+process.waitFor());
		}catch(InterruptedException ex){
			ex.printStackTrace();
		}
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		JLabel label = new JLabel("Process "+(tabNumber+1));
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
		panel.add(label);
		JButton button = new JButton("x");
		button.setUI(new BasicButtonUI());
		button.setContentAreaFilled(false);
		button.setFocusable(false);
		button.setBorder(BorderFactory.createEtchedBorder());
		button.setPreferredSize(new Dimension(17, 17));
		button.addActionListener(l->{
			tabbedPane.removeTabAt(tabbedPane.indexOfTabComponent(panel));
			processes.remove(process);
		});
		panel.add(button);
		panel.setOpaque(false);
		tabbedPane.setTabComponentAt(tabbedPane.indexOfComponent(tab), panel);
	}
	public JFrame getFrame(){
		return frame;
	}
}
