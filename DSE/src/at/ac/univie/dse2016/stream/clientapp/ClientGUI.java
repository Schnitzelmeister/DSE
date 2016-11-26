package at.ac.univie.dse2016.stream.clientapp;

import at.ac.univie.dse2016.stream.common.*;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.JButton;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import java.awt.SystemColor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;

public class ClientGUI {

	public static BoerseAdmin boerseAdmin;
	public static ClientGUI window;
	
	private JFrame frmClientInterface;
	
	public javax.swing.DefaultListModel<Emittent> listModelEmittents;
	public javax.swing.DefaultListModel<Broker> listModelBrokers;
	private JTextField txtBoerseServer;
	private JButton btnConnect;
	private JTabbedPane tabbedPane;
	private BoerseStatus status;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField txtHttplocalhostwebservicespublic;
	private JTextField txtHttplocalhostrest;
	private JTextField txtHttplocalhostwebservicespublic_1;
	private JTextField txtHttplocalhostrest_1;
	private JTextField textField_5;
	private JTextField textField_6;
	private JTextField textField_7;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if (System.getSecurityManager() == null) {
			        //    System.setSecurityManager(new SecurityManager());
			        }
					
					ClientGUI.window = new ClientGUI();
					ClientGUI.window.frmClientInterface.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ClientGUI() {
		initialize();
	}

