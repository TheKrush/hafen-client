package haven;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class RadarCFG {

	private static final List<Group> groups = new LinkedList<>();

	static {
		String file = Config.loadFile("radar.json");
		if (file != null) {
			try {
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
				Document doc = builder.parse(file);
				groups.clear();
				NodeList groupNodes = doc.getElementsByTagName("group");
				for (int i = 0; i < groupNodes.getLength(); i++) {
					groups.add(new Group((Element) groupNodes.item(i)));
				}
			} catch (ParserConfigurationException | IOException | SAXException ignored) {
			}
		}
	}

	public static class Group {

		public Group(Element config) {
			config.hasAttribute("name");
		}
	}
}
