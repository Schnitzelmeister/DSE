package at.ac.univie.dse2016.stream.clientapp;

import at.ac.univie.dse2016.stream.common.*;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.SystemColor;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import java.awt.Color;
import java.awt.Font;

public class ClientGUI {

	static class MyRunnable implements Runnable {
		private int brokerId;
		private int clientId;
		private String remoteHostBoerse;
		private int remotePortUDPBoerse;
		private int remotePortRMIBoerse;
		private String remoteHostBoerseSOAP;
		private String remoteHostBoerseREST;
		private String remoteHostBroker;
		private int remotePortRMIBroker;	

	    public MyRunnable (int brokerId, int clientId, 
	    		String remoteHostBoerse, int remotePortUDPBoerse, int remotePortRMIBoerse,
	    		String remoteHostBoerseSOAP, String remoteHostBoerseREST,
	    		String remoteHostBroker, int remotePortRMIBroker) {
	    	this.brokerId = brokerId;
	    	this.clientId = clientId;
	    	this.remoteHostBoerse = remoteHostBoerse;
	    	this.remotePortUDPBoerse = remotePortUDPBoerse;
	    	this.remotePortRMIBoerse = remotePortRMIBoerse;
	    	this.remoteHostBoerseSOAP = remoteHostBoerseSOAP;
	    	this.remoteHostBoerseREST = remoteHostBoerseREST;
	    	this.remoteHostBroker = remoteHostBroker;
	    	this.remotePortRMIBroker = remotePortRMIBroker;
	    }
	    
