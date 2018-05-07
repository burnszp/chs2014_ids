/*******************************************************************************
 * @project: IDS_Test
 * @package: com.tw.ids.test
 * @file: IDS_CAVAL_AutoWire.java
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

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * 排列设备，按照连接关系最复杂的在中间，越简单连接设备越靠近外边。 一个设备上的连接器最多，说明连接关系最复杂。
 * 
 * @ClassName: IDS_CAVAL_AutoWire
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author Burns[张沛]
 * @date 2018-3-29 下午2:27:46
 * 
 */
public class IDS_CAVAL_AutoWire_20180404_3 {

	public static void main(String[] args) throws Exception {
		/**
		 * 第一步，得到document对象。
		 */
		Document document = getDocument();

		/**
		 * 第二步，修改得到的document对象
		 */

		/**
		 * 首先，读取功能
		 */
		Element topoplane = readAllElementsFromXMLDocument(document);
		// traverseBooks(virtualharnessList);

		/**
		 * 其次，修改功能 修改内容：将id为b002的元素的title改为Java Core，Price改为100.01
		 */
		ModifyInformationOfXMLDocument(topoplane);

		/**
		 * 再者：实现删除功能 删除内容：删除掉id为book1的元素内容。
		 */
		// deleteInformationOfXMLDocument(document);

		/**
		 * 最后：实现添加i新元素功能 添加内容：id为book3，title内容为：凤姐玉照，price内容为10000.00
		 */
		// addNewBookToXMLDocument(document);

		/**
		 * 第三步：将得到的document对象持久化保存到硬盘（XML）
		 */
//		writeToNewXMLDocument(document);
	}

	/**
	 * 实现了添加新节点：book的功能
	 * 
	 * @param document
	 */
	private static void addNewBookToXMLDocument(Document document) {
		Element root = document.getRootElement();
		Element newBook = root.addElement("book");
		newBook.addAttribute("id", "book3");
		Element title = newBook.addElement("title");
		title.setText("凤姐玉照");
		Element price = newBook.addElement("price");
		price.setText("10000.01");
	}

	/**
	 * 该方法实现了使用dom4j的删除元素的功能
	 * 
	 * @param document
	 */
	private static void deleteInformationOfXMLDocument(Document document) {
		Element root = document.getRootElement();
		for (Iterator it = root.elementIterator(); it.hasNext();) {
			Element book = (Element) it.next();
			String id = book.attributeValue("id");
			if ("book1".equals(id)) {
				Element parent = book.getParent();
				parent.remove(book);
			}
		}
	}

	/**
	 * 该方法的作用是修改document中的内容 将id为b002的元素的title改为Java Core，Price改为100.01
	 * 
	 * @param document
	 */
	private static void ModifyInformationOfXMLDocument(Element topoplane)
			throws Exception {
		List<Element> slotList = topoplane.elements("slot");
		List<Element> virtualconnectorList = topoplane.elements("virtualconnector");
		
		System.out.println("slotList.size()>>>"+slotList.size());
		System.out.println("virtualconnectorList.size()>>>"+virtualconnectorList.size());
		
	}

	/**
	 * 遍历集合
	 * 
	 * @param books
	 */
	private static void traverseBooks(List<Element> virtualharnessList) {
		for (Iterator<Element> iterator = virtualharnessList.iterator(); iterator
				.hasNext();) {
			Element virtualharness = iterator.next();
			System.out.println(virtualharness);
		}
	}

	/**
	 * 该方法实现了对xml文档的读取功能
	 * 
	 * @param document
	 * @return
	 */
	private static Element readAllElementsFromXMLDocument(Document document) {
		Element root = document.getRootElement();
		Element topoplane = root.element("designmgr").element("topologydesign")
				.element("planedesign").element("topoplane");
		return topoplane;
	}

	/*
	 * 方法二：推荐，速度最快 判断是否为整数
	 * 
	 * @param str 传入的字符串
	 * 
	 * @return 是整数返回true,否则返回false
	 */

	public static boolean isInteger(String str) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}

	/**
	 * 通过document对象将内存中的dom树保存到新的xml文档。
	 * 
	 * @param document
	 * @throws Exception
	 */
	private static void writeToNewXMLDocument(Document document)
			throws Exception {

		XMLWriter writer = new XMLWriter(new FileWriter(
				"D:/eclipse_wks/IDS_Test/20180404/integrator_2.xml"));
		// XMLWriter writer = new XMLWriter(new FileWriter(
		// "D:/eclipse_wks/IDS_Test/integratorDesign1_update.xml"));
		writer.write(document);
		writer.close();
	}

	/**
	 * 该方法用于得到document对象。
	 * 
	 * @return
	 * @throws Exception
	 */
	private static Document getDocument() throws Exception {
		SAXReader sr = new SAXReader();
		Document document = sr
				.read("D:/eclipse_wks/IDS_Test/20180404/integrator.xml");
		// Document document =
		// sr.read("D:/eclipse_wks/IDS_Test/integratorDesign1.xml");
		return document;
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
