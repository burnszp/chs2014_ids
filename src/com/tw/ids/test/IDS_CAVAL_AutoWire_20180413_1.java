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
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.plaf.basic.BasicScrollPaneUI.HSBChangeListener;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.tw.ids.model.Bundle;

/**
 * 方法1、对每个harness按照距离最近的两个设备先连接，然后从连接导线选择一个中间点，然后把这个中间点看作设备和其他设备再比较，
 * 找出最近距离的两个设备连接，以此类推
 * 方法2、对每个harness按照距离最近的两个设备先连接，然后对这条连接线的延长线上其他设备对该直线做垂线，如果垂点做在连接线的延长线上
 * ，则垂点靠近哪个设备，就把其他设备距离该设备最短的设备和垂点靠近的设备相连。
 * 方法3、对每个harness按照两两最近设备连接，然后对每个连线找到中间点，在对中间点两辆最近连接，一次类推。
 * 
 * 该类采用第三种方法。
 * 
 * @ClassName: IDS_CAVAL_AutoWire
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author Burns[张沛]
 * @date 2018-3-29 下午2:27:46
 * 
 */
public class IDS_CAVAL_AutoWire_20180413_1 {
	static String uid1 = "UID822c59-1626649577f-1ed30ffe71c1334";
	static String uid2 = "UID822c59-1626649577f-1ed30ffe71c1335";
	static String uid3 = "UID822c59-1626649577f-1ed30ffe71c1336";
	static String uid4 = "UID822c59-1626649577f-1ed30ffe71c1337";

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

		List<Element> virtualharnessList = topoplane.elements("virtualharness");
		System.out.println("virtualharnessList.size():"
				+ virtualharnessList.size());
		int count1 = 0;
		int count = 0;

		Map<String, List<Element>> bundleMap = new HashMap<String, List<Element>>();
		Map<String, List<Element>> nodeMap = new HashMap<String, List<Element>>();