	public void setConnected(boolean connected) {
		txtBoerseServer.setEditable(!connected);
		btnConnect.setEnabled(!connected);
		/*
        if (status == BoerseStatus.Open) {
        	btnStartBoerse.setEnabled(false);
        	btnCloseBoerse.setEnabled(true);
        } else if (status == BoerseStatus.Closed) {
        	btnStartBoerse.setEnabled(true);
        	btnCloseBoerse.setEnabled(false);
        } else {
        	btnStartBoerse.setEnabled(false);
        	btnCloseBoerse.setEnabled(false);
        }*/
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		status = BoerseStatus.Closed;
		
		frmClientInterface = new JFrame();
		frmClientInterface.setResizable(false);
		frmClientInterface.setTitle("Client Interface");
		frmClientInterface.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent arg0) {
			}
		});
		frmClientInterface.setBounds(100, 100, 613, 520);
		frmClientInterface.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmClientInterface.getContentPane().setLayout(null);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(12, 26, 584, 452);
		frmClientInterface.getContentPane().add(tabbedPane);
		
		listModelEmittents = new javax.swing.DefaultListModel<Emittent>();
		
		JPanel panelConnection = new JPanel();
		tabbedPane.addTab("Connection", null, panelConnection, null);
		panelConnection.setLayout(null);
		
		JLabel lblRMIBoerse = new JLabel("RMI Boerse Connection Server:");
		lblRMIBoerse.setBounds(26, 35, 157, 15);
		panelConnection.add(lblRMIBoerse);
		
		txtBoerseServer = new JTextField();
		txtBoerseServer.setHorizontalAlignment(SwingConstants.LEFT);
		txtBoerseServer.setText("localhost");
		txtBoerseServer.setBounds(198, 32, 177, 19);
		panelConnection.add(txtBoerseServer);
		txtBoerseServer.setColumns(10);
		
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Registry registryBoerse = LocateRegistry.getRegistry(10001);
					boerseAdmin = (BoerseAdmin) registryBoerse.lookup("adminBoerse");
					
					listModelEmittents.clear();
		            for (Emittent e : ClientGUI.boerseAdmin.getEmittentsList())
		            	listModelEmittents.addElement(e);
		            
		            listModelBrokers.clear();
		            for (Broker b : ClientGUI.boerseAdmin.getBrokersList())
		            	listModelBrokers.addElement(b);

		            status = boerseAdmin.getStatus();
	            
		            setConnected(true);

				} catch (Exception e) {
					boerseAdmin = null;
					setConnected(false);
					e.printStackTrace();
				}

			}
		});
		btnConnect.setBounds(26, 362, 117, 25);
		panelConnection.add(btnConnect);
		
		textField = new JTextField();
		textField.setText("10001");
		textField.setBounds(452, 32, 60, 20);
		panelConnection.add(textField);
		textField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Port:");
		lblNewLabel.setBounds(414, 35, 24, 14);
		panelConnection.add(lblNewLabel);
		
		JLabel lblRmiBrokerConnection = new JLabel("UDP Boerse Connection Server:");
		lblRmiBrokerConnection.setBounds(26, 66, 157, 15);
		panelConnection.add(lblRmiBrokerConnection);
		
		textField_1 = new JTextField();
		textField_1.setText("localhost");
		textField_1.setHorizontalAlignment(SwingConstants.LEFT);
		textField_1.setEditable(true);
		textField_1.setColumns(10);
		textField_1.setBounds(198, 63, 177, 19);
		panelConnection.add(textField_1);
		
		JLabel label_1 = new JLabel("Port:");
		label_1.setBounds(414, 66, 24, 14);
		panelConnection.add(label_1);
		
		textField_2 = new JTextField();
		textField_2.setText("10000");
		textField_2.setColumns(10);
		textField_2.setBounds(452, 63, 60, 20);
		panelConnection.add(textField_2);
		
		JLabel label = new JLabel("RMI Broker Connection Server:");
		label.setBounds(26, 220, 157, 15);
		panelConnection.add(label);
		
		textField_3 = new JTextField();
		textField_3.setText("localhost");
		textField_3.setHorizontalAlignment(SwingConstants.LEFT);
		textField_3.setEditable(true);
		textField_3.setColumns(10);
		textField_3.setBounds(198, 217, 177, 19);
		panelConnection.add(textField_3);
		
		JLabel label_2 = new JLabel("Port:");
		label_2.setBounds(414, 220, 24, 14);
		panelConnection.add(label_2);
		
		textField_4 = new JTextField();
		textField_4.setText("5001");
		textField_4.setColumns(10);
		textField_4.setBounds(452, 217, 60, 20);
		panelConnection.add(textField_4);
		
		JLabel lblSoapBoerseConnection = new JLabel("SOAP Boerse Connection Server (URI with Port):");
		lblSoapBoerseConnection.setBounds(26, 112, 242, 15);
		panelConnection.add(lblSoapBoerseConnection);
		
		txtHttplocalhostwebservicespublic = new JTextField();
		txtHttplocalhostwebservicespublic.setText("http://localhost:8080/WebServices/public");
		txtHttplocalhostwebservicespublic.setHorizontalAlignment(SwingConstants.LEFT);
		txtHttplocalhostwebservicespublic.setEditable(true);
		txtHttplocalhostwebservicespublic.setColumns(10);
		txtHttplocalhostwebservicespublic.setBounds(278, 109, 234, 19);
		panelConnection.add(txtHttplocalhostwebservicespublic);
		
		txtHttplocalhostrest = new JTextField();
		txtHttplocalhostrest.setText("http://localhost:9999/rest/");
		txtHttplocalhostrest.setHorizontalAlignment(SwingConstants.LEFT);
		txtHttplocalhostrest.setEditable(true);
		txtHttplocalhostrest.setColumns(10);
		txtHttplocalhostrest.setBounds(278, 147, 234, 19);
		panelConnection.add(txtHttplocalhostrest);
		
		JLabel lblRestBoerseConnection = new JLabel("REST Boerse Connection Server (URI with Port):");
		lblRestBoerseConnection.setBounds(26, 150, 242, 15);
		panelConnection.add(lblRestBoerseConnection);
		
		JLabel lblSoapBrokerConnection = new JLabel("SOAP Broker Connection Server (URI with Port):");
		lblSoapBrokerConnection.setBounds(26, 258, 242, 15);
		panelConnection.add(lblSoapBrokerConnection);
		
		txtHttplocalhostwebservicespublic_1 = new JTextField();
		txtHttplocalhostwebservicespublic_1.setText("http://localhost:18080/WebServices/public");
		txtHttplocalhostwebservicespublic_1.setHorizontalAlignment(SwingConstants.LEFT);
		txtHttplocalhostwebservicespublic_1.setEditable(true);
		txtHttplocalhostwebservicespublic_1.setColumns(10);
		txtHttplocalhostwebservicespublic_1.setBounds(278, 255, 234, 19);
		panelConnection.add(txtHttplocalhostwebservicespublic_1);
		
		JLabel lblRestBrokerConnection = new JLabel("REST Broker Connection Server (URI with Port):");
		lblRestBrokerConnection.setBounds(26, 296, 242, 15);
		panelConnection.add(lblRestBrokerConnection);
		
		txtHttplocalhostrest_1 = new JTextField();
		txtHttplocalhostrest_1.setText("http://localhost:19999/rest/");
		txtHttplocalhostrest_1.setHorizontalAlignment(SwingConstants.LEFT);
		txtHttplocalhostrest_1.setEditable(true);
		txtHttplocalhostrest_1.setColumns(10);
		txtHttplocalhostrest_1.setBounds(278, 293, 234, 19);
		panelConnection.add(txtHttplocalhostrest_1);
		
		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setEnabled(true);
		btnDisconnect.setBounds(153, 362, 117, 25);
		panelConnection.add(btnDisconnect);
		
		
		JPanel panelMain = new JPanel();
		tabbedPane.addTab("Main", null, panelMain, null);
		//tabbedPane.setEnabledAt(1, connected);
		panelMain.setLayout(null);
		
		JList<Emittent> listEmittents = new JList<Emittent>(listModelEmittents);
		listEmittents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listEmittents.setBounds(25, 75, 175, 65);
		panelMain.add(listEmittents);
		
		JLabel lblKontostand = new JLabel("Kontostand:");
		lblKontostand.setBounds(25, 14, 73, 14);
		panelMain.add(lblKontostand);
		
		textField_5 = new JTextField();
		textField_5.setBounds(108, 11, 86, 20);
		panelMain.add(textField_5);
		textField_5.setColumns(10);
		
		JLabel lblMeineEmittents = new JLabel("Meine Emittents:");
		lblMeineEmittents.setBounds(25, 50, 86, 14);
		panelMain.add(lblMeineEmittents);
		
		JList list = new JList();
		list.setBounds(274, 42, 270, 98);
		panelMain.add(list);
		
		JLabel lblMeineAuftraege = new JLabel("Meine Auftraege:");
		lblMeineAuftraege.setBounds(274, 17, 86, 14);
		panelMain.add(lblMeineAuftraege);
		
		JList list_1 = new JList();
		list_1.setBounds(27, 218, 207, 157);
		panelMain.add(list_1);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setBounds(134, 179, 100, 20);
		panelMain.add(comboBox);
		
		JLabel lblEmittentSection = new JLabel("Emittent Section:");
		lblEmittentSection.setBounds(25, 182, 86, 14);
		panelMain.add(lblEmittentSection);
		
		JRadioButton rdbtnKaufen = new JRadioButton("Kaufen");
		rdbtnKaufen.setBounds(310, 215, 66, 23);
		panelMain.add(rdbtnKaufen);
		
		JRadioButton rdbtnVerkaufen = new JRadioButton("Verkaufen");
		rdbtnVerkaufen.setBounds(436, 215, 86, 23);
		panelMain.add(rdbtnVerkaufen);
		
		JLabel lblEmittent = new JLabel("Emittent:");
		lblEmittent.setBounds(310, 258, 46, 14);
		panelMain.add(lblEmittent);
		
		JComboBox comboBox_1 = new JComboBox();
		comboBox_1.setBounds(366, 255, 100, 20);
		panelMain.add(comboBox_1);
		
		JLabel lblAnzahl = new JLabel("Anzahl:");
		lblAnzahl.setBounds(310, 296, 46, 14);
		panelMain.add(lblAnzahl);
		
		textField_6 = new JTextField();
		textField_6.setBounds(366, 293, 86, 20);
		panelMain.add(textField_6);
		textField_6.setColumns(10);
		
		JLabel lblBedingungPreis = new JLabel("Bedingung - Preis:");
		lblBedingungPreis.setBounds(310, 338, 100, 14);
		panelMain.add(lblBedingungPreis);
		
		textField_7 = new JTextField();
		textField_7.setBounds(436, 335, 86, 20);
		panelMain.add(textField_7);
		textField_7.setColumns(10);
		
		JButton btnAccept = new JButton("Accept");
		btnAccept.setBounds(376, 366, 89, 23);
		panelMain.add(btnAccept);
		
		listModelBrokers = new javax.swing.DefaultListModel<Broker>();
		
		setConnected(false);
	}
}