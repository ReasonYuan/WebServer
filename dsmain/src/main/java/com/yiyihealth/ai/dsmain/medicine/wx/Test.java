package com.yiyihealth.ai.dsmain.medicine.wx;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

public class Test {
	public static void main(String[] args) {
		File desktopDir = FileSystemView.getFileSystemView().getHomeDirectory();
		String desktopPath = desktopDir.getAbsolutePath();
		
		System.out.println("-----------路径：---"+ desktopPath +"/Desktop/");
	}
	
	
}
