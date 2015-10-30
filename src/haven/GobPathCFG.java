package haven;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GobPathCFG {

	static final String XML_FILE = "gob_path.xml";
	private static String CONFIG_XML;

	public static final List<Group> groups = new LinkedList<>();
	private static DocumentBuilder builder;

	private static List<GobPathCFG.GobPathCFGObserver> observers;

	static {
		observers = new ArrayList<>();

		loadConfig();
	}

	public static synchronized void loadConfig() {
		loadConfig(true);
	}

	public static synchronized void loadConfig(boolean observe) {
		String configXml = Globals.SettingFileString(Globals.USERNAME + "/" + XML_FILE, true);
		try {
			// first check if we have username config
			if (Globals.USERNAME.isEmpty() || !xmlFromString(Config.loadFile(configXml))) {
				// now check for default config
				configXml = Globals.SettingFileString("/" + XML_FILE, true);
				if (!xmlFromString(configXml)) {
					// use the internal one
					xmlFromString(Config.loadFile(XML_FILE));
				}
			}
		} catch (Exception e) {
		}
		CONFIG_XML = configXml;
		System.out.println("Using setting file: " + CONFIG_XML);

		if (observe) {
			for (GobPathCFG.GobPathCFGObserver observer : observers) {
				observer.cfgUpdated();
			}
		}
	}

	public static synchronized void saveConfig() {
		saveConfig(true);
	}

	public static synchronized void saveConfig(boolean observe) {
		try {
			Config.saveFile(CONFIG_XML, xmlToString());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		if (observe) {
			for (GobPathCFG.GobPathCFGObserver observer : observers) {
				observer.cfgUpdated();
			}
		}
	}

	private static synchronized boolean xmlFromString(String xml) {
		if (xml != null && !xml.isEmpty()) {
			try {
				DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				builder = documentBuilderFactory.newDocumentBuilder();
				Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
				groups.clear();
				NodeList groupNodes = doc.getElementsByTagName("group");
				for (int i = 0; i < groupNodes.getLength(); i++) {
					groups.add(new Group((Element) groupNodes.item(i)));
				}
				return true;
			} catch (ParserConfigurationException | IOException | SAXException ignored) {
				ignored.printStackTrace();
			}
		}
		return false;
	}

	private static synchronized String xmlToString() {
		try {
			Document doc = builder.newDocument();

			// construct XML
			Element root = doc.createElement("paths");
			doc.appendChild(root);
			for (Group group : groups) {
				Element el = doc.createElement("group");
				group.write(el);
				root.appendChild(el);
			}

			// write XML
			OutputStream out = new ByteArrayOutputStream();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			DOMSource source = new DOMSource(doc);
			StreamResult console = new StreamResult(out);
			transformer.transform(source, console);
			return out.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public GobPathCFG() {
		observers = new ArrayList<>();
	}

	public static interface GobPathCFGObserver {

		void cfgUpdated();
	}

	public static synchronized void addObserver(GobPathCFGObserver observer) {
		if (observer == null) {
			return;
		}
		observers.add(observer);
	}

	public static synchronized void remObserver(GobPathCFGObserver observer) {
		if (observer == null) {
			return;
		}
		observers.remove(observer);
	}

	public static class Group {

		private final Color color;
		public String name;
		public Boolean show = null;
		public List<PathCFG> pathCFGs;

		public Group(Element config) {
			name = config.getAttribute("name");
			color = Utils.hex2color(config.getAttribute("color"), null);
			if (config.hasAttribute("show")) {
				show = config.getAttribute("show").toLowerCase().equals("true");
			}
			NodeList children = config.getElementsByTagName("path");
			pathCFGs = new LinkedList<>();
			for (int i = 0; i < children.getLength(); i++) {
				pathCFGs.add(PathCFG.parse((Element) children.item(i), this));
			}

		}

		public void write(Element el) {
			Document doc = el.getOwnerDocument();
			el.setAttribute("name", name);
			if (color != null) {
				el.setAttribute("color", Utils.color2hex(color));
			}
			if (show != null) {
				el.setAttribute("show", show.toString());
			}
			for (PathCFG marker : pathCFGs) {
				Element mel = doc.createElement("path");
				marker.write(mel);
				el.appendChild(mel);
			}
		}

		public Color color() {
			return color != null ? color : Color.WHITE;
		}
	}

	public static class PathCFG {

		public Group parent;
		private Match type;
		private String pattern;
		private Boolean show = null;
		public String name = null;
		private Color color;

		public static PathCFG parse(Element config, Group parent) {
			PathCFG cfg = new PathCFG();

			cfg.parent = parent;
			Match[] types = Match.values();
			String name = null;
			for (Match type : types) {
				name = type.name();
				if (config.hasAttribute(name)) {
					break;
				}
			}
			if (name == null) {
				throw new RuntimeException();
			}
			if (config.hasAttribute("name")) {
				cfg.name = config.getAttribute("name");
			}
			cfg.color = Utils.hex2color(config.getAttribute("color"), null);
			cfg.type = Match.valueOf(name);
			cfg.pattern = config.getAttribute(name);
			if (config.hasAttribute("show")) {
				cfg.show = config.getAttribute("show").toLowerCase().equals("true");
			}

			return cfg;
		}

		public boolean match(String target) {
			return type.match(pattern, target);
		}

		public boolean visible() {
			if (parent.show != null && !parent.show) {
				return false;
			} else if (show != null) {
				return show;
			} else {
				return true;
			}
		}

		public void write(Element el) {
			el.setAttribute(type.name(), pattern);
			if (color != null) {
				el.setAttribute("color", Utils.color2hex(color));
			}
			if (name != null) {
				el.setAttribute("name", name);
			}
			if (show != null) {
				el.setAttribute("show", show.toString());
			}
		}

		public Color color() {
			return color != null ? color : parent.color();
		}
	}

	public static class GroupCheck extends CheckBox {

		public final Group group;

		public GroupCheck(Group group) {
			super(group.name);
			this.group = group;
			this.hitbox = true;
			this.a = group.show == null || group.show;
		}

		@Override
		public void changed(boolean val) {
			group.show = val;
		}
	}

	public static class PathCheck extends CheckBox {

		public final PathCFG marker;

		public PathCheck(PathCFG marker) {
			super(marker.name != null ? marker.name : marker.pattern);
			this.marker = marker;
			this.a = marker.show == null || marker.show;
		}

		@Override
		public void changed(boolean val) {
			marker.show = val;
		}
	}

	enum Match {

		exact {
							@Override
							public boolean match(String pattern, String target) {
								return target.equals(pattern);
							}
						},
		regex {
							@Override
							public boolean match(String pattern, String target) {
								return target.matches(pattern);
							}
						},
		startsWith {
							@Override
							public boolean match(String pattern, String target) {
								return target.startsWith(pattern);
							}
						},
		contains {
							@Override
							public boolean match(String pattern, String target) {
								return target.contains(pattern);
							}
						};

		public abstract boolean match(String pattern, String target);
	}
}