	    @Override
	    public void run() {
			try {
				if (System.getSecurityManager() == null) {
		        //    System.setSecurityManager(new SecurityManager());
		        }
				
				ClientGUI gui = new ClientGUI();
				gui.brokerId = brokerId;
				gui.clientId = clientId;
				gui.txtBoerseServer.setText(remoteHostBoerse);
				gui.txtBoerseRMIPort.setText(String.valueOf(remotePortRMIBoerse));
				gui.txtBoerseUDPPort.setText(String.valueOf(remotePortUDPBoerse));

				gui.txtBoerseSOAPServer.setText(remoteHostBoerseSOAP);
				gui.txtBoerseRESTServer.setText(remoteHostBoerseREST);
				
				gui.txtBrokerRMIServer.setText(remoteHostBroker);
				if (remotePortRMIBroker > 0)
					gui.txtBrokerRMIPort.setText(String.valueOf(remotePortRMIBroker));

				
				//load Bots
				Class.forName("at.ac.univie.dse2016.stream.clientapp.Bot");
				
				Vector<String> bots = new Vector<String>();
				for (Class<?> cls : getClassesForPackage(Package.getPackage("at.ac.univie.dse2016.stream.clientapp"))) {
					if (cls.getSuperclass() == at.ac.univie.dse2016.stream.clientapp.Bot.class) {
						bots.addElement(cls.getAnnotation(at.ac.univie.dse2016.stream.clientapp.BotDescription.class).Description() + " <" + cls.getName() + ">");
					}
				}
				gui.cmbBots.setModel(new DefaultComboBoxModel<String>(bots));

				
				gui.frmClientInterface.setVisible(true);
				
				gui.UpdateControls();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	};
	
	
	static class FeedMsgUDP {
		public int Id;
		public boolean Buy;
		public int Amount;
		public float Price;

	    public FeedMsgUDP (int id, boolean buy, int amount, float price) {
	    	this.Id = id;
	    	this.Buy = buy;
	    	this.Amount = amount;
	    	this.Price = price;
	    }
	    
		public String toString() { 
			if (Buy) {
			    return String.format("%10d", this.Amount) + "  -  " + String.format("%9.4f%n", this.Price) + " EUR";
			}
			else {
			    return "                " +  String.format("%9.4f%n", this.Price) + " EUR  -  " + String.format("%10d", this.Amount);
			}
				
		} 
	    
	};

	
	@SuppressWarnings("serial")
	static class PopUp extends JPopupMenu {
	    JMenuItem anItem;
	    public PopUp(){
	        anItem = new JMenuItem("Cancel order");
	        add(anItem);
	    }
	    
	    public void setListener(ActionListener l) {
	        anItem.addActionListener(l);
	    }
	}
	
	private BoersePublic boersePublic = null;
	private BrokerClient brokerClient = null;
	
	//active Bot
	private Bot bot;
	private int brokerId;
	private int clientId;
	private int sessionId = -1;


	//gui controls
	private JFrame frmClientInterface;
	private JPanel panelMain;
	private JComboBox<String> cmbBots;
	private JButton btnStartBot;
	private JTextField txtBoerseServer;
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
	private JList<Auftrag> listAuftraege;
	private JList<String> listUDP;
	private javax.swing.DefaultListModel<String> aktiveUDP;
	private JButton btnAccept;
	private JTextField txtLastTransaction;

	java.util.TreeMap< Integer /* auftragId */, Auftrag > auftraege;
	private javax.swing.DefaultListModel<Auftrag> aktiveAuftrage;

	private JComboBox<Emittent> cmbEmittentSection;
	private JComboBox<Emittent> cmbEmittent;
	private JList<String> listAccountEmittents;

	private java.util.TreeMap<Integer, FeedMsgUDP> feedMsgs;
	private java.util.TreeMap<Integer, Integer> accountEmittents;

	private java.util.TreeMap<String, Emittent> emittents;
	public Integer emittentIdByTicker(String ticker) {
		return emittents.get(ticker).getId();
	}
	public String emittentTickerById(int id) {
		for (Emittent e : emittents.values())
			if (e.getId() == id)
				return e.getTicker();
		
		return null;
	}

//	private javax.swing.DefaultListModel<Emittent> listModelEmittents;
//	private javax.swing.DefaultListModel<Auftrag> auftraege;
//	private javax.swing.DefaultListModel<String> kontoEmittents;

	
	protected void UpdateControls() {
		boolean connected =  (boersePublic != null);

		this.listAccountEmittents.setEnabled(connected);
		this.listAuftraege.setEnabled(connected);
		//this.listUDP.setEnabled(connected);
		this.cmbEmittentSection.setEnabled(connected);
		this.btnAccept.setEnabled(connected);
		
		this.txtBoerseServer.setEditable(!connected);
		this.txtBoerseSOAPServer.setEditable(!connected);
		this.txtBoerseRESTServer.setEditable(!connected);
		this.txtBoerseRMIPort.setEditable(!connected);
		this.txtBoerseUDPPort.setEditable(!connected);
		this.txtBrokerRMIServer.setEditable(!connected);
		this.txtBrokerRMIPort.setEditable(!connected);
		
		this.btnStartBot.setEnabled(connected && this.cmbBots.getSelectedItem() != null);
	}
		
	protected void Connect() {
		try {
	        Registry registryBoerse = LocateRegistry.getRegistry(this.txtBoerseServer.getText(), Integer.valueOf(this.txtBoerseRMIPort.getText()));
	        this.boersePublic = (BoersePublic) registryBoerse.lookup("public");
	
	        this.emittents.clear();
	        for (Emittent e : this.boersePublic.getEmittentsList()) {
	        	this.emittents.put(e.getTicker(), e);
	        }
	        
	        this.cmbEmittentSection.setModel( new javax.swing.DefaultComboBoxModel( new java.util.Vector (this.emittents.values()) ) );
	        this.cmbEmittent.setModel( new javax.swing.DefaultComboBoxModel( new java.util.Vector (this.emittents.values()) ) );
	        
	        if (this.txtBrokerSOAPServer.getText().length() == 0) {
	        	this.txtBrokerSOAPServer.setText( this.boersePublic.getBrokerNetworkAddress(brokerId, NetworkResource.SOAP)  );
	        }
	        if (this.txtBrokerRESTServer.getText().length() == 0) {
	        	this.txtBrokerRESTServer.setText( this.boersePublic.getBrokerNetworkAddress(brokerId, NetworkResource.REST)  );
	        }

	        if (this.txtBrokerRMIServer.getText().length() == 0) {
	            String remoteHost = boersePublic.getBrokerNetworkAddress(brokerId, NetworkResource.RMI);
	            String[] ar = remoteHost.split(":");
	        	this.txtBrokerRMIServer.setText( ar[0]  );
	        	this.txtBrokerRMIPort.setText( ar[1]  );
	        }

	        Registry registryBroker = LocateRegistry.getRegistry(this.txtBrokerRMIServer.getText(), Integer.valueOf(this.txtBrokerRMIPort.getText()));
	        this.brokerClient = (BrokerClient) registryBroker.lookup("client");
	
	        this.UpdateClientInfo();
	        
	        this.getFeedUDP();
		}
		catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage());
		}

	}
	
