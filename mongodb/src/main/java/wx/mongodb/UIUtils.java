package wx.mongodb;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import wx.mongodb.DBUtils.onLoadDbOK;

public class UIUtils {

	/**
	 * 创建数据库插入数据的UI
	 */
	public static void createDbInsertUI(){
		final JFrame mFrame = new JFrame("mongodb manager");
		mFrame.setLayout(null);
		
		final JLabel tittle = new JLabel("加载数据库中。。。");
		tittle.setSize(100, 30);
		tittle.setBackground(Color.RED);
		tittle.setLocation(400, 20);
		mFrame.getContentPane().add(tittle);
		
		JLabel mJLabel = new JLabel("word");
		mJLabel.setSize(50, 30);
		mJLabel.setLocation(100, 100);
		mFrame.getContentPane().add(mJLabel);
		
		final JTextField mField = new JTextField();
		mField.setSize(50, 30);
		mField.setLocation((int) (mJLabel.getWidth() + mJLabel.getLocation().getX() + 10), (int) mJLabel.getLocation().getY());
		mFrame.getContentPane().add(mField);
		
		JButton mClearBtn = new JButton("清除");
		mClearBtn.setSize(50, 30);
		mClearBtn.setLocation((int) (mField.getWidth() + mField.getLocation().getX() + 10), (int) mField.getLocation().getY());
		mClearBtn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand() == "清除"){
					mField.setText("");
				}
			}
		});
		mFrame.getContentPane().add(mClearBtn);
		
		JLabel mJLabelUpdate = new JLabel("NatureName");
		mJLabelUpdate.setSize(100, 30);
		mJLabelUpdate.setLocation((int) (mClearBtn.getWidth() + mClearBtn.getLocation().getX() + 50), (int) mClearBtn.getLocation().getY() - 50);
		mFrame.getContentPane().add(mJLabelUpdate);
		JLabel mJLabelUpdateWord = new JLabel("NewNatureWord");
		mJLabelUpdateWord.setSize(150, 30);
		mJLabelUpdateWord.setLocation((int) (mClearBtn.getWidth() + mClearBtn.getLocation().getX() + 150), (int) mClearBtn.getLocation().getY() - 50);
		mFrame.getContentPane().add(mJLabelUpdateWord);
		
		final JTextField mFieldUpdate = new JTextField();
		mFieldUpdate.setSize(50, 30);
		mFieldUpdate.setLocation((int) (mClearBtn.getWidth() + mClearBtn.getLocation().getX() + 50), (int) mClearBtn.getLocation().getY());
		mFrame.getContentPane().add(mFieldUpdate);
		
		final JTextField mFieldUpdateWord = new JTextField();
		mFieldUpdateWord.setSize(50, 30);
		mFieldUpdateWord.setLocation((int) (mFieldUpdate.getWidth() + mFieldUpdate.getLocation().getX() + 100), (int) mFieldUpdate.getLocation().getY());
		mFrame.getContentPane().add(mFieldUpdateWord);
		
		JButton mClearBtnUpdate = new JButton("清除");
		mClearBtnUpdate.setSize(50, 30);
		mClearBtnUpdate.setLocation((int) (mFieldUpdateWord.getWidth() + mFieldUpdateWord.getLocation().getX() + 10), (int) mFieldUpdateWord.getLocation().getY());
		mClearBtnUpdate.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand() == "清除"){
					mFieldUpdate.setText("");
				}
			}
		});
		mFrame.getContentPane().add(mClearBtnUpdate);
		
		JLabel mJLabel2 = new JLabel("nature1");
		mJLabel2.setSize(50, 30);
		mJLabel2.setLocation(100, 150);
		mFrame.getContentPane().add(mJLabel2);
		
		final JTextField mField2 = new JTextField();
		mField2.setSize(50, 30);
		mField2.setLocation((int) (mJLabel2.getWidth() + mJLabel2.getLocation().getX() + 10), (int) mJLabel2.getLocation().getY());
		mFrame.getContentPane().add(mField2);
		
		JButton mClearBtn2 = new JButton("清除");
		mClearBtn2.setSize(50, 30);
		mClearBtn2.setLocation((int) (mField2.getWidth() + mField2.getLocation().getX() + 10), (int) mField2.getLocation().getY());
		mClearBtn2.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand() == "清除"){
					mField2.setText("");
				}
			}
		});
		mFrame.getContentPane().add(mClearBtn2);
		
		JLabel mJLabel3 = new JLabel("nature2");
		mJLabel3.setSize(50, 30);
		mJLabel3.setLocation(100, 200);
		mFrame.getContentPane().add(mJLabel3);
		
		final JTextField mField3 = new JTextField();
		mField3.setSize(50, 30);
		mField3.setLocation((int) (mJLabel3.getWidth() + mJLabel3.getLocation().getX() + 10), (int) mJLabel3.getLocation().getY());
		mFrame.getContentPane().add(mField3);
		
		JButton mClearBtn3 = new JButton("清除");
		mClearBtn3.setSize(50, 30);
		mClearBtn3.setLocation((int) (mField3.getWidth() + mField3.getLocation().getX() + 10), (int) mField3.getLocation().getY());
		mClearBtn3.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand() == "清除"){
					mField3.setText("");
				}
			}
		});
		mFrame.getContentPane().add(mClearBtn3);
		
		
		
		JButton mClearBtnInsert = new JButton("插入");
		mClearBtnInsert.setSize(50, 30);
		mClearBtnInsert.setLocation(100,(int) (mClearBtn3.getLocation().getY() + 50));
		mClearBtnInsert.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand() == "插入"){
					//插入数据库
					if(mField.getText().toString().length() != 0 && mField2.getText().toString().length() != 0){
						ArrayList<String> naturelist = new ArrayList<String>();
						naturelist.add(mField2.getText().toString());
						if(mField3.getText().toString().length() != 0){
							naturelist.add(mField3.getText().toString());
						}
						String tittle = "";
						try {
							DBUtils.inserWord(mField.getText().toString(), naturelist);
							tittle =  "插入成功";
							
						} catch (Exception e2) {
							tittle =  "插入失败";
						}
						JDialog mDialog = new JDialog(mFrame, tittle);
						mDialog.setSize(300, 100);
						mDialog.setLocationRelativeTo(null);
						mDialog.setVisible(true);
					}
				}
			}
		});
		
		JButton mClearBtnDelete = new JButton("删除");
		mClearBtnDelete.setSize(50, 30);
		mClearBtnDelete.setLocation(170,(int) (mClearBtn3.getLocation().getY() + 50));
		mClearBtnDelete.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand() == "删除"){
					if(mField.getText().toString().length() != 0){
						DBUtils.DeleteOneWord(mField.getText().toString());
					}
				}
			}
		});
		
		final JTextArea mQueryMessage = new JTextArea();
		JScrollPane scroll = new JScrollPane(mQueryMessage); 
		scroll.setHorizontalScrollBarPolicy( 
		JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); 
		scroll.setVerticalScrollBarPolicy( 
		JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); 
		scroll.setSize(600, 250);
		scroll.setLocation(100, (int) (mClearBtnInsert.getLocation().getY() + 30));
		
		JButton mClearBtnQuery = new JButton("查询");
		mClearBtnQuery.setSize(50, 30);
		mClearBtnQuery.setLocation(230,(int) (mClearBtn3.getLocation().getY() + 50));
		mClearBtnQuery.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand() == "查询"){
					//
					if(mField.getText().toString().length() != 0){
						String ss = DBUtils.queryOneDocument(mField.getText().toString());
						mQueryMessage.setText(ss);
					}
				}
			}
		});
		
		JButton mClearBtnQueryAll = new JButton("查询所有");
		mClearBtnQueryAll.setSize(80, 30);
		mClearBtnQueryAll.setLocation(300,(int) (mClearBtn3.getLocation().getY() + 50));
		mClearBtnQueryAll.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand() == "查询所有"){
					//
					String ss = DBUtils.queryAllDocment();
					mQueryMessage.setText(ss);
				}
			}
		});
		
		JButton mClearBtnAll = new JButton("清空所有");
		mClearBtnAll.setSize(80, 30);
		mClearBtnAll.setLocation(400,(int) (mClearBtn3.getLocation().getY() + 50));
		mClearBtnAll.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand() == "清空所有"){
					mQueryMessage.setText("");
					mField.setText("");
					mField2.setText("");
					mField3.setText("");
				}
			}
		});
		
		JButton mUpdateNature = new JButton("更新nature");
		mUpdateNature.setSize(120, 30);
		mUpdateNature.setLocation(500,(int) (mClearBtn3.getLocation().getY() + 50));
		mUpdateNature.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand() == "更新nature"){
					
					String tittle = "";
					try {
						DBUtils.updateOneDocument(mField.getText().toString(), mFieldUpdate.getText().toString(), mFieldUpdateWord.getText().toString());
						tittle =  "更新成功";
						
					} catch (Exception e2) {
						tittle =  "更新失败";
					}
					JDialog mDialog = new JDialog(mFrame, tittle);
					mDialog.setSize(300, 100);
					mDialog.setLocationRelativeTo(null);
					mDialog.setVisible(true);
				}
			}
		});
		
		mFrame.getContentPane().add(mUpdateNature);
		mFrame.getContentPane().add(mClearBtnInsert);
		mFrame.getContentPane().add(mClearBtnDelete);
		mFrame.getContentPane().add(mClearBtnQuery);
		mFrame.getContentPane().add(scroll);
		mFrame.getContentPane().add(mClearBtnQueryAll);
		mFrame.getContentPane().add(mClearBtnAll);
		
		mFrame.setSize(800, 600);
		mFrame.setVisible(true);
		
		mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mFrame.setLocationRelativeTo(null);
		DBUtils.setLoadCallBack(new onLoadDbOK() {
			
			public void callBakc() {
				tittle.setText("加载完毕");
			}
		});
		mFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				DBUtils.closeClient();
			}
		});
	}
	
	
	public static void main(String[] args) {
		createDbInsertUI();
	}
}
