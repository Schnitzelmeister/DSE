package at.ac.univie.dse2016.stream.clientapp;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import java.awt.Font;
import javax.swing.JButton;
import net.miginfocom.swing.MigLayout;
import javax.swing.JRadioButton;
import java.awt.Component;
import javax.swing.Box;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.JCheckBox;

public class ClientGUI extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientGUI frame = new ClientGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ClientGUI() {
		setResizable(false);
		setTitle("Klient Verwaltung");
		setForeground(Color.WHITE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 639, 482);
		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setForeground(Color.GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblKlientname = new JLabel("Klientname:");
		lblKlientname.setBounds(12, 15, 68, 16);
		contentPane.add(lblKlientname);
		
		textField = new JTextField();
		textField.setBounds(186, 12, 85, 22);
		textField.setEditable(false);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JLabel lblKontostand = new JLabel("Kontostand:");
		lblKontostand.setBounds(12, 41, 68, 16);
		contentPane.add(lblKontostand);
		
		textField_1 = new JTextField();
		textField_1.setBounds(186, 38, 85, 22);
		textField_1.setEditable(false);
		textField_1.setColumns(10);
		contentPane.add(textField_1);
		
		JLabel lblMeineAktien = new JLabel("Meine Aktien:");
		lblMeineAktien.setBounds(12, 93, 80, 17);
		lblMeineAktien.setFont(new Font("Tahoma", Font.PLAIN, 14));
		contentPane.add(lblMeineAktien);
		
		JLabel lblMeineAuftrge = new JLabel("Meine Auftr\u00E4ge:");
		lblMeineAuftrge.setBounds(333, 90, 121, 17);
		lblMeineAuftrge.setFont(new Font("Tahoma", Font.PLAIN, 14));
		contentPane.add(lblMeineAuftrge);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setBounds(462, 90, 159, 22);
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"auftrag1.toString();", "auftrag2.toString();", "auftrag3.toString();", "auftrag4.toString();"}));
		contentPane.add(comboBox);
		
		JButton btnStornieren = new JButton("Stornieren");
		btnStornieren.setBounds(333, 116, 93, 25);
		contentPane.add(btnStornieren);
		
		JLabel lblAktiveAuftrge = new JLabel("Aktive Auftr\u00E4ge:");
		lblAktiveAuftrge.setBounds(12, 264, 98, 17);
		lblAktiveAuftrge.setFont(new Font("Tahoma", Font.PLAIN, 14));
		contentPane.add(lblAktiveAuftrge);
		
		JLabel lblAuftragHinzufgen = new JLabel("Auftrag hinzuf\u00FCgen:");
		lblAuftragHinzufgen.setBounds(333, 264, 121, 17);
		lblAuftragHinzufgen.setFont(new Font("Tahoma", Font.PLAIN, 14));
		contentPane.add(lblAuftragHinzufgen);
		
		JRadioButton rdbtnKaufen = new JRadioButton("Kaufen");
		rdbtnKaufen.setBounds(462, 285, 65, 25);
		rdbtnKaufen.setBackground(Color.WHITE);
		contentPane.add(rdbtnKaufen);
		
		JButton btnBesttigen = new JButton("Best\u00E4tigen");
		btnBesttigen.setBounds(333, 416, 91, 25);
		contentPane.add(btnBesttigen);
		
		JRadioButton rdbtnVerkaufen = new JRadioButton("Verkaufen");
		rdbtnVerkaufen.setBounds(531, 285, 83, 25);
		rdbtnVerkaufen.setBackground(Color.WHITE);
		contentPane.add(rdbtnVerkaufen);
		
		ButtonGroup group = new ButtonGroup();
	    group.add(rdbtnVerkaufen);
	    group.add(rdbtnKaufen);
	    
	    JScrollPane scrollPane = new JScrollPane();
	    scrollPane.setBounds(12, 285, 266, 156);
	    contentPane.add(scrollPane);
	    
	    JList list = new JList();
	    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    scrollPane.setViewportView(list);
	    list.setModel(new AbstractListModel() {
	    	String[] values = new String[] {"auftrag1.toString();", "auftrag2.toString();", "auftrag3.toString();", "auftrag4.toString();", "auftrag5.toString();", "auftrag6.toString();", "auftrag7.toString();", "auftrag1.toString();", "auftrag2.toString();", "auftrag3.toString();", "auftrag4.toString();", "auftrag5.toString();", "auftrag6.toString();", "auftrag7.toString();"};
	    	public int getSize() {
	    		return values.length;
	    	}
	    	public Object getElementAt(int index) {
	    		return values[index];
	    	}
	    });
	    
	    JScrollPane scrollPane_1 = new JScrollPane();
	    scrollPane_1.setBounds(12, 119, 266, 132);
	    contentPane.add(scrollPane_1);
	    
	    JList list_1 = new JList();
	    list_1.setModel(new AbstractListModel() {
	    	String[] values = new String[] {"AAPL", "MSFT", "APHT", "AAPL", "MSFT", "APHT", "AAPL", "MSFT", "APHT", "AAPL", "MSFT", "APHT"};
	    	public int getSize() {
	    		return values.length;
	    	}
	    	public Object getElementAt(int index) {
	    		return values[index];
	    	}
	    });
	    scrollPane_1.setViewportView(list_1);
	    list_1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    
	    JLabel lblTicker = new JLabel("Ticker:");
	    lblTicker.setBounds(410, 313, 56, 22);
	    contentPane.add(lblTicker);
	    
	    textField_2 = new JTextField();
	    textField_2.setBounds(462, 313, 152, 22);
	    contentPane.add(textField_2);
	    textField_2.setColumns(10);
	    
	    textField_3 = new JTextField();
	    textField_3.setColumns(10);
	    textField_3.setBounds(462, 341, 152, 22);
	    contentPane.add(textField_3);
	    
	    JLabel lblAnzahl = new JLabel("Anzahl:");
	    lblAnzahl.setBounds(410, 341, 56, 22);
	    contentPane.add(lblAnzahl);
	    
	    JCheckBox chckbxBedingung = new JCheckBox("Bedingung:");
	    chckbxBedingung.setBackground(Color.WHITE);
	    chckbxBedingung.setBounds(366, 364, 93, 35);
	    contentPane.add(chckbxBedingung);
	    
	    textField_4 = new JTextField();
	    textField_4.setColumns(10);
	    textField_4.setBounds(462, 370, 152, 22);
	    contentPane.add(textField_4);
	}
}
