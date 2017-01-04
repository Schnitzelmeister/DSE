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

	public static ClientGUI window;

	private BoersePublic boersePublic;
	private BrokerClient brokerClient;
	private BoerseStatus status;
	private int brokerId;
	private int clientId;
	
	private JFrame frmClientInterface;
	
	private java.util.TreeMap<String, Emittent> emittents;
	
	public Integer emittentIdByTicker(String ticker) {
		return emittents.get(ticker).getId();
	}
	
	//public javax.swing.DefaultListModel<Emittent> listModelEmittents;
	//public javax.swing.DefaultListModel<Broker> listModelBrokers;
	private JTextField txtBoerseServer;
	private JButton btnConnect;
	private JTabbedPane tabbedPane;
	
	private JTextField txtBoerseRMIPort;
	private JTextField txtBoerseUDPPort;
	private JTextField txtBrokerRMIServer;
	private JTextField txtBrokerRMIPort;
	private JTextField txtBoerseSOAPServer;
	private JTextField txtBoerseRESTServer;
	private JTextField txtBrokerSOAPServer;
	private JTextField txtBrokerRESTServer;
	private JTextField txtKontoStand;
	private JTextField txtAnzahl;
	private JTextField txtBedingung;
	
	private JComboBox<Emittent> cmbEmittentSection;
	private JComboBox<Emittent> cmbEmittent;
	private JList<Emittent> listAccountEmittents;
	private javax.swing.ListModel<Auftrag> auftraege;
	private javax.swing.ListModel<String> kontoEmittents;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		if (args.length < 2)
			throw new IllegalArgumentException("arguments: brokerId clientId {remoteHostBoerse remotePortUDPBoerse remotePortRMIBoerse remoteHostBroker remotePortRMIBroker}");

		int brokerId = Integer.valueOf(args[0]);
		int clientId = Integer.valueOf(args[1]);
		
		String remoteHostBoerse = "localhost";
		int remotePortUDPBoerse = 10000;
		int remotePortRMIBoerse = 10001;
		if (args.length > 2)
			remoteHostBoerse = args[2];
		if (args.length > 3)
			remotePortUDPBoerse = Integer.valueOf(args[3]);
		if (args.length > 4)
			remotePortRMIBoerse = Integer.valueOf(args[4]);
		
		String remoteHostBroker = "localhost";
		int remotePortRMIBroker = 10002;	
		if (args.length > 5)
			remoteHostBroker = args[5];
		if (args.length > 6)
			remotePortRMIBroker = Integer.valueOf(args[6]);
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					if (System.getSecurityManager() == null) {
			        //    System.setSecurityManager(new SecurityManager());
			        }
					
					ClientGUI.window = new ClientGUI();
					window.brokerId = brokerId;
					window.clientId = clientId;
					//window.txtBoerseServer.setText(remoteHostBoerse);
					//window.txtBoerseRMIPort.setText(String.valueOf(remotePortRMIBoerse));
					//window.txtBoerseUDPPort.setText(String.valueOf(remotePortUDPBoerse));

					//window.txtBrokerRMIServer.setText(remoteHostBoerse);
					//window.txtBrokerRMIPort.setText(String.valueOf(remotePortRMIBoerse));


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
		
		//status = BoerseStatus.Closed;
		
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
		
		//listModelEmittents = new javax.swing.DefaultListModel<Emittent>();
		
		JPanel panelConnection = new JPanel();
		tabbedPane.addTab("Connection", null, panelConnection, null);
		panelConnection.setLayout(null);
		
		JLabel lblRMIBoerse = new JLabel("RMI Boerse Connection Server:");
		lblRMIBoerse.setBounds(26, 35, 157, 15);
		panelConnection.add(lblRMIBoerse);
		
		txtBoerseServer = new JTextField();
		txtBoerseServer.setHorizontalAlignment(SwingConstants.LEFT);
		txtBoerseServer.setText("localhost");
		txtBoerseServer.setBounds(198, 47, 177, 19);
		panelConnection.add(txtBoerseServer);
		txtBoerseServer.setColumns(10);
		
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					java.util.ArrayList<Emittent> ret_emittents;
					//emittents = new java.util.TreeMap<Integer, Emittent>();
					
					Registry registryBoerse = LocateRegistry.getRegistry(window.txtBoerseServer.getText(), Integer.valueOf(window.txtBoerseRMIPort.getText()));
					boersePublic = (BoersePublic) registryBoerse.lookup("public");
					
					cmbEmittent.removeAll();
					cmbEmittentSection.removeAll();
					//cmbEmittentSection.addItem( new Object() { public String toString() { return "<not selected>"; } } );
					ret_emittents = boersePublic.getEmittentsList();
					for (Emittent e : ret_emittents ) {
						//emittents.put(e.getId(), e);
						//cmbEmittent.addItem( new Object() { public String toString() { return e.getTicker(); } } );
						//cmbEmittentSection.addItem( new Object() { public String toString() { return e.getTicker() + " - " + e.getName(); } } );
		            }
		            
		            status = boersePublic.getStatus();
	            
					Registry registryBroker = LocateRegistry.getRegistry(window.txtBrokerRESTServer.getText(), Integer.valueOf(window.txtBrokerRMIPort.getText()));
					brokerClient = (BrokerClient) registryBroker.lookup("client");
					
					Client clientState = brokerClient.getState(clientId);
		            
					txtKontoStand.setText( Float.toString(clientState.getKontostand()) );
		            
					//javax.swing.DefaultListModel listAccountEmittentsModel = new javax.swing.DefaultListModel();
					

					//listAccountEmittents.removeAll();
		            for (java.util.Map.Entry<Integer, Integer> e : clientState.getAccountEmittents().entrySet()) {
		            	//kontoEmittents
		            	//listAccountEmittentsModel.addElement(  emittents.get(e.getKey()).getTicker() + " = " + e.getValue() + " Stueck" );
		            }

					
		            
		            setConnected(true);

				} catch (Exception e) {
					boersePublic = null;
					setConnected(false);
					e.printStackTrace();
				}

			}
		});
		btnConnect.setBounds(24, 339, 117, 25);
		panelConnection.add(btnConnect);
		
		txtBoerseRMIPort = new JTextField();
		txtBoerseRMIPort.setText("10001");
		txtBoerseRMIPort.setBounds(452, 32, 60, 20);
		panelConnection.add(txtBoerseRMIPort);
		txtBoerseRMIPort.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Port:");
		lblNewLabel.setBounds(414, 35, 24, 14);
		panelConnection.add(lblNewLabel);
		
		JLabel lblRmiBrokerConnection = new JLabel("UDP Boerse Connection Server:");
		lblRmiBrokerConnection.setBounds(26, 66, 157, 15);
		panelConnection.add(lblRmiBrokerConnection);
		
		JLabel label_1 = new JLabel("Port:");
		label_1.setBounds(414, 66, 24, 14);
		panelConnection.add(label_1);
		
		txtBoerseUDPPort = new JTextField();
		txtBoerseUDPPort.setText("10000");
		txtBoerseUDPPort.setColumns(10);
		txtBoerseUDPPort.setBounds(452, 63, 60, 20);
		panelConnection.add(txtBoerseUDPPort);
		
		JLabel label = new JLabel("RMI Broker Connection Server:");
		label.setBounds(26, 227, 157, 15);
		panelConnection.add(label);
		
		txtBrokerRMIServer = new JTextField();
		txtBrokerRMIServer.setText("localhost");
		txtBrokerRMIServer.setHorizontalAlignment(SwingConstants.LEFT);
		txtBrokerRMIServer.setEditable(true);
		txtBrokerRMIServer.setColumns(10);
		txtBrokerRMIServer.setBounds(198, 224, 177, 19);
		panelConnection.add(txtBrokerRMIServer);
		
		JLabel label_2 = new JLabel("Port:");
		label_2.setBounds(414, 227, 24, 14);
		panelConnection.add(label_2);
		
		txtBrokerRMIPort = new JTextField();
		txtBrokerRMIPort.setText("5001");
		txtBrokerRMIPort.setColumns(10);
		txtBrokerRMIPort.setBounds(452, 224, 60, 20);
		panelConnection.add(txtBrokerRMIPort);
		
		JLabel lblSoapBoerseConnection = new JLabel("SOAP Boerse Connection Server (URL with Port):");
		lblSoapBoerseConnection.setBounds(26, 112, 242, 15);
		panelConnection.add(lblSoapBoerseConnection);
		
		txtBoerseSOAPServer = new JTextField();
		txtBoerseSOAPServer.setText("http://localhost:8080/WebServices/public");
		txtBoerseSOAPServer.setHorizontalAlignment(SwingConstants.LEFT);
		txtBoerseSOAPServer.setEditable(true);
		txtBoerseSOAPServer.setColumns(10);
		txtBoerseSOAPServer.setBounds(278, 109, 234, 19);
		panelConnection.add(txtBoerseSOAPServer);
		
		txtBoerseRESTServer = new JTextField();
		txtBoerseRESTServer.setText("http://localhost:9999/rest/");
		txtBoerseRESTServer.setHorizontalAlignment(SwingConstants.LEFT);
		txtBoerseRESTServer.setEditable(true);
		txtBoerseRESTServer.setColumns(10);
		txtBoerseRESTServer.setBounds(278, 147, 234, 19);
		panelConnection.add(txtBoerseRESTServer);
		
		JLabel lblRestBoerseConnection = new JLabel("REST Boerse Connection Server (URL with Port):");
		lblRestBoerseConnection.setBounds(26, 150, 242, 15);
		panelConnection.add(lblRestBoerseConnection);
		
		JLabel lblSoapBrokerConnection = new JLabel("SOAP Broker Connection Server (URL with Port):");
		lblSoapBrokerConnection.setBounds(26, 265, 242, 15);
		panelConnection.add(lblSoapBrokerConnection);
		
		txtBrokerSOAPServer = new JTextField();
		txtBrokerSOAPServer.setText("http://localhost:18080/WebServices/public");
		txtBrokerSOAPServer.setHorizontalAlignment(SwingConstants.LEFT);
		txtBrokerSOAPServer.setEditable(true);
		txtBrokerSOAPServer.setColumns(10);
		txtBrokerSOAPServer.setBounds(278, 262, 234, 19);
		panelConnection.add(txtBrokerSOAPServer);
		
		JLabel lblRestBrokerConnection = new JLabel("REST Broker Connection Server (URL with Port):");
		lblRestBrokerConnection.setBounds(26, 303, 242, 15);
		panelConnection.add(lblRestBrokerConnection);
		
		txtBrokerRESTServer = new JTextField();
		txtBrokerRESTServer.setText("http://localhost:19999/rest/");
		txtBrokerRESTServer.setHorizontalAlignment(SwingConstants.LEFT);
		txtBrokerRESTServer.setEditable(true);
		txtBrokerRESTServer.setColumns(10);
		txtBrokerRESTServer.setBounds(278, 300, 234, 19);
		panelConnection.add(txtBrokerRESTServer);
		
		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setEnabled(true);
		btnDisconnect.setBounds(151, 339, 117, 25);
		panelConnection.add(btnDisconnect);
		
		
		JPanel panelMain = new JPanel();
		tabbedPane.addTab("Main", null, panelMain, null);
		//tabbedPane.setEnabledAt(1, connected);
		panelMain.setLayout(null);
		
		listAccountEmittents = new JList<Emittent>();
		listAccountEmittents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listAccountEmittents.setBounds(25, 75, 175, 65);
		panelMain.add(listAccountEmittents);
		
		JLabel lblKontostand = new JLabel("Kontostand:");
		lblKontostand.setBounds(25, 14, 73, 14);
		panelMain.add(lblKontostand);
		
		txtKontoStand = new JTextField();
		txtKontoStand.setEditable(false);
		txtKontoStand.setBounds(108, 11, 86, 20);
		panelMain.add(txtKontoStand);
		txtKontoStand.setColumns(10);
		
		JLabel lblMeineEmittents = new JLabel("Meine Emittents:");
		lblMeineEmittents.setBounds(25, 50, 86, 14);
		panelMain.add(lblMeineEmittents);
		
		JList<Auftrag> listAuftraege = new JList<Auftrag>(auftraege);
		listAuftraege.setBounds(274, 49, 270, 98);
		panelMain.add(listAuftraege);
		
		JLabel lblMeineAuftraege = new JLabel("Meine Auftraege:");
		lblMeineAuftraege.setBounds(274, 17, 86, 14);
		panelMain.add(lblMeineAuftraege);
		
		JList list_1 = new JList();
		list_1.setBounds(27, 218, 207, 157);
		panelMain.add(list_1);
		
		cmbEmittentSection = new JComboBox();
		cmbEmittentSection.setBounds(134, 179, 100, 20);
		panelMain.add(cmbEmittentSection);
		
		JLabel lblEmittentSection = new JLabel("Emittent Section:");
		lblEmittentSection.setBounds(25, 182, 86, 14);
		panelMain.add(lblEmittentSection);
		
		JRadioButton rdbtnKaufen = new JRadioButton("Kaufen");
		rdbtnKaufen.setSelected(true);
		rdbtnKaufen.setBounds(297, 215, 66, 23);
		panelMain.add(rdbtnKaufen);
		
		JRadioButton rdbtnVerkaufen = new JRadioButton("Verkaufen");
		rdbtnVerkaufen.setBounds(436, 215, 86, 23);
		panelMain.add(rdbtnVerkaufen);
		
		JLabel lblEmittent = new JLabel("Emittent:");
		lblEmittent.setBounds(299, 248, 46, 14);
		panelMain.add(lblEmittent);
		
		cmbEmittent = new JComboBox();
		cmbEmittent.setBounds(368, 245, 100, 20);
		panelMain.add(cmbEmittent);
		
		JLabel lblAnzahl = new JLabel("Anzahl:");
		lblAnzahl.setBounds(297, 276, 46, 14);
		panelMain.add(lblAnzahl);
		
		txtAnzahl = new JTextField();
		txtAnzahl.setBounds(366, 273, 86, 20);
		panelMain.add(txtAnzahl);
		txtAnzahl.setColumns(10);
		
		JLabel lblBedingungPreis = new JLabel("Bedingung - Preis:");
		lblBedingungPreis.setBounds(297, 304, 100, 14);
		panelMain.add(lblBedingungPreis);
		
		txtBedingung = new JTextField();
		txtBedingung.setBounds(436, 301, 86, 20);
		panelMain.add(txtBedingung);
		txtBedingung.setColumns(10);
		
		JButton btnAccept = new JButton("Accept");
		btnAccept.setBounds(355, 353, 89, 23);
		panelMain.add(btnAccept);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setBounds(271, 11, 104, 20);
		frmClientInterface.getContentPane().add(comboBox);
		
		JButton btnStartBot = new JButton("Start Bot");
		btnStartBot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//listAuftraege.;
			}
		});
		btnStartBot.setBounds(393, 10, 89, 23);
		frmClientInterface.getContentPane().add(btnStartBot);
		
		JButton btnStopBot = new JButton("Stop Bot");
		btnStopBot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				javax.swing.JPopupMenu popup = new javax.swing.JPopupMenu();
				javax.swing.JMenuItem menuItem = new javax.swing.JMenuItem("A popup menu item");
			    menuItem.addActionListener(this);
			    popup.add(menuItem);
			    
			    //javax.swing.MouseListener popupListener = new javax.swing.PopupListener();
			    //output.addMouseListener(popupListener);
			    //menuBar.addMouseListener(popupListener);
				
				 
			}
		});
		btnStopBot.setBounds(492, 10, 89, 23);
		frmClientInterface.getContentPane().add(btnStopBot);
		
		//listModelBrokers = new javax.swing.DefaultListModel<Broker>();
		
		setConnected(false);
	}
}