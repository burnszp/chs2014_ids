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
import java.util.Set;
import java.util.regex.Pattern;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * 所有harness中bundle和node重名的问题
 * 
 * @ClassName: IDS_CAVAL_AutoWire
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author Burns[张沛]
 * @date 2018-3-29 下午2:27:46
 * 
 */
public class IDS_CAVAL_AutoWire_20180413 {

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
		// traverseBooks(topoplane);

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
		 writeToNewXMLDocument(document);
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
		// 1、获取当前xml文件中id的最大号，添加元素时依次增加id.
		List<Element> elements = getDocument().getRootElement().elements();
		List<Attribute> idList = new ArrayList<Attribute>();
		for (Element element : elements) {
			Attribute id = element.attribute("id");
			if (id != null) {
				idList.add(id);
				// System.out.println(id.getValue());
			}
		}
		int idMax = 0;
		for (Attribute id : idList) {
			String idIndex = id.getValue().substring(1);
			if (isInteger(idIndex)) {
				if (idMax < Integer.parseInt(idIndex)) {
					idMax = Integer.parseInt(idIndex);
				}
			}
		}
		System.out.println("idMax>>:" + idMax);

		// 2、计算一根电缆两个连接器x、y左边的各个中间值。
		String uid1 = "UID822c59-1626649577f-1ed30ffe71c1334";
		String uid2 = "UID822c59-1626649577f-1ed30ffe71c1335";

		List<Element> virtualharnessList = topoplane.elements("virtualharness");
		System.out.println("virtualharnessList.size():"
				+ virtualharnessList.size());
		int count1 = 0;
		int count = 0;
		
		Map<String,List<Element>> bundleMap = new HashMap<String,List<Element>>();
		Map<String,List<Element>> nodeMap = new HashMap<String,List<Element>>();
		

