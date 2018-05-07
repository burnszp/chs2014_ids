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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class DOM4j {

	public static void main(String[] args)throws Exception {
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
        List<Book> books = readAllElementsFromXMLDocument(document);
        traverseBooks(books);

        /**
         * 其次，修改功能 修改内容：将id为b002的元素的title改为Java Core，Price改为100.01
         */
        ModifyInformationOfXMLDocument(document);

         /**
         * 再者：实现删除功能 删除内容：删除掉id为book1的元素内容。
         */
         deleteInformationOfXMLDocument(document);

         /**
         * 最后：实现添加i新元素功能 添加内容：id为book3，title内容为：凤姐玉照，price内容为10000.00
         */
         addNewBookToXMLDocument(document);

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
    private static void ModifyInformationOfXMLDocument(Document document) {
        Element root = document.getRootElement();
        List books = root.elements();
        for (int i = 0; i < books.size(); i++) {

            Element book = (Element) books.get(i);
            if ("book2".equals(book.attributeValue("id"))) {

                for (Iterator it = book.elementIterator(); it.hasNext();) {
                    Element node = (Element) it.next();
                    String type = node.getName();
                    if ("title".equals(type)) {
                        node.setText("JAVA Core");
                    }
                    if ("price".equals(type)) {
                        node.setText("100.01");
                    }
                }
            }
        }

        try {
            writeToNewXMLDocument(document);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 遍历集合
     * 
     * @param books
     */
    private static void traverseBooks(List<Book> books) {
        for (Iterator<Book> iterator = books.iterator(); iterator.hasNext();) {
            Book book = iterator.next();
            System.out.println(book);
        }
    }

    /**
     * 该方法实现了对xml文档的读取功能
     * 
     * @param document
     * @return
     */
    private static List<Book> readAllElementsFromXMLDocument(Document document) {
        List<Book> books = new ArrayList<Book>();
        Element root = document.getRootElement();
        List list = root.elements();
        for (int i = 0; i < list.size(); i++) {
            Element book = (Element) list.get(i);
            Book b = new Book();
            String id = book.attributeValue("id");
            List ll = book.elements();
            b.setId(id);
            System.out.println(id);
            for (int j = 0; j < ll.size(); j++) {
                Element element = (Element) ll.get(j);
                if ("title".equals(element.getName())) {
                    String title = element.getText();
                    b.setTitle(title);
                    System.out.println(title);
                }
                if ("price".equals(element.getName())) {
                    String price = element.getText();
                    double p = Double.parseDouble(price);
                    b.setPrice(p);
                    System.out.println(price);
                }
            }
            books.add(b);
        }
        return books;
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
                "src/com/zc/homeWork19/newbooks.xml"));
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
        Document document = sr.read("src\\books.xml");
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