package lemon.launcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class LoggerGui implements Runnable {
	private JTextArea textArea;
	private InputStream inputStream;
	public LoggerGui(InputStream inputStream){
		textArea = new JTextArea();
		this.inputStream = inputStream;
	}
	public JTextArea getJTextArea(){
		return textArea;
	}
	public void setCaretPolicy(int policy){
		((DefaultCaret)textArea.getCaret()).setUpdatePolicy(policy);
	}
	@Override
	public void run() {
		try{
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			String line;
			while((line=reader.readLine())!=null){
				line = line+System.getProperty("line.separator");
				textArea.append(line);
			}
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
}