		for (int i = 0; i < virtualharnessList.size(); i++) {
			String time = new SimpleDateFormat("yyyyMMddHHmmssSSS")
					.format(new Date());
			Element virtualharness = (Element) virtualharnessList.get(i);

			// 把每个virtualharness中的phd下的每个node判断是否有connector_id，分成两组。
			Map<String, Element> hasPropertyConnector_idNodeMap = new HashMap<String, Element>();
			Map<String, Element> notHasPropertyConnector_idNodeMap = new HashMap<String, Element>();

			List<Element> nodeList = virtualharness.element("phd").elements(
					"node");
			for (Element node : nodeList) {
				if (node.attribute("connector_id") != null) {
					hasPropertyConnector_idNodeMap.put(
							node.attributeValue("id"), node);
				} else {
					notHasPropertyConnector_idNodeMap.put(
							node.attributeValue("id"), node);
				}
			}

			System.out.println("hasPropertyConnector_idNodeMap.size():"
					+ hasPropertyConnector_idNodeMap.size());
			System.out.println("notHasPropertyConnector_idNodeMap.size():"
					+ notHasPropertyConnector_idNodeMap.size());

			// 根据hasPropertyConnector_idNodeMap中的node的坐标算出一个平均值坐标，作为一个harenss的中心坐标的node坐标，然后把notHasPropertyConnector_idNodeMap中的node都替换成中心坐标的node。
			Map<String, Element> toponodeShouldHoldMap = new HashMap<String, Element>();
			List<Element> toponodeList = virtualharness.element("topoharness")
					.elements("toponode");
			for (Element toponode : toponodeList) {
				String connref = toponode.attributeValue("connref");
				if (hasPropertyConnector_idNodeMap.get(connref) != null) {
					toponodeShouldHoldMap.put(toponode.attributeValue("id"),
							toponode);
				}
			}
			
			double sumX = 0;
			double sumY = 0;
			String attributeref = null;
			for (Element toponode : toponodeShouldHoldMap.values()) {
				sumX = sumX + Double.parseDouble(toponode.attributeValue("x"));
				sumY = sumY + Double.parseDouble(toponode.attributeValue("y"));
				attributeref = toponode.attributeValue("attributeref");
			}
			
			//先删除bundle，保证添加的node在bundle之前
			Element phd = virtualharness.element("phd");
			List<Element> bundleListTemp = phd.elements("bundle");

			for (Element bundleTemp : bundleListTemp) {
				phd.remove(bundleTemp);
			}
			
			//创建node节点。
			Element node = phd.addElement("node");
			node.addAttribute("id", "_"+String.valueOf(idMax++));
			node.addAttribute("name", "_N_"+idMax);
			node.addAttribute("nameindex", ""+idMax);
			node.addAttribute("baseid", uid1+time);
			node.addAttribute("isreference", "0");
			node.addAttribute("ismarker", "0");
			node.addAttribute("node", "_N_"+idMax);
			node.addAttribute("pokehome", "false");
			
			//把删除的bundle添加上。
			for (Element bundleTemp : bundleListTemp) {
				phd.add(bundleTemp);
			}
			
			
			Element topoharness = virtualharness.element("topoharness");
			List<Element> topobundleListTemp = topoharness.elements("topobundle");
			for (Element topobundleTemp : topobundleListTemp) {
				topoharness.remove(topobundleTemp);
			}
		
			
			
			//创建toponode节点
			Element toponode = topoharness.addElement("toponode");
			toponode.addAttribute("id", "_"+String.valueOf(idMax++));
			toponode.addAttribute("connref", node.attributeValue("id"));
			toponode.addAttribute("x", String.valueOf(sumX/toponodeShouldHoldMap.size()));
			toponode.addAttribute("y", String.valueOf(sumY/toponodeShouldHoldMap.size()));
			toponode.addAttribute("attributeref", attributeref);
//			toponode.addAttribute("ismarker", "0");
			toponode.addAttribute("isexcludefromapplystyle", "false");
			toponode.addAttribute("visibility", "true");
			
			for (Element topobundleTemp : topobundleListTemp) {
				topoharness.add(topobundleTemp);
			}
			
			//把bundle中不和设备关联的node用创建的node节点替换。
			List<Element> bundleList = virtualharness.element("phd").elements("bundle");
			for (Element bundle : bundleList) {
				String startnoderef = bundle.attributeValue("startnoderef");
				String endnoderef = bundle.attributeValue("endnoderef");
				if(hasPropertyConnector_idNodeMap.get(startnoderef)==null){
					bundle.addAttribute("startnoderef", node.attributeValue("id"));
				}
				
				if(hasPropertyConnector_idNodeMap.get(endnoderef)==null){
					bundle.addAttribute("endnoderef", node.attributeValue("id"));
				}
			}
			
			//把topobundle中不和设备关联的toponode勇创建的toponode节点替换。
			List<Element> topoBundleSet = virtualharness.element("topoharness").elements("topobundle");
			
			for (Element topoBundle : topoBundleSet) {
				List<Element> toposegmentList = topoBundle.elements("toposegment");
				for (Element toposegment : toposegmentList) {
					String start = toposegment.attributeValue("start");
					String end = toposegment.attributeValue("end");
					if(toponodeShouldHoldMap.get(start)==null&&toponodeShouldHoldMap.get(end)!=null){
						toposegment.addAttribute("start",toponode.attributeValue("id"));
					}
					if(toponodeShouldHoldMap.get(end)==null&&toponodeShouldHoldMap.get(start)!=null){
						toposegment.addAttribute("end",toponode.attributeValue("id"));
					}
				}
			}
			
			
			// //
			// 判断每个virtualharness中的phd下的每个bundle，如果每个bundle下的startnoderef和endnoderef所指向的node都不是包含属性connector_id，则考虑把该bundle删除。
			// Map<String, Element> bundleShouldDeleteMap = new HashMap<String,
			// Element>();
			// List<Element> bundleList =
			// virtualharness.element("phd").elements(
			// "bundle");
			//
			// for (Element bundle : bundleList) {
			// String startnoderef = bundle.attributeValue("startnoderef");
			// String endnoderef = bundle.attributeValue("endnoderef");
			// System.out.println(startnoderef + " $ " + endnoderef);
			// if (hasPropertyConnector_idNodeMap.get(startnoderef) == null
			// && hasPropertyConnector_idNodeMap.get(endnoderef) == null) {
			// bundleShouldDeleteMap.put(bundle.attributeValue("id"),
			// bundle);
			//
			// }else{
			// if(notHasPropertyConnector_idNodeMap.get(startnoderef)!=null){
			// notHasPropertyConnector_idNodeMap.remove(startnoderef);
			// }
			//
			// if(notHasPropertyConnector_idNodeMap.get(endnoderef)!=null){
			// notHasPropertyConnector_idNodeMap.remove(endnoderef);
			// }
			// }
			// }
			//
			// //
			// 把notHasPropertyConnector_idNodeMap和bundleShouldDeleteMap涉及的node和bundle删除，同时删除topoharness下的toponode和topobundle节点中的connref属性和已删除的node和bundle的id相同的节点。
			// Map<String,Element> toponodeShouldDeleteMap = new
			// HashMap<String,Element>();
			// List<Element> toponodeList =
			// virtualharness.element("topoharness")
			// .elements("toponode");
			// for (Element toponode : toponodeList) {
			// String connref = toponode.attributeValue("connref");
			// if(notHasPropertyConnector_idNodeMap.get(connref)!=null){
			// toponodeShouldDeleteMap.put(toponode.attributeValue("id"),
			// toponode);
			// }
			// }
			//
			// Map<String,Element> topobundleShouldDeleteMap = new
			// HashMap<String,Element>();
			// List<Element> topobundleList =
			// virtualharness.element("topoharness")
			// .elements("topobundle");
			// for (Element topobundle : topobundleList) {
			// String connref = topobundle.attributeValue("connref");
			// if(bundleShouldDeleteMap.get(connref)!=null){
			// topobundleShouldDeleteMap.put(topobundle.attributeValue("id"),
			// topobundle);
			// }
			// }
			//
			//
			// //对需要删除的node、bundle、toponode、topobundle。进行删除。
			// System.out.println("---------------------------------");
			// System.out.println("bundleList.size():"+bundleList.size());
			// System.out.println("hasPropertyConnector_idNodeMap.size():"+hasPropertyConnector_idNodeMap.size());
			// System.out.println("notHasPropertyConnector_idNodeMap.size():"+notHasPropertyConnector_idNodeMap.size());
			// System.out.println("bundleShouldDeleteMap.size():"+bundleShouldDeleteMap.size());
			// System.out.println("toponodeShouldDeleteMap.size():"+toponodeShouldDeleteMap.size());
			// System.out.println("topobundleShouldDeleteMap.size():"+topobundleShouldDeleteMap.size());
			// System.out.println("---------------------------------");
			// Element phd = virtualharness.element("phd");
			// for (Element node : notHasPropertyConnector_idNodeMap.values()) {
			// phd.remove(node);
			// }
			//
			// for (Element bundle : bundleShouldDeleteMap.values()) {
			// phd.remove(bundle);
			// }
			//
			// Element topoharness = virtualharness.element("topoharness");
			// for (Element toponode : toponodeShouldDeleteMap.values()) {
			// topoharness.remove(toponode);
			// }
			//
			// for (Element topobundle : topobundleShouldDeleteMap.values()) {
			// topoharness.remove(topobundle);
			// }

			// List<Element> toponodeList =
			// virtualharness.element("topoharness")
			// .elements("toponode");
			// String node1X;
			// String node2X;
			// System.out.println("toponodeList.size()>>:" +
			// toponodeList.size());
			// if (toponodeList.size() == 2) {
			// count1++;
			// node1X = toponodeList.get(0).attribute("x").getValue();
			// node2X = toponodeList.get(1).attribute("x").getValue();
			//
			// float midX = (Float.parseFloat(node1X) + Float
			// .parseFloat(node2X)) / 2;
			//
			// String node1Y = toponodeList.get(0).attribute("y").getValue();
			// String node2Y = toponodeList.get(1).attribute("y").getValue();
			//
			// float midY = (Float.parseFloat(node1Y) + Float
			// .parseFloat(node2Y)) / 2;
			//
			// System.out.println(midX + "::" + midY);
			// // 3、添加布线信息:添加topojoint和toposegment
			// Element topobundle = virtualharness.element("topoharness")
			// .element("topobundle");
			// Element toposegment = topobundle.element("toposegment");
			// String arrtibuteref = toposegment
			// .attributeValue("attributeref");
			// if (topobundle.elements("topojoint").size() != 2) {
			// count++;
			// Element topojoint1 = topobundle.addElement("topojoint");
			// topojoint1.addAttribute("id", "_" + (++idMax));
			// topojoint1.addAttribute("x", Float.toString(midX));
			// topojoint1.addAttribute("y", node1Y);
			//
			// Element topojoint2 = topobundle.addElement("topojoint");
			// topojoint2.addAttribute("id", "_" + (++idMax));
			// topojoint2.addAttribute("x", Float.toString(midX));
			// topojoint2.addAttribute("y", node2Y);
			//
			// Element toposegment1 = topobundle.addElement("toposegment");
			// toposegment1.addAttribute("id", "_" + (++idMax));
			// toposegment1.addAttribute("start", toponodeList.get(0)
			// .attributeValue("id"));
			// toposegment1.addAttribute("end",
			// topojoint1.attributeValue("id"));
			// toposegment1.addAttribute("baseid", uid1 + time);
			// toposegment1.addAttribute("attributeref", arrtibuteref);
			//
			// Element toposegment2 = topobundle.addElement("toposegment");
			// toposegment2.addAttribute("id", "_" + (++idMax));
			// toposegment2.addAttribute("start",
			// topojoint1.attributeValue("id"));
			// toposegment2.addAttribute("end",
			// topojoint2.attributeValue("id"));
			// toposegment2.addAttribute("baseid", uid2 + time);
			// toposegment2.addAttribute("attributeref", arrtibuteref);
			//
			// toposegment.addAttribute("start",
			// topojoint2.attributeValue("id"));
			// toposegment.addAttribute("end", toponodeList.get(1)
			// .attributeValue("id"));
			//
			// }
			//
			// }

		}
		System.out.println("count1:" + count1);
		System.out.println("count:" + count);

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
				"D:/eclipse_wks/IDS_Test/20180411/tw_ids_integrator_4.xml"));
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
				.read("D:/eclipse_wks/IDS_Test/20180411/tw_ids_integrator_3.xml");
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
