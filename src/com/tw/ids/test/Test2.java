/*******************************************************************************
 * @project: IDS_Test
 * @package: com.tw.ids.test
 * @file: Test2.java
 * @author: zhangpei
 * @created: 2018-4-18
 * @purpose:
 * 
 * @version: 1.0
 * 
 * Revision History at the end of file.
 * 
 * Copyright 2018 AcconSys All rights reserved.
 ******************************************************************************/

package com.tw.ids.test;

public class Test2 {

	private static int j = 0;
	public static void main(String[] args) {
		
		int length = 30;
		for (int i = 0; i < length; i++) {
			System.out.println("=========="+j);
			
			int x = i;
			handle(x);
			
		}
	}
	
	public static void  handle(int x){
		j++;
		System.out.println("---"+j);
		x--;
		if(x>-1){
			handle(x);
		}
	}
}


/*******************************************************************************
 * <B>Revision History</B><BR>
 * [type 'revision' and press Alt + / to insert revision block]<BR>
 * 
 * 
 * 
 * Copyright 2018 AcconSys All rights reserved.
 ******************************************************************************/