package org.ender.updater;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class UpdaterConfig {

	private static final String EXTRACT = "extract";
	private static final String ITEM = "item";
	private static final String ARCH = "arch";
	private static final String OS = "os";
	private static final String FILE = "file";
	private static final String LINK = "link";
	public String smem;
	public String mem;
	public String res;
	public String server;
	public String jar;
	public String errorFile;
	public static File dir = new File(".");

	List<Item> items = new ArrayList();

	public UpdaterConfig() {
		if (!dir.exists()) {
			dir.mkdirs();
		}

		InputStream stream = UpdaterConfig.class.getResourceAsStream("/config.xml");

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(stream);
			stream.close();

			NamedNodeMap attrs = doc.getDocumentElement().getAttributes();
			Node node;

			node = attrs.getNamedItem("smem");
			this.smem = (node != null ? node.getNodeValue() : "");

			node = attrs.getNamedItem("mem");
			this.mem = (node != null ? node.getNodeValue() : "");

			node = attrs.getNamedItem("res");
			this.res = (node != null ? node.getNodeValue() : "");

			node = attrs.getNamedItem("server");
			this.server = (node != null ? node.getNodeValue() : "");

			node = attrs.getNamedItem("jar");
			this.jar = (node != null ? node.getNodeValue() : "");

			node = attrs.getNamedItem("errorFile");
			this.errorFile = (node != null ? node.getNodeValue() : "");

			NodeList groupNodes = doc.getElementsByTagName("item");
			for (int i = 0; i < groupNodes.getLength(); i++) {
				Item itm = parseItem(groupNodes.item(i));
				if (itm != null) {
					this.items.add(itm);
				}
			}
		} catch (Exception e) {
		}
	}

	private Item parseItem(Node node) {
		Item itm = new Item();
		if (node.getNodeType() != 1) {
			return null;
		}
		Element el = (Element) node;

		itm.link = el.getAttribute("link");
		if (el.hasAttribute("file")) {
			itm.file = new File(dir, el.getAttribute("file"));
		} else {
			int i = itm.link.lastIndexOf("/");
			itm.file = new File(dir, itm.link.substring(i + 1));
		}
		itm.os = el.getAttribute("os");
		itm.arch = el.getAttribute("arch");
		String e = el.getAttribute("extract");
		if (e.length() > 0) {
			itm.extract = new File(dir, e);
		}
		return itm;
	}

	public static class Item {

		public String arch;
		public String os;
		public File file;
		public String link;
		public long date = 0L;
		public long size = 0L;
		public File extract = null;
	}
}
