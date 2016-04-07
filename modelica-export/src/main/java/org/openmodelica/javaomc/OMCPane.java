package org.openmodelica.javaomc;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.GridLayout;
import java.awt.event.*;

import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import java.awt.Label;
import java.util.ArrayList;

import javax.swing.BoxLayout;

public class OMCPane extends JPanel implements ActionListener, KeyListener
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField jTextField = null;
	private JButton jButton = null;
	private JButton jDemoButton = null;
	private JTextArea jTextArea = null;
	private OMCProxy omc;
	private JPanel jPanel1 = null;
	private Label label = null;
	private JSplitPane jSplitPane = null;
	private JScrollPane jScrollPane = null;
	
	private ArrayList<String> history = new ArrayList<String>();
	/**
	 * This is the default constructor
	 */
	public OMCPane() 
	{
		super(new GridLayout(1,0));
		initialize();
		this.omc = new OMCProxy();;
		history.clear();
		history.add(0, "getVersion()");
		history.add(0, "loadModel(Modelica)");
		history.add(0, "getClassNames()");
		history.add(0, "getClassNames(...class name...)");
		history.add(0, "loadFile(\"...file...\")");
		history.add(0, "list()");
		history.add(0, "list(...class name...)");
		history.add(0, "simulate(...class name...)");
		history.add(0, "plot(...variable name...)");
		history.add(0, "plot({...var1, var2,...})");
		history.add(0, "instantiateModel(...class name...");
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() 
	{
		this.setSize(398, 260);
		this.add(getJSplitPane(), null);
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() 
	{
		if (jTextField == null) 
		{
			jTextField = new JTextField();
			jTextField.setName("Expression");
			jTextField.setColumns(100);
		}
		jTextField.addKeyListener(this);
		return jTextField;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() 
	{
		if (jButton == null) 
		{
			jButton = new JButton();
			jButton.setText("Send");
			jButton.setActionCommand("send");
		}
		jButton.addActionListener(this);		
		return jButton;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJDemoButton() 
	{
		if (jDemoButton == null) 
		{
			jDemoButton = new JButton();
			jDemoButton.setText("Demo");
			jDemoButton.setActionCommand("demo");
		}
		jDemoButton.addActionListener(this);		
		return jDemoButton;
	}
	
	/**
	 * This method initializes jTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getJTextArea() 
	{
		if (jTextArea == null) 
		{
			jTextArea = new JTextArea();
			jTextArea.setColumns(100);
			jTextArea.setRows(20);
		}
		return jTextArea;
	}
	
	public void executeCommand(String command)
	{
		System.out.println("Expression:" + command);		
		if (command != null && 
			command.length() > 0)
		{
			history.add(command);
			String result = "";
			try
			{
				jTextArea.append("\nSending expression:" + command);
				result = omc.sendExpression(command);
				jTextArea.append("\nGot reply:" + result);					
			}
			catch(Exception ex)
			{
				jTextArea.append(
				  "\nError while sending expression: " + command + "\n"+
				  ex.getMessage());
			}
			jTextField.setText("");
		}
		else
		{
			jTextArea.append("\nNo expression sent because is empty");
		}		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		System.out.println("ActionCommand:" + e.getActionCommand());
		if (e.getActionCommand().equals("send"))
		{
			executeCommand(jTextField.getText());
		}
		if (e.getActionCommand().equals("demo"))
		{
			Thread t = new Thread()
			{				
				public void run()
				{
					jTextArea.append("\n// ******************************  DEMO STARTS *************************************");
					jTextArea.append("\n// ******************************  Starting BouncingBall DEMO **********************");
					jTextArea.append("\n// send the BouncingBall model");
					executeCommand("\n" +
							"model BouncingBall\n"+ 
							"  parameter Real e=0.7 \"coefficient of restitution\";\n" +
							"  parameter Real g=9.81 \"gravity acceleration\";\n" + 
							"  Real h(start=1) \"height of ball\";\n" +
							"  Real v \"velocity of ball\";\n" +
							"  Boolean flying(start=true)  \"true, if ball is flying\";\n" +
							"  Boolean impact;\n" +
							"  Real v_new;\n" +
							"  Integer foo;\n" +
							"equation\n" +   
							"  impact = h <= 0.0;\n" +
							"  foo = if impact then 1 else 2;\n" +
							"  der(v) = if flying then -g else 0;\n" +
							"  der(h) = v;\n" +
							"  when {h <= 0.0 and v <= 0.0,impact} then\n" +
							"    v_new = if edge(impact) then -e*pre(v) else 0;\n" +
							"    flying = v_new > 0;\n" +
							"    reinit(v, v_new);\n" +
							"  end when;\n" +
					"end BouncingBall;\n");
					jTextArea.repaint();
					jTextArea.append("\n// see if there were any errors");
					executeCommand("getErrorString()");
					jTextArea.append("\n// instantiate the BouncingBall model");
					executeCommand("instantiateModel(BouncingBall)"); executeCommand("getErrorString()");
					jTextArea.append("\n// check the BouncingBall model");
					executeCommand("instantiateModel(BouncingBall)"); executeCommand("getErrorString()");
					jTextArea.append("\n// simulate the BouncingBall model. The files will be generated in the current directory.");
					executeCommand("simulate(BouncingBall,stopTime=3.5)"); executeCommand("getErrorString()");
					jTextArea.append("\n// plot some of the BouncingBall model variables");
					executeCommand("plot({h, impact, v})"); executeCommand("getErrorString()");
					jTextArea.append("\n// ******************* BouncingBall DEMO DONE ****************");
					jTextArea.append("\n// ******************* Starting dcmotor script DEMO *************");
					executeCommand("readFile(\"sim_dcmotor.mos\")"); executeCommand("getErrorString()");
					executeCommand("runScript(\"sim_dcmotor.mos\")"); executeCommand("getErrorString()");
					jTextArea.append("\n// ****** DEMO OVER, see OpenModelica Users/System Guides for more API commands ********");
				}
			};
			t.start();
		}		
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			label = new Label();
			label.setText("Expression:");
			jPanel1 = new JPanel();
			JPanel jPanelX = new JPanel();
			jPanelX.setLayout(new BoxLayout(jPanelX, BoxLayout.X_AXIS));
			jPanelX.add(label, null);
			jPanelX.add(getJTextField(), null);
			jPanelX.add(getJButton(), null);
			jPanelX.add(getJDemoButton(), null);
			JPanel jPanelY = new JPanel();			
			jPanelY.setLayout(new BoxLayout(jPanelY, BoxLayout.Y_AXIS));
			jPanelY.add(jPanelX);
			Label helpLabel = new Label();			
			helpLabel.setText("Use <ENTER> or press Send to send the command, <Up>/<Down> keys for command history." +
					" Press Demo for a quick demonstration of available OMC commands.");
			jPanelY.add(helpLabel, null);
			jPanel1.add(jPanelY);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
			jSplitPane.setBottomComponent(getJScrollPane());
			jSplitPane.setTopComponent(getJPanel1());
		}
		return jSplitPane;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTextArea());
		}
		return jScrollPane;
	}

	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			executeCommand(jTextField.getText());
		}
		if (e.getKeyCode() == KeyEvent.VK_UP)
		{
			// set the last command in the text field
			jTextField.setText(history.get(history.size()-1));
			// move the last command at the beginning!
			String tmp = history.get(history.size()-1);
			history.add(0, tmp);
			history.remove(history.size()-1);
		}
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
		{
			// set the first command in the text field
			jTextField.setText(history.get(0));
			// move the first command at the end!
			String tmp = history.get(0);
			history.add(history.size()-1, tmp);
			history.remove(0);
		}
	}

	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
