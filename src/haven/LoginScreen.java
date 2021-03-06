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

import java.awt.event.KeyEvent;
import java.io.*;

public class LoginScreen extends Widget {

	Login cur;
	Text error;
	IButton btn;
	Button optbtn, clbtn;
	OptWnd opts;
	AccountList accounts;
	static Text.Foundry textf, textfs;
	static Tex bg = Resource.loadtex("gfx/loginscr");
	Text progress = null;
	private Window log;

	static {
		textf = new Text.Foundry(Text.sans, 16).aa(true);
		textfs = new Text.Foundry(Text.sans, 14).aa(true);
	}

	public LoginScreen() {
		super(bg.sz());
		setfocustab(true);
		add(new Img(bg), Coord.z);
		optbtn = adda(new Button(100, "Options"), 10, sz.y - 10, 0, 1);
		clbtn = adda(new Button(100, "Changelog") {

			@Override
			public void click() {
				showChangeLog();
			}
		}, sz.x - 10, 10, 1, 0);
		accounts = add(new AccountList(10));
	}

	private void showChangeLog() {
		if (log == null) {
			initChangeLog();
		}
		log.show();
	}

	private void initChangeLog() {
		log = ui.root.add(new Window(new Coord(50, 50), "Changelog") {

			@Override
			public void presize() {
				c = parent.sz.div(2).sub(sz.div(2));
			}
		});
		log.justclose = true;
		int width = sz.x - 150;
		int height = sz.y - 150;
		Textlog txt = log.add(new Textlog(new Coord(width, height), Textlog.fndMono));
		log.pack();

		Button btn = new Button(width, "Close") {

			@Override
			public void click() {
				log.hide();
			}
		};
		log.adda(btn, txt.c.x + (txt.sz.x / 2), txt.c.y + txt.sz.y + 5, 0.5, 0);
		log.pack();
		parent.pack();
		log.c = parent.sz.div(2).sub(sz.div(2));

		txt.quote = false;
		int maxlines = txt.maxLines = 200;
		try {
			InputStream in = LoginScreen.class.getResourceAsStream("/changelog.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			String strLine;
			int count = 0;
			while ((count < maxlines) && (strLine = br.readLine()) != null) {
				if ("".equals(strLine)) {
					strLine = " ";
				}
				txt.append(strLine);
				count++;
			}
			br.close();
			in.close();
		} catch (FileNotFoundException ignored) {
		} catch (IOException ignored) {
		}
		txt.setprog(0);

		//WikiBrowser.toggle();
	}

	private static abstract class Login extends Widget {

		abstract Object[] data();

		abstract boolean enter();
	}

	private class Pwbox extends Login {

		TextEntry user, pass;
		CheckBox savepass;

		private Pwbox(String username, boolean save) {
			setfocustab(true);
			add(new Label("User name", textf), Coord.z);
			add(user = new TextEntry(150, username), new Coord(0, 20));
			add(new Label("Password", textf), new Coord(0, 50));
			add(pass = new TextEntry(150, ""), new Coord(0, 70));
			pass.pw = true;
			add(savepass = new CheckBox("Remember me", true), new Coord(0, 100));
			savepass.a = save;
			if (user.text.isEmpty()) {
				setfocus(user);
			} else {
				setfocus(pass);
			}
			resize(new Coord(150, 150));
			LoginScreen.this.add(this, new Coord(345, 310));
		}

		@Override
		public void wdgmsg(Widget sender, String name, Object... args) {
		}

		@Override
		Object[] data() {
			return (new Object[]{new AuthClient.NativeCred(user.text, pass.text), savepass.a});
		}

		@Override
		boolean enter() {
			if (user.text.isEmpty()) {
				setfocus(user);
				return (false);
			} else if (pass.text.isEmpty()) {
				setfocus(pass);
				return (false);
			} else {
				return (true);
			}
		}

		@Override
		public boolean globtype(char k, KeyEvent ev) {
			if ((k == 'r') && ((ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0)) {
				savepass.set(!savepass.a);
				return (true);
			}
			return (false);
		}
	}

	private class Tokenbox extends Login {

		private final String name;
		private final String token;
		Text label;
		Button btn;

		private Tokenbox(String username, String token) {
			label = textfs.render("Identity is saved for " + username, java.awt.Color.WHITE);
			add(btn = new Button(100, "Forget me"), new Coord(75, 30));
			resize(new Coord(250, 100));
			LoginScreen.this.add(this, new Coord(295, 330));
			this.name = username;
			this.token = token;
		}

		@Override
		Object[] data() {
			return (new Object[]{name, token});
		}

		@Override
		boolean enter() {
			return (true);
		}

		@Override
		public void wdgmsg(Widget sender, String name, Object... args) {
			if (sender == btn) {
				LoginScreen.this.wdgmsg("forget");
				return;
			}
			super.wdgmsg(sender, name, args);
		}

		@Override
		public void draw(GOut g) {
			g.image(label.tex(), new Coord((sz.x / 2) - (label.sz().x / 2), 0));
			super.draw(g);
		}

		@Override
		public boolean globtype(char k, KeyEvent ev) {
			if ((k == 'f') && ((ev.getModifiersEx() & (KeyEvent.META_DOWN_MASK | KeyEvent.ALT_DOWN_MASK)) != 0)) {
				LoginScreen.this.wdgmsg("forget");
				return (true);
			}
			return (false);
		}
	}

	private void mklogin() {
		synchronized (ui) {
			adda(btn = new IButton("gfx/hud/buttons/login", "u", "d", "o") {
				@Override
				protected void depress() {
					Audio.play(Button.lbtdown.stream());
				}

				@Override
				protected void unpress() {
					Audio.play(Button.lbtup.stream());
				}
			}, 419, 510, 0.5, 0.5);
			progress(null);
		}
	}

	private void error(String error) {
		synchronized (ui) {
			if (this.error != null) {
				this.error = null;
			}
			if (error != null) {
				this.error = textf.render(error, java.awt.Color.RED);
			}
		}
	}

	private void progress(String p) {
		synchronized (ui) {
			if (progress != null) {
				progress = null;
			}
			if (p != null) {
				progress = textf.render(p, java.awt.Color.WHITE);
			}
		}
	}

	private void clear() {
		if (cur != null) {
			ui.destroy(cur);
			cur = null;
			ui.destroy(btn);
			btn = null;
		}
		progress(null);
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
		if (sender == btn) {
			if (cur.enter()) {
				super.wdgmsg("login", cur.data());
			}
			return;
		} else if (msg.equals("account")) {
			//repeat if was not token box to do actual login
			boolean repeat = !(cur instanceof Tokenbox);
			if (repeat) {
				super.wdgmsg("login", args[0], args[1]);
			}
			super.wdgmsg("login", args[0], args[1]);
			return;
		} else if (sender == optbtn) {
			if (opts == null) {
				opts = ui.root.adda(new OptWnd(false) {
					@Override
					public void hide() {
						/* XXX */
						reqdestroy();
					}
				}, sz.div(2), 0.5, 0.5);
			} else {
				opts.reqdestroy();
				opts = null;
			}
			return;
		} else if (sender == opts) {
			opts.reqdestroy();
			opts = null;
		}
		super.wdgmsg(sender, msg, args);
	}

	@Override
	public void cdestroy(Widget ch) {
		if (ch == opts) {
			opts = null;
		}
	}

	@Override
	public void uimsg(String msg, Object... args) {
		synchronized (ui) {
			switch (msg) {
				case "passwd":
					clear();
					cur = new Pwbox((String) args[0], (Boolean) args[1]);
					mklogin();
					break;
				case "token":
					clear();
					cur = new Tokenbox((String) args[0], (String) args[1]);
					mklogin();
					break;
				case "error":
					error((String) args[0]);
					break;
				case "prg":
					error(null);
					clear();
					progress((String) args[0]);
					break;
			}
		}
	}

	@Override
	public void presize() {
		c = parent.sz.div(2).sub(sz.div(2));
	}

	@Override
	protected void added() {
		presize();
		parent.setfocus(this);
		if (Config.isUpdate) {
			showChangeLog();
		}
	}

	@Override
	public void draw(GOut g) {
		super.draw(g);
		if (error != null) {
			g.image(error.tex(), new Coord(420 - (error.sz().x / 2), 450));
		}
		if (progress != null) {
			g.image(progress.tex(), new Coord(420 - (progress.sz().x / 2), 350));
		}
	}

	@Override
	public boolean type(char k, KeyEvent ev) {
		if (k == 10) {
			if ((cur != null) && cur.enter()) {
				wdgmsg("login", cur.data());
			}
			return (true);
		}
		return (super.type(k, ev));
	}

	@Override
	public void destroy() {
		if (log != null) {
			ui.destroy(log);
			log = null;
		}
		super.destroy();
	}
}