	protected void Disconnect() {
		this.brokerClient = null;
		this.boersePublic = null;
		this.stopFeedUDP();
		this.UpdateControls();
	}
	
	
	protected void cancelOrder() {
		try {
			this.brokerClient.auftragCancel(this.clientId, this.listAuftraege.getSelectedValue().getId());
		}
		catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage());
		}

	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void UpdateClientInfo() {
		try {
			Client clientState = this.brokerClient.getState(clientId);
	        
			txtKontoStand.setText( Float.toString(clientState.getKontostand()) );
	        
	        this.accountEmittents.clear();
	        java.util.Vector vek = new java.util.Vector();
	        for (java.util.Map.Entry<Integer,Integer> entry : clientState.getAccountEmittents().entrySet() ) {
	        	this.accountEmittents.put(entry.getKey(), entry.getValue());
	        	vek.addElement(this.emittentTickerById(entry.getKey()) +  " - " + entry.getValue());
	        }
	        
	        
	        this.listAccountEmittents.setModel( new javax.swing.DefaultComboBoxModel( vek ) );
	        auftraege.clear();
	        this.aktiveAuftrage.clear();
	        
	        java.util.TreeSet<Auftrag> els = this.brokerClient.getAuftraege(this.clientId);
	        if (els != null) {
		        for (Auftrag a : els) {
		        	auftraege.put(a.getId(), a);
		        	this.aktiveAuftrage.addElement(a);
		        }
	        }
	        
	        this.listAuftraege.setModel( this.aktiveAuftrage );			
	        
	        this.UpdateControls();
		}
		catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage());

		}
	}

	

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
		
		String remoteHostBoerseSOAP = "http://localhost:8080/WebServices/public";
		String remoteHostBoerseREST = "http://localhost:9999/rest/";
		if (args.length > 5)
			remoteHostBoerseSOAP = args[5];
		if (args.length > 6)
			remoteHostBoerseREST = args[6];
		
		
		String remoteHostBroker = "";
		int remotePortRMIBroker = 0;	
		if (args.length > 7)
			remoteHostBroker = args[7];
		if (args.length > 8)
			remotePortRMIBroker = Integer.valueOf(args[8]);
		
		
		EventQueue.invokeLater(new MyRunnable(brokerId, clientId, 
		    	 remoteHostBoerse, remotePortUDPBoerse, remotePortRMIBoerse,
		    	 remoteHostBoerseSOAP, remoteHostBoerseREST,
		    	 remoteHostBroker, remotePortRMIBroker) 
		);
		
		/*
		EventQueue.invokeLater(new Runnable() {			
			public void run() {	
				try {		
					if (System.getSecurityManager() == null) {			       
						//    System.setSecurityManager(new SecurityManager());			       
					}										
						ClientGUI gui = new ClientGUI();	
				
						gui.frmClientInterface.setVisible(true);			
				} catch (Exception e) {					e.printStackTrace();				
				}		
				}		
			});*/
	}

	/**
	 * Create the application.
	 */
	public ClientGUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		this.feedMsgs = new java.util.TreeMap<Integer, FeedMsgUDP>();
		this.auftraege = new java.util.TreeMap< Integer /* auftragId */, Auftrag >();
		//this.listModelEmittents = new javax.swing.DefaultListModel<Emittent>();
		//this.auftraege = new javax.swing.DefaultListModel<Auftrag>();
		//this.kontoEmittents = new javax.swing.DefaultListModel<String>();
		this.emittents = new java.util.TreeMap<String, Emittent>();
		this.accountEmittents = new java.util.TreeMap<Integer, Integer>();
		//status = BoerseStatus.Closed;
		this.aktiveAuftrage = new javax.swing.DefaultListModel<Auftrag>();
		this.aktiveUDP = new javax.swing.DefaultListModel<String>();
		
		frmClientInterface = new JFrame();
		frmClientInterface.setResizable(false);
		frmClientInterface.setTitle("Client Interface");
		frmClientInterface.setBounds(100, 100, 613, 520);
		frmClientInterface.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmClientInterface.getContentPane().setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
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
		
		JButton btnConnect = new JButton("Connect RMI");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Connect();
					UpdateClientInfo();
				} catch (Exception e) {
					boersePublic = null;
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
		txtBrokerRMIServer.setHorizontalAlignment(SwingConstants.LEFT);
		txtBrokerRMIServer.setEditable(true);
		txtBrokerRMIServer.setColumns(10);
		txtBrokerRMIServer.setBounds(198, 224, 177, 19);
		panelConnection.add(txtBrokerRMIServer);
		
		JLabel label_2 = new JLabel("Port:");
		label_2.setBounds(414, 227, 24, 14);
		panelConnection.add(label_2);
		
		txtBrokerRMIPort = new JTextField();
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
		txtBrokerSOAPServer.setHorizontalAlignment(SwingConstants.LEFT);
		txtBrokerSOAPServer.setEditable(true);
		txtBrokerSOAPServer.setColumns(10);
		txtBrokerSOAPServer.setBounds(278, 262, 234, 19);
		panelConnection.add(txtBrokerSOAPServer);
		
		JLabel lblRestBrokerConnection = new JLabel("REST Broker Connection Server (URL with Port):");
		lblRestBrokerConnection.setBounds(26, 303, 242, 15);
		panelConnection.add(lblRestBrokerConnection);
		
		txtBrokerRESTServer = new JTextField();
		txtBrokerRESTServer.setHorizontalAlignment(SwingConstants.LEFT);
		txtBrokerRESTServer.setEditable(true);
		txtBrokerRESTServer.setColumns(10);
		txtBrokerRESTServer.setBounds(278, 300, 234, 19);
		panelConnection.add(txtBrokerRESTServer);
		
		JButton btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				boersePublic = null;
				UpdateClientInfo();
			}
		});
		btnDisconnect.setEnabled(true);
		btnDisconnect.setBounds(151, 339, 117, 25);
		panelConnection.add(btnDisconnect);
		
		
		panelMain = new JPanel();
		tabbedPane.addTab("Main", null, panelMain, null);
		//tabbedPane.setEnabledAt(1, connected);
		panelMain.setLayout(null);
		
		listAccountEmittents = new JList<String>();
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
		
		listAuftraege = new JList<Auftrag>(this.aktiveAuftrage);
		// Then on your component(s)
		listAuftraege.addMouseListener(new MouseListener() {
		    public void mousePressed(MouseEvent e){
		        if (e.isPopupTrigger())
		            doPop(e);
		    }

		    
		    public void mouseReleased(MouseEvent e){
		        if (e.isPopupTrigger())
		            doPop(e);
		    }

		    private void doPop(MouseEvent e){
		    	if (listAuftraege.isSelectionEmpty())
		    		return;
		        PopUp menu = new PopUp();
		        menu.show(e.getComponent(), e.getX(), e.getY());
		        menu.setListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						cancelOrder();
					}
				});
		    }
		    

		     public void mouseEntered(MouseEvent e) {
		     }

		     public void mouseExited(MouseEvent e) {
		     }

		     public void mouseClicked(MouseEvent e) {
		     }
		});
		listAuftraege.setBounds(274, 49, 270, 98);
		panelMain.add(listAuftraege);
		
		JLabel lblMeineAuftraege = new JLabel("Meine Auftraege:");
		lblMeineAuftraege.setBounds(274, 17, 86, 14);
		panelMain.add(lblMeineAuftraege);
		
		listUDP = new JList<String>(this.aktiveUDP);
		listUDP.setBounds(27, 247, 207, 157);
		panelMain.add(listUDP);
		
		cmbEmittentSection = new JComboBox<Emittent>();
		cmbEmittentSection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getFeedUDP();
			}
		});

		cmbEmittentSection.setBounds(134, 163, 100, 20);
		panelMain.add(cmbEmittentSection);
		
		JLabel lblEmittentSection = new JLabel("Emittent Section:");
		lblEmittentSection.setBounds(25, 166, 86, 14);
		panelMain.add(lblEmittentSection);
		
		JRadioButton rdbtnKaufen = new JRadioButton("Kaufen");
		rdbtnKaufen.setSelected(true);
		rdbtnKaufen.setBounds(297, 215, 66, 23);
		panelMain.add(rdbtnKaufen);
		
		JRadioButton rdbtnVerkaufen = new JRadioButton("Verkaufen");
		rdbtnVerkaufen.setBounds(436, 215, 86, 23);
		panelMain.add(rdbtnVerkaufen);
		
		ButtonGroup group = new ButtonGroup();
	    group.add(rdbtnKaufen);
	    group.add(rdbtnVerkaufen);

		
		JLabel lblEmittent = new JLabel("Emittent:");
		lblEmittent.setBounds(299, 248, 46, 14);
		panelMain.add(lblEmittent);
		
		cmbEmittent = new JComboBox<Emittent>();
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
		
		btnAccept = new JButton("Accept");
		btnAccept.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					Auftrag auftrag;
					if (txtBedingung.getText().length() == 0)
						auftrag = new Auftrag(clientId, rdbtnKaufen.isSelected(), ((Emittent)cmbEmittent.getSelectedItem()).getTicker(), Integer.valueOf(txtAnzahl.getText()));
					else
						auftrag = new Auftrag(clientId, rdbtnKaufen.isSelected(), ((Emittent)cmbEmittent.getSelectedItem()).getTicker(), Integer.valueOf(txtAnzahl.getText()), Float.valueOf(txtBedingung.getText()));
					int auftragId = brokerClient.auftragAddNew(clientId, auftrag);
					aktiveAuftrage.addElement(new Auftrag(auftragId, auftrag.getOwnerId(), auftrag.getKaufen(), auftrag.getTicker(), auftrag.getAnzahl(), auftrag.getBedingung()) );
					txtAnzahl.setText("");
					txtBedingung.setText("");
					
					UpdateControls();
				}
				catch(Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, e.getMessage());
				}
			}
		});
		btnAccept.setBounds(355, 353, 89, 23);
		panelMain.add(btnAccept);
		
		txtLastTransaction = new JTextField();
		txtLastTransaction.setFont(new Font("Tahoma", Font.BOLD, 11));
		txtLastTransaction.setText("NO DATA");
		txtLastTransaction.setForeground(Color.RED);
		txtLastTransaction.setBackground(Color.YELLOW);
		txtLastTransaction.setColumns(10);
		txtLastTransaction.setBounds(25, 207, 209, 32);
		panelMain.add(txtLastTransaction);
		
		JLabel lblNewLabel_1 = new JLabel("Last Transaction:");
		lblNewLabel_1.setBackground(Color.YELLOW);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblNewLabel_1.setForeground(Color.RED);
		lblNewLabel_1.setBounds(25, 191, 209, 14);
		panelMain.add(lblNewLabel_1);
		
		cmbBots = new JComboBox<String>();
		cmbBots.setBounds(168, 11, 207, 20);
		frmClientInterface.getContentPane().add(cmbBots);
		
		btnStartBot = new JButton("Start Bot");
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
		
	}
	
	public void actionPerformed(ActionEvent e) {
		//if (e.getSource() == this.btnAccept)
		 
		JOptionPane.showMessageDialog(null, aktiveAuftrage.size());
		this.UpdateControls();
		
	}

	
	
	
	//UDP-Server stuff
	/**
	 * UDP-Thread des Servers
	 */
	private ScheduledExecutorService execUDP;
	private DatagramSocket socketUDP;
	
	//Emittents, die via UDP abgehoert sind
	private Integer[] udpEmittentIds;
	//UDP Counters - Fehlererkennung
	private java.util.TreeMap< Integer /* emittentId */, Integer > udpCounters;


	/**
	 * Start UDP Listener
	 */
	public void getFeedUDP() throws IllegalArgumentException {
		Integer[] emittentIds = new Integer[1];
		emittentIds[0] = ((Emittent)this.cmbEmittentSection.getSelectedItem()).getId();
		
		getFeedUDP(emittentIds);
	}
	
	/**
	 * Start UDP Listener
	 */
	public void getFeedUDP(Integer[] emittentIds) throws IllegalArgumentException {
        
		if (emittentIds == null || emittentIds.length == 0)
			throw new IllegalArgumentException("arg is empty");

		//stop current UDP Listener
		if (execUDP != null)
			execUDP.shutdown();

		this.udpEmittentIds = emittentIds;
		
		this.aktiveUDP.clear();
		
		//init counters
		this.udpCounters = new java.util.TreeMap< Integer, Integer >();
		for (byte i = 0; i < this.udpEmittentIds.length; ++i)
			this.udpCounters.put(this.udpEmittentIds[i], -1);
		
		
		try {
			socketUDP = new DatagramSocket();
			execUDP = Executors.newScheduledThreadPool(2);
	
			//start UDP Listener
			execUDP.schedule(new Runnable() {
				  @Override
				  public void run() {
					  listenUDPResponse();
				  }
				}, 0, TimeUnit.SECONDS);
			
			//start UDP Request every 5 seconds
			execUDP.scheduleAtFixedRate(new Runnable() {
			  @Override
			  public void run() {
				  sendUDPrequest();
			  }
			}, 0, 5, TimeUnit.SECONDS);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void listenUDPResponse() {
	    try {
	    	byte[] buf = new byte[64 * 1024];
	    	
//System.out.println( "listenUDPResponse START" );
	    	
	    	
	    	do {
				DatagramPacket requestPacket = new DatagramPacket(buf, buf.length);
				socketUDP.receive(requestPacket);
//System.out.println( "listenUDPResponse" );
			    ByteArrayInputStream bis = new ByteArrayInputStream(requestPacket.getData());
			    ObjectInput in = new ObjectInputStream(bis);
			    for (byte i = 0; i < 255; ++i) {
			    	try {
			    		FeedMsg feed = (FeedMsg) in.readObject();
			    		processFeedMsg(feed);
//System.out.println( "listenUDPResponse process" );

			    	}
			    	catch(java.io.EOFException e) { break; }
			    	catch(java.io.StreamCorruptedException e) { break; }
			    	catch(java.io.OptionalDataException e) { break; }
			    	
			    }
//System.out.println( "listenUDPResponse end" );
			    in.close();
			    bis.close();
			    
	    	} while (true);
	    }   
	    catch (SocketException e){
	        System.err.println("Socket: " + e.getMessage());
	        e.printStackTrace();
	    }
	    catch (IOException e){
	    	System.err.println("IO: " + e.getMessage());
	    	e.printStackTrace();
	    }
	    catch (ClassNotFoundException e){
	    	System.err.println("IO: " + e.getMessage());
	    	e.printStackTrace();
	    }
	    catch (Exception e){
	    	System.err.println("Exception: " + e.getMessage());
	    	e.printStackTrace();
	    }
	    
//System.out.println( "listenUDPResponse BUY" );
	}
	
	private void processFeedMsg(FeedMsg feedMsg) {
		
		System.out.println( "get feed = " + feedMsg.getCounter() + " " + feedMsg.getAnzahl() +  " " + feedMsg.getPrice() + " " + feedMsg.getId()  + " " + feedMsg.getId2() + " " +  feedMsg.getStatus() );

		if (feedMsg.getStatus().equals(AuftragStatus.Bearbeitet) || feedMsg.getStatus().equals(AuftragStatus.TeilweiseBearbeitet))
			txtLastTransaction.setText(feedMsg.getAnzahl() + " items, price=" + feedMsg.getPrice() );

		if (feedMsg.getCounter().equals(-1) && feedMsg.getAnzahl().equals(0)) {
			if (this.sessionId < feedMsg.getId())
				this.sessionId = feedMsg.getId();
			return;
		}



		
		//Fehlererkennung
		if (!(this.udpCounters.get(feedMsg.getTickerId()).equals(-1))) {

			//same packet
			if ( this.udpCounters.get(feedMsg.getTickerId()) >= feedMsg.getCounter() )
				return;
							
			//Fehler - mach etwas, vielleicht kan man einfach boerse.getState aufrufen
			if (feedMsg.getCounter() != this.udpCounters.get(feedMsg.getTickerId()) + 1) {
				System.out.println( "Fehlererkennung!!!" );
				//updateStatus();
			}
		}
		
		this.udpCounters.put(feedMsg.getTickerId(), feedMsg.getCounter());
		
		if (feedMsg.getStatus().equals(AuftragStatus.Accepted)) {
			if (this.feedMsgs.containsKey(feedMsg.getId())) {
				this.feedMsgs.get(feedMsg.getId()).Amount = feedMsg.getAnzahl();
				System.out.println( "this.feedMsgs.EDIT = " + feedMsg.getId() );
			}
			else {
				this.feedMsgs.put(feedMsg.getId(), new FeedMsgUDP(feedMsg.getId(), feedMsg.getKaufen(), feedMsg.getAnzahl(), feedMsg.getPrice()));
				System.out.println( "this.feedMsgs.put = " + feedMsg.getId() );
			}
				
			this.updateUDPGlass();

		}
		else if (feedMsg.getStatus().equals(AuftragStatus.Canceled)) {
			if (this.feedMsgs.containsKey(feedMsg.getId())) {
				this.feedMsgs.remove(feedMsg.getId());
				
				this.updateUDPGlass();
			}
			
		}
		
		if (feedMsg.getId() != -1)
			processFeedMsgAuftrag(feedMsg, false);
		if (feedMsg.getId2() != -1)
			processFeedMsgAuftrag(feedMsg, true);
	}

	private void updateUDPGlass() {
		final int itemsCount = 5;
		
		Vector<String> itemsUDP = new Vector<String>();
		
		FeedMsgUDP[] sorted = this.feedMsgs.values().toArray(new FeedMsgUDP[this.feedMsgs.values().size()]); 
		
		java.util.Arrays.sort(sorted, new java.util.Comparator<FeedMsgUDP>() {
		    public int compare(FeedMsgUDP o1, FeedMsgUDP o2) {
		        if (o1.Buy && !o2.Buy) {
		        	return 0;
		        }
		        else if (o1.Buy && o2.Buy) {
		        	if (o2.Price == o1.Price)
		        		return 0;
		        	else if (o2.Price > o1.Price)
		        		return -1;
		        	else
		        		return 1;
		        }
		        else if (!o1.Buy && !o2.Buy) {
		        	return 1;
		        }
		        else if (!o1.Buy && o2.Buy) {
		        	if (o2.Price == o1.Price)
		        		return 0;
		        	else if (o2.Price > o1.Price)
		        		return 1;
		        	else
		        		return -1;
		        }
		        
		        return 0;
		    }
		});

		int pos = 0;
		for (int i = 0; i < sorted.length; ++i) {
			if (sorted[i].Buy) {
				pos = i; break;
			}
		}

		int start = 0;
		int finish = sorted.length;
		if (pos > itemsCount)
			start = pos - itemsCount;
		if (pos + itemsCount < sorted.length)
			finish = pos + itemsCount;

		System.out.println( sorted.length +  " updateUDPGlass from " + start + " till " + finish );

		for (int i = start; i < finish; ++i) {
			itemsUDP.addElement(sorted[i].toString());
		}

		this.listUDP.setModel(new DefaultComboBoxModel<String>(itemsUDP));
//		JOptionPane.showMessageDialog(null, "ok");
	}
	
	private void processFeedMsgAuftrag(FeedMsg feedMsg, boolean id2) {

		int id;
		AuftragStatus status;
		if (id2) {
			id = feedMsg.getId2();
			status = feedMsg.getStatus2();
		}
		else {
			id = feedMsg.getId();
			status = feedMsg.getStatus();
		}
		
		System.out.println( "processFeedMsg Auftrag , auftragId= " + id + ", status= " + status + " " + feedMsg.getId2() );

System.out.println( "this.auftraege.containsKey(id)=" + this.auftraege.containsKey(id) );

		//search if this Auftrag Data is our Client Auftrag 
		if (this.auftraege.containsKey(id)) {
			this.UpdateClientInfo();
		}
		
System.out.println( "this.feedMsgs.containsKey(id)=" + this.feedMsgs.containsKey(id) );
		if (this.feedMsgs.containsKey(id) && (status.equals(AuftragStatus.TeilweiseBearbeitet)) && (status.equals(AuftragStatus.Bearbeitet))) {
			if (status.equals(AuftragStatus.TeilweiseBearbeitet))
				this.feedMsgs.get(id).Amount = feedMsg.getAnzahl();
			else
				this.feedMsgs.remove(id);
			
			this.updateUDPGlass();
		}
	}

	
	
	private void sendUDPrequest() {
		
	    try {
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutput out = new ObjectOutputStream(bos);
	    	out.writeObject(new FeedRequest(this.sessionId, this.udpEmittentIds));
	    	
			DatagramPacket requestPacket = new DatagramPacket(bos.toByteArray(), bos.size(), InetAddress.getByName(this.txtBoerseServer.getText()), Integer.valueOf(this.txtBoerseUDPPort.getText()));
			socketUDP.send(requestPacket);
			//System.out.println( "send Addr = " + requestPacket.getAddress().toString() );
			//System.out.println( "send Port = " + requestPacket.getPort() );

            
	    	out.close();
    	    bos.close();
    	}   
	    catch (SocketException e){
	    	e.printStackTrace();
	        System.err.println("Socket: " + e.getMessage());
	    }
	    catch (IOException e){
	    	e.printStackTrace();
	    	System.err.println("IO: " + e.getMessage());
	    }
	}

	
	/**
	 * Stop UDP Listener
	 */
	public void stopFeedUDP() throws IllegalArgumentException {
		socketUDP.close();
		socketUDP = null;
	}

	
	
	
	
	
	
	private static ArrayList<Class<?>> getClassesForPackage(Package pkg) {
	    String pkgname = pkg.getName();
	    ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
	    // Get a File object for the package
	    File directory = null;
	    String fullPath;
	    String relPath = pkgname.replace('.', '/');
	    //System.out.println("ClassDiscovery: Package: " + pkgname + " becomes Path:" + relPath);
	    URL resource = ClientGUI.class.getClassLoader().getResource(relPath);
	    //System.out.println("ClassDiscovery: Resource = " + resource);
	    if (resource == null) {
	        throw new RuntimeException("No resource for " + relPath);
	    }
	    fullPath = resource.getFile();
	    //System.out.println("ClassDiscovery: FullPath = " + resource);

	    try {
	        directory = new File(resource.toURI());
	    } catch (URISyntaxException e) {
	        throw new RuntimeException(pkgname + " (" + resource + ") does not appear to be a valid URL / URI.  Strange, since we got it from the system...", e);
	    } catch (IllegalArgumentException e) {
	        directory = null;
	    }
	    //System.out.println("ClassDiscovery: Directory = " + directory);

	    if (directory != null && directory.exists()) {
	        // Get the list of the files contained in the package
	        String[] files = directory.list();
	        for (int i = 0; i < files.length; i++) {
	            // we are only interested in .class files
	            if (files[i].endsWith(".class")) {
	                // removes the .class extension
	                String className = pkgname + '.' + files[i].substring(0, files[i].length() - 6);
	                //System.out.println("ClassDiscovery: className = " + className);
	                try {
	                    classes.add(Class.forName(className));
	                } 
	                catch (ClassNotFoundException e) {
	                    throw new RuntimeException("ClassNotFoundException loading " + className);
	                }
	            }
	        }
	    }
	    else {
            String jarPath = fullPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
            JarFile jarFile = null;         
	        try {
	        	jarFile = new JarFile(jarPath);
	            Enumeration<JarEntry> entries = jarFile.entries();
	            while(entries.hasMoreElements()) {
	                JarEntry entry = entries.nextElement();
	                String entryName = entry.getName();
	                if(entryName.startsWith(relPath) && entryName.length() > (relPath.length() + "/".length())) {
	                    //System.out.println("ClassDiscovery: JarEntry: " + entryName);
	                    String className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
	                    //System.out.println("ClassDiscovery: className = " + className);
	                    try {
	                        classes.add(Class.forName(className));
	                    } 
	                    catch (ClassNotFoundException e) {
	                        throw new RuntimeException("ClassNotFoundException loading " + className);
	                    }
	                }
	            }
	        } catch (IOException e) {
	            throw new RuntimeException(pkgname + " (" + directory + ") does not appear to be a valid package", e);
	        }
	        finally {
	        	try {
					jarFile.close();
				} catch (Exception e) {
				}
	        }
	    }
	    return classes;
	}
}