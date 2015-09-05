/*
 *  This file is part of the Haven & Hearth game client.
 *  Copyright (C) 2009 Fredrik Tolf <fredrik@dolda2000.com>, and
 *                     Björn Johannessen <johannessen.bjorn@gmail.com>
 *
 *  Redistribution and/or modification of this file is subject to the
 *  terms of the GNU Lesser General Public License, version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Other parts of this source tree adhere to other copying
 *  rights. Please see the file `COPYING' in the root directory of the
 *  source tree for details.
 *
 *  A copy the GNU Lesser General Public License is distributed along
 *  with the source tree of which this file is a part in the file
 *  `doc/LPGL-3'. If it is missing for any reason, please see the Free
 *  Software Foundation's website at <http://www.fsf.org/>, or write
 *  to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *  Boston, MA 02111-1307 USA
 */
package haven;

import java.io.*;
import java.net.URL;
import java.util.Properties;

import static haven.Utils.getprop;

public class Config {

	public static final File HOMEDIR = new File(".").getAbsoluteFile();
	public static String authuser = getprop("haven.authuser", null);
	public static String authserv = getprop("haven.authserv", null);
	public static String defserv = getprop("haven.defserv", "127.0.0.1");
	public static URL resurl = geturl("haven.resurl", "");
	public static URL mapurl = geturl("haven.mapurl", "");
	public static boolean dbtext = getprop("haven.dbtext", "off").equals("on");
	public static boolean bounddb = getprop("haven.bounddb", "off").equals("on");
	public static boolean profile = getprop("haven.profile", "off").equals("on");
	public static boolean profilegpu = getprop("haven.profilegpu", "off").equals("on");
	public static boolean fscache = getprop("haven.fscache", "on").equals("on");
	public static String resdir = getprop("haven.resdir", null);
	public static boolean nopreload = getprop("haven.nopreload", "no").equals("yes");
	public static String loadwaited = getprop("haven.loadwaited", null);
	public static String allused = getprop("haven.allused", null);
	public static int mainport = getint("haven.mainport", 1870);
	public static int authport = getint("haven.authport", 1871);
	public static boolean softres = getprop("haven.softres", "on").equals("on");
	public static byte[] authck = null;
	public static String prefspec = "hafen";

	public static String version;

	static {
		String p;
		if ((p = getprop("haven.authck", null)) != null) {
			authck = Utils.hex2byte(p);
		}

		loadBuildVersion();
	}

	private static void loadBuildVersion() {
		InputStream in = Config.class.getResourceAsStream("/buildinfo");
		try {
			try {
				if (in != null) {
					Properties info = new Properties();
					info.load(in);
					version = info.getProperty("version");
				}
			} finally {
				if (in != null) {
					in.close();
				}
			}
		} catch (IOException e) {
			throw (new Error(e));
		}
	}

	public static File getFile(String name) {
		return new File(HOMEDIR, name);
	}

	public static String loadFile(String name) {
		InputStream inputStream = null;
		File file = Config.getFile(name);
		if (file.exists() && file.canRead()) {
			try {
				inputStream = new FileInputStream(file);
			} catch (FileNotFoundException ignored) {
			}
		} else {
			inputStream = Config.class.getResourceAsStream("/" + name);
		}
		if (inputStream != null) {
			try {
				return Utils.stream2str(inputStream);
			} catch (Exception ignore) {
			} finally {
				try {
					inputStream.close();
				} catch (IOException ignored) {
				}
			}
		}
		return null;
	}

	public static void saveFile(String name, String data) {
		File file = Config.getFile(name);
		boolean exists = file.exists();
		if (!exists) {
			try {
				//noinspection ResultOfMethodCallIgnored
				String parent = file.getParent();
				new File(parent).mkdirs();
				exists = file.createNewFile();
			} catch (IOException ignored) {
			}
		}
		if (exists && file.canWrite()) {
			PrintWriter out = null;
			try {
				out = new PrintWriter(file);
				out.print(data);
			} catch (FileNotFoundException ignored) {
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}
	}

	private static int getint(String name, int def) {
		String val = getprop(name, null);
		if (val == null) {
			return (def);
		}
		return (Integer.parseInt(val));
	}

	private static URL geturl(String name, String def) {
		String val = getprop(name, def);
		if (val.equals("")) {
			return (null);
		}
		try {
			return (new URL(val));
		} catch (java.net.MalformedURLException e) {
			throw (new RuntimeException(e));
		}
	}

	private static void usage(PrintStream out) {
		out.println("usage: haven.jar [OPTIONS] [SERVER[:PORT]]");
		out.println("Options include:");
		out.println("  -h                 Display this help");
		out.println("  -d                 Display debug text");
		out.println("  -P                 Enable profiling");
		out.println("  -G                 Enable GPU profiling");
		out.println("  -U URL             Use specified external resource URL");
		out.println("  -r DIR             Use specified resource directory (or HAVEN_RESDIR)");
		out.println("  -A AUTHSERV[:PORT] Use specified authentication server");
		out.println("  -u USER            Authenticate as USER (together with -C)");
		out.println("  -C HEXCOOKIE       Authenticate with specified hex-encoded cookie");
	}

	public static void cmdline(String[] args) {
		PosixArgs opt = PosixArgs.getopt(args, "hdPGU:r:A:u:C:");
		if (opt == null) {
			usage(System.err);
			System.exit(1);
		}
		for (char c : opt.parsed()) {
			switch (c) {
				case 'h':
					usage(System.out);
					System.exit(0);
					break;
				case 'd':
					dbtext = true;
					break;
				case 'P':
					profile = true;
					break;
				case 'G':
					profilegpu = true;
					break;
				case 'r':
					resdir = opt.arg;
					break;
				case 'A':
					int p = opt.arg.indexOf(':');
					if (p >= 0) {
						authserv = opt.arg.substring(0, p);
						authport = Integer.parseInt(opt.arg.substring(p + 1));
					} else {
						authserv = opt.arg;
					}
					break;
				case 'U':
					try {
						resurl = new URL(opt.arg);
					} catch (java.net.MalformedURLException e) {
						System.err.println(e);
						System.exit(1);
					}
					break;
				case 'u':
					authuser = opt.arg;
					break;
				case 'C':
					authck = Utils.hex2byte(opt.arg);
					break;
			}
		}
		if (opt.rest.length > 0) {
			int p = opt.rest[0].indexOf(':');
			if (p >= 0) {
				defserv = opt.rest[0].substring(0, p);
				mainport = Integer.parseInt(opt.rest[0].substring(p + 1));
			} else {
				defserv = opt.rest[0];
			}
		}
	}

	static {
		Console.setscmd("stats", new Console.Command() {
			public void run(Console cons, String[] args) {
				dbtext = Utils.parsebool(args[1]);
			}
		});
		Console.setscmd("profile", new Console.Command() {
			public void run(Console cons, String[] args) {
				if (args[1].equals("none") || args[1].equals("off")) {
					profile = profilegpu = false;
				} else if (args[1].equals("cpu")) {
					profile = true;
				} else if (args[1].equals("gpu")) {
					profilegpu = true;
				} else if (args[1].equals("all")) {
					profile = profilegpu = true;
				}
			}
		});
	}
}