		for (int i = 0; i < virtualharnessList.size(); i++) {

			Element virtualharness = (Element) virtualharnessList.get(i);

			Element phd = virtualharness.element("phd");
			Element topoharness = virtualharness.element("topoharness");
			List<Element> bundleList = phd.elements("bundle");
			List<Element> topobundleList = topoharness.elements("topobundle");
			List<Element> segmentList = topobundleList.get(0).elements(
					"toposegment");
			String attributeref = segmentList.get(0).attributeValue(
					"attributeref");

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

			// 根据hasPropertyConnector_idNodeMap找到对应的toponode的每个坐标，（toponode的connref属性和node的id相同）每个坐标等价于设备上的连接器，
			List<Element> toponodeList = topoharness.elements("toponode");
			Map<String, String> nodeAndToponodeMap = new HashMap<String, String>();
			List<Element> toponodeLinkDeviceList = new ArrayList<Element>();
			List<Element> toponodeNotLinkDeviceList = new ArrayList<Element>();
			for (Element toponode : toponodeList) {
				String connref = toponode.attributeValue("connref");
				Element node = hasPropertyConnector_idNodeMap.get(connref);
				if (node != null) {
					toponodeLinkDeviceList.add(toponode);
					nodeAndToponodeMap.put(toponode.attributeValue("id"),
							node.attributeValue("id"));
				} else {
					toponodeNotLinkDeviceList.add(toponode);
				}
			}

			// 删除所有的bundle和toponode节点，同时删除node和toponode中不和设备相连的部分。
			for (Element topobundle : topobundleList) {
				topoharness.remove(topobundle);
			}

			for (Element bundle : bundleList) {
				phd.remove(bundle);
			}

			for (Element toponode : toponodeNotLinkDeviceList) {
				topoharness.remove(toponode);

			}

			for (Element node : notHasPropertyConnector_idNodeMap.values()) {
				phd.remove(node);
			}

			if (toponodeLinkDeviceList.size() > 1) {

				handleIntegrator(virtualharness, idMax, attributeref,
						nodeAndToponodeMap, toponodeLinkDeviceList);
			}

			// 根据bundleLineMap中的bundle连接关系，创建bundle和对应的topobundle,找到每个bundle的中间点。得到重点点的坐标，并生成node点和toponode，把该node点作为下次两两node点连接的其中一个点。

			// 1、生成bundle。

			// //
			// 根据hasPropertyConnector_idNodeMap中的node的坐标算出一个平均值坐标，作为一个harenss的中心坐标的node坐标，然后把notHasPropertyConnector_idNodeMap中的node都替换成中心坐标的node。
			// Map<String, Element> toponodeShouldHoldMap = new HashMap<String,
			// Element>();
			// List<Element> toponodeList =
			// virtualharness.element("topoharness")
			// .elements("toponode");
			// for (Element toponode : toponodeList) {
			// String connref = toponode.attributeValue("connref");
			// if (hasPropertyConnector_idNodeMap.get(connref) != null) {
			// toponodeShouldHoldMap.put(toponode.attributeValue("id"),
			// toponode);
			// }
			// }
			//
			// double sumX = 0;
			// double sumY = 0;
			// String attributeref = null;
			// for (Element toponode : toponodeShouldHoldMap.values()) {
			// sumX = sumX + Double.parseDouble(toponode.attributeValue("x"));
			// sumY = sumY + Double.parseDouble(toponode.attributeValue("y"));
			// attributeref = toponode.attributeValue("attributeref");
			// }
			//
			// //先删除bundle，保证添加的node在bundle之前
			// Element phd = virtualharness.element("phd");
			// List<Element> bundleListTemp = phd.elements("bundle");
			//
			// for (Element bundleTemp : bundleListTemp) {
			// phd.remove(bundleTemp);
			// }
			//
			// //创建node节点。
			// Element node = phd.addElement("node");
			// node.addAttribute("id", "_"+String.valueOf(idMax++));
			// node.addAttribute("name", "_N_"+idMax);
			// node.addAttribute("nameindex", ""+idMax);
			// node.addAttribute("baseid", uid1+time);
			// node.addAttribute("isreference", "0");
			// node.addAttribute("ismarker", "0");
			// node.addAttribute("node", "_N_"+idMax);
			// node.addAttribute("pokehome", "false");
			//
			// //把删除的bundle添加上。
			// for (Element bundleTemp : bundleListTemp) {
			// phd.add(bundleTemp);
			// }
			//
			//
			// Element topoharness = virtualharness.element("topoharness");
			// List<Element> topobundleListTemp =
			// topoharness.elements("topobundle");
			// for (Element topobundleTemp : topobundleListTemp) {
			// topoharness.remove(topobundleTemp);
			// }
			//
			//
			//
			// //创建toponode节点
			// Element toponode = topoharness.addElement("toponode");
			// toponode.addAttribute("id", "_"+String.valueOf(idMax++));
			// toponode.addAttribute("connref", node.attributeValue("id"));
			// toponode.addAttribute("x",
			// String.valueOf(sumX/toponodeShouldHoldMap.size()));
			// toponode.addAttribute("y",
			// String.valueOf(sumY/toponodeShouldHoldMap.size()));
			// toponode.addAttribute("attributeref", attributeref);
			// // toponode.addAttribute("ismarker", "0");
			// toponode.addAttribute("isexcludefromapplystyle", "false");
			// toponode.addAttribute("visibility", "true");
			//
			// for (Element topobundleTemp : topobundleListTemp) {
			// topoharness.add(topobundleTemp);
			// }
			//
			// //把bundle中不和设备关联的node用创建的node节点替换。
			// List<Element> bundleList =
			// virtualharness.element("phd").elements("bundle");
			// for (Element bundle : bundleList) {
			// String startnoderef = bundle.attributeValue("startnoderef");
			// String endnoderef = bundle.attributeValue("endnoderef");
			// if(hasPropertyConnector_idNodeMap.get(startnoderef)==null){
			// bundle.addAttribute("startnoderef", node.attributeValue("id"));
			// }
			//
			// if(hasPropertyConnector_idNodeMap.get(endnoderef)==null){
			// bundle.addAttribute("endnoderef", node.attributeValue("id"));
			// }
			// }
			//
			// //把topobundle中不和设备关联的toponode勇创建的toponode节点替换。
			// List<Element> topoBundleSet =
			// virtualharness.element("topoharness").elements("topobundle");
			//
			// for (Element topoBundle : topoBundleSet) {
			// List<Element> toposegmentList =
			// topoBundle.elements("toposegment");
			// for (Element toposegment : toposegmentList) {
			// String start = toposegment.attributeValue("start");
			// String end = toposegment.attributeValue("end");
			// if(toponodeShouldHoldMap.get(start)==null&&toponodeShouldHoldMap.get(end)!=null){
			// toposegment.addAttribute("start",toponode.attributeValue("id"));
			// }
			// if(toponodeShouldHoldMap.get(end)==null&&toponodeShouldHoldMap.get(start)!=null){
			// toposegment.addAttribute("end",toponode.attributeValue("id"));
			// }
			// }
			// }
			//

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
	 * @param nodeAndToponodeMap
	 * @param attributeref
	 * @param virtualharness
	 *            把toponode的点用topobundle连接起来，按照两两最近距离链接，然后依次最远距离连接。
	 * @Title: handleIntegrator
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @param toponodeLinkDeviceList 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	private static void handleIntegrator(Element virtualharness, int idMax,
			String attributeref, Map<String, String> nodeAndToponodeMap,
			List<Element> toponodeLinkDeviceList) {

		Element phd = virtualharness.element("phd");
		Element topoharness = virtualharness.element("topoharness");
		List<Element> bundleList = phd.elements("bundle");
		List<Element> topobundleList = topoharness.elements("topobundle");

		Map<String, List<String>> bundleLineMap = new HashMap<String, List<String>>();
		List<String> deleteToponodeIdList = new ArrayList<String>();
		System.out.println("nodeAndToponodeMap.size()"+nodeAndToponodeMap.size());
		System.out.println("toponodeLinkDeviceList.size():"
				+ toponodeLinkDeviceList.size());

		Map<String, Element> toponodeMap = new HashMap<String, Element>();
		// 根据每个和设备连接的toponode的坐标，得到两两一组最短的连接关系。
		for (Element toponode : toponodeLinkDeviceList) {
			String id = toponode.attributeValue("id");
			for (Element toponode1 : toponodeLinkDeviceList) {
				String id1 = toponode1.attributeValue("id");
				if (id != id1) {
					// 计算两个toponode的长度。
					double toponodex = Double.parseDouble(toponode
							.attributeValue("x"));
					double toponodey = Double.parseDouble(toponode
							.attributeValue("y"));
					double toponode1x = Double.parseDouble(toponode1
							.attributeValue("x"));
					double toponode1y = Double.parseDouble(toponode1
							.attributeValue("y"));
					double result = Math.sqrt((toponode1y - toponodey)
							* (toponode1y - toponodey)
							+ (toponode1x - toponodex)
							* (toponode1x - toponodex));
					// double result = (toponode1y - toponodey)
					// * (toponode1y - toponodey)
					// + (toponode1x - toponodex)
					// * (toponode1x - toponodex);
					// List<List<String>> toponodeIdListList = new
					// ArrayList<List<String>>();
					List<String> toponodeIdList = new ArrayList<String>();
					deleteToponodeIdList.add(id);
					deleteToponodeIdList.add(id1);

					toponodeIdList.add(id);
					toponodeIdList.add(id1);
					toponodeMap.put(id, toponode);
					toponodeMap.put(id1, toponode1);

					// toponodeIdListList.add(toponodeIdList);

					// toponodeLinkDeviceList.remove(toponode);
					// toponodeLinkDeviceList.remove(toponode1);
					System.out.println("result:" + result);
					List<String> toponodeIdListTemp = bundleLineMap.get(Double
							.toString(result));
					if (toponodeIdListTemp == null) {
						bundleLineMap.put(Double.toString(result),
								toponodeIdList);
					} else {
						// 如果有其他坐标连接长度和此两个坐标连接长度相同。
						System.out.println("有三个及以上坐标连接长度相同，请处理！");
						// if(toponodeIdListTemp.containsAll(toponodeIdList)){
						// }else{
						// bundleLineMap.put(Double.toString(result),
						// toponodeIdList);
						// }
					}
				}
			}
		}

		System.out.println("bundleLineMap.size():" + bundleLineMap.size());
		// for (String result : bundleLineMap.keySet()) {
		// System.out.print(result + "~~");
		// List<String> idList1 = bundleLineMap.get(result);
		// for (String toponodeId : idList1) {
		// System.out.print(toponodeId + "@");
		//
		// }
		// System.out.println("_________________________");
		// }

		// 按照bundleLineMap中的key由小到大开始创建bundle和topobundle.每创建一个bundle和对应的topobundle，就要生成对应的bundle中间位置左边的node和，及和node对应的toponode，从集合删除bundle两端的node对应的toponode，并把node坐标放到下次循环中

		// 对bundleLineMap的key进行排序，去掉其value中有部分坐标重合的id

		String[] bundleLengthArr = new String[bundleLineMap.size()];
		int i1 = 0;
		for (String bundleLength : bundleLineMap.keySet()) {
			bundleLengthArr[i1] = bundleLength;
			i1++;
		}

		for (int j = 0; j < bundleLengthArr.length; j++) {
			for (int k = j; k < bundleLengthArr.length - 1; k++) {
				double dk = Double.parseDouble(bundleLengthArr[k]);
				double dk1 = Double.parseDouble(bundleLengthArr[k + 1]);
				if (dk > dk1) {
					String temp = bundleLengthArr[k];
					bundleLengthArr[k] = bundleLengthArr[k + 1];
					bundleLengthArr[k + 1] = temp;
				}
			}

		}

		List<String> shouldDeleteBundleLengthList = new ArrayList<String>();
		List<String> toponodeIdList = new ArrayList<String>();
		for (String bundleLength : bundleLengthArr) {
			System.out.print(bundleLength + "*");
			List<String> nodeIdListTemp = bundleLineMap.get(bundleLength);
			if (nodeIdListTemp.size() == 2) {
				if (!toponodeIdList.contains(nodeIdListTemp.get(0))
						&& !toponodeIdList.contains(nodeIdListTemp.get(1))) {
					toponodeIdList.add(nodeIdListTemp.get(0));
					toponodeIdList.add(nodeIdListTemp.get(1));
				} else {
					shouldDeleteBundleLengthList.add(bundleLength);
				}
			}

		}

		System.out.println("shouldDeleteBundleLengthList.size()"
				+ shouldDeleteBundleLengthList.size());

		Iterator<Entry<String, List<String>>> it = bundleLineMap.entrySet()
				.iterator();
		while (it.hasNext()) {
			Entry<String, List<String>> entry = it.next();
			if (shouldDeleteBundleLengthList.contains(entry.getKey())) {
				it.remove();
			}
		}
//		for (String bundleLength : bundleLineMap.keySet()) {
//			if (shouldDeleteBundleLengthList.contains(bundleLength)) {
//				bundleLineMap.remove(bundleLength);
//			}
//		}

		// 记录两两组合成bundle的toponode，后面为删除集合中的这两个点做准备。
		List<Element> shouldDeleteToponode = new ArrayList<Element>();
		for (Element toponode : toponodeLinkDeviceList) {
			String toponodeId = toponode.attributeValue("id");
			if (toponodeIdList.contains(toponodeId)) {
				shouldDeleteToponode.add(toponode);
			}
		}

		// 过滤bundleLineMap中重合

		// 循环bundleLineMap集合，

		System.out.println("-------bundleLineMap.size()" + bundleLineMap.size());

		for (String result : bundleLineMap.keySet()) {
			String time = new SimpleDateFormat("yyyyMMddHHmmssSSS")
					.format(new Date());
			List<String> toponodeIdList1 = bundleLineMap.get(result);

			if (toponodeIdList1.size() == 2) {
				// 1、生成bundle
				Element bundle = phd.addElement("bundle");
				bundle.addAttribute("id", "_" + String.valueOf(++idMax));
				bundle.addAttribute("name", "BUN" + idMax);
				bundle.addAttribute("nameindex", "" + idMax);
				bundle.addAttribute("startnoderef", nodeAndToponodeMap.get(toponodeIdList1.get(0)));
				bundle.addAttribute("endnoderef", nodeAndToponodeMap.get(toponodeIdList1.get(1)));
				bundle.addAttribute("length",
						Double.toString(Math.sqrt(Double.parseDouble(result))));
				bundle.addAttribute("grang", "0.0");
				bundle.addAttribute("variantindex", "0");
				bundle.addAttribute("baseid", uid1 + time);
				bundle.addAttribute("parentid", "_" + String.valueOf(++idMax));

				// 2、生成topobundle,其中topobundle下还有segment标签集合（如果topobundle是个折线，那么这个segment就是标签集合）
				Element topobundle = topoharness.addElement("topobundle");
				topobundle.addAttribute("id", "_" + String.valueOf(idMax++));
				topobundle.addAttribute("connref", bundle.attributeValue("id"));
				topobundle.addAttribute("isexcludefromapplystyle", "false");

				Element segment = topobundle.addElement("toposegment");

				segment.addAttribute("id", "_" + String.valueOf(++idMax));
				segment.addAttribute("start",toponodeIdList1.get(0) );
				segment.addAttribute("end",toponodeIdList1.get(1));
				segment.addAttribute("baseid", uid2 + time);
				segment.addAttribute("parentid", "_" + String.valueOf(++idMax));
				segment.addAttribute("attributeref", attributeref);
				/**
				 * 3、创建node节点。在创建之前先删除bundle，在添加node，最后再增加bundle，
				 * 保证node在bundle先创建
				 */

				// 3.1、先删除bundle，保证添加的node在bundle之前
				List<Element> bundleListTemp = phd.elements("bundle");

				for (Element bundleTemp : bundleListTemp) {
					phd.remove(bundleTemp);
				}
				// 3.2、创建node节点
				Element node = phd.addElement("node");
				node.addAttribute("id", "_" + String.valueOf(++idMax));
				node.addAttribute("name", "_N_" + idMax);
				node.addAttribute("nameindex", "" + idMax);
				node.addAttribute("baseid", uid3 + time);
				node.addAttribute("isreference", "0");
				node.addAttribute("ismarker", "0");
				node.addAttribute("node", "_N_" + idMax);
				node.addAttribute("pokehome", "false");

				// 3.3、把删除的bundle添加上。
				for (Element bundleTemp : bundleListTemp) {
					phd.add(bundleTemp);
				}

				/**
				 * 4、创建toponode节点，创建之前先删除topobundle，在创建toponode， 在田间topobundle。
				 */
				// 4.1、删除topobundle
				List<Element> topobundleListTemp = topoharness
						.elements("topobundle");
				for (Element topobundleTemp : topobundleListTemp) {
					topoharness.remove(topobundleTemp);
				}

				// 4.2、 创建toponode节点
				double node1x = Double.parseDouble(toponodeMap.get(
						toponodeIdList1.get(0)).attributeValue("x"));
				double node2x = Double.parseDouble(toponodeMap.get(
						toponodeIdList1.get(1)).attributeValue("x"));
				String toponodeX = Double.toString((node1x + node2x) / 2);

				double node1y = Double.parseDouble(toponodeMap.get(
						toponodeIdList1.get(0)).attributeValue("y"));
				double node2y = Double.parseDouble(toponodeMap.get(
						toponodeIdList1.get(1)).attributeValue("y"));
				String toponodeY = Double.toString((node1y + node2y) / 2);

				Element toponode = topoharness.addElement("toponode");
				toponode.addAttribute("id", "_" + String.valueOf(++idMax));
				toponode.addAttribute("connref", node.attributeValue("id"));
				toponode.addAttribute("x", toponodeX);
				toponode.addAttribute("y", toponodeY);
				toponode.addAttribute("attributeref", attributeref);
				// toponode.addAttribute("ismarker", "0");
				toponode.addAttribute("isexcludefromapplystyle", "false");
				toponode.addAttribute("visibility", "true");

				// 4.3、
				for (Element topobundleTemp : topobundleListTemp) {
					topoharness.add(topobundleTemp);
				}
				
				nodeAndToponodeMap.put(toponode.attributeValue("id"), node.attributeValue("id"));

				toponodeLinkDeviceList.add(toponode);

			}

		}

		System.out.println("-------------------");
		System.out.println("~~toponodeLinkDeviceList.size():"
				+ toponodeLinkDeviceList.size());
		System.out.println("shouldDeleteToponode.size():"
				+ shouldDeleteToponode.size());
		toponodeLinkDeviceList.removeAll(shouldDeleteToponode);
		System.out.println("toponodeLinkDeviceList.size():"
				+ toponodeLinkDeviceList.size());
		System.out.println("-------------------");

		if (toponodeLinkDeviceList.size() > 1) {
			handleIntegrator(virtualharness, idMax, attributeref,
					nodeAndToponodeMap, toponodeLinkDeviceList);
		}
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
				"D:/eclipse_wks/IDS_Test/20180417/ids_tw_integrator_2.xml"));
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
				.read("D:/eclipse_wks/IDS_Test/20180417/ids_tw_integrator_1.xml");
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
