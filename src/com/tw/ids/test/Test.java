/*******************************************************************************
 * @project: IDS_Test
 * @package: com.tw.ids.test
 * @file: Test.java
 * @author: zhangpei
 * @created: 2018-3-27
 * @purpose:
 * 
 * @version: 1.0
 * 
 * Revision History at the end of file.
 * 
 * Copyright 2018 AcconSys All rights reserved.
 ******************************************************************************/

package com.tw.ids.test;

import java.util.ArrayList;
import java.util.List;

public class Test {
	public static void main(String[] args) {
		double x = 1.0;
		double y = 2.0;
		
		double x1 = 4.0;
		double y1 = 6.0;
		
		double result = Math.sqrt(2.01);
		System.out.println(result);
		
		List<String> stringList = new ArrayList<String>();
		stringList.add(new String("1"));
		stringList.add(new String("2"));
		stringList.add(new String("3"));
		String string1 = new String("1");
		System.out.println(stringList.contains(string1));
//		for (int i = 0; i < 10; i++) {
//			String time = new SimpleDateFormat("yyyyMMddHHmmssSSS") .format(new Date() );
//			System.out.println(time);
//			
//		}
		
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