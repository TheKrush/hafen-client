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

import haven.glsl.*;
import javax.media.opengl.*;
import java.awt.Color;

public abstract class States extends GLState {

	private States() {
	}

	public static final Slot<ColState> color = new Slot<>(Slot.Type.DRAW, ColState.class, HavenPanel.global);

	public static class ColState extends GLState {

		private static final ShaderMacro[] shaders = {new haven.glsl.GLColorVary()};
		public final Color c;
		public final float[] ca;

		public ColState(Color c) {
			this.c = c;
			this.ca = Utils.c2fa(c);
		}

		public ColState(int r, int g, int b, int a) {
			this(Utils.clipcol(r, g, b, a));
		}

		@Override
		public void apply(GOut g) {
			BGL gl = g.gl;
			gl.glColor4fv(ca, 0);
		}

		@Override
		public int capply() {
			return (1);
		}

		@Override
		public void unapply(GOut g) {
			BGL gl = g.gl;
			gl.glColor3f(1, 1, 1);
		}

		@Override
		public int capplyfrom(GLState o) {
			if (o instanceof ColState) {
				return (1);
			}
			return (-1);
		}

		@Override
		public void applyfrom(GOut g, GLState o) {
			apply(g);
		}

		@Override
		public ShaderMacro[] shaders() {
			return (shaders);
		}

		@Override
		public void prep(Buffer buf) {
			buf.put(color, this);
		}

		@Override
		public boolean equals(Object o) {
			return ((o instanceof ColState) && (((ColState) o).c == c));
		}

		@Override
		public String toString() {
			return ("ColState(" + c + ")");
		}
	}
	public static final ColState vertexcolor = new ColState(0, 0, 0, 0) {
		@Override
		public void apply(GOut g) {
		}

		@Override
		public boolean equals(Object o) {
			return (o == this);
		}

		@Override
		public String toString() {
			return ("ColState(vertex)");
		}
	};

	@Material.ResName("vcol")
	public static class $vcol implements Material.ResCons {

		@Override
		public GLState cons(Resource res, Object... args) {
			return (new States.ColState((Color) args[0]));
		}
	}

	public static final StandAlone ndepthtest = new StandAlone(Slot.Type.GEOM, PView.proj) {
		@Override
		public void apply(GOut g) {
			g.gl.glDisable(GL.GL_DEPTH_TEST);
		}

		@Override
		public void unapply(GOut g) {
			g.gl.glEnable(GL.GL_DEPTH_TEST);
		}
	};

	public static final GLState xray = compose(ndepthtest, Rendered.last);

	public static final StandAlone fsaa = new StandAlone(Slot.Type.SYS, PView.proj) {
		@Override
		public void apply(GOut g) {
			g.gl.glEnable(GL.GL_MULTISAMPLE);
		}

		@Override
		public void unapply(GOut g) {
			g.gl.glDisable(GL.GL_MULTISAMPLE);
		}
	};

	public static final Slot<Coverage> coverage = new Slot<>(Slot.Type.DRAW, Coverage.class, PView.proj);

	public static class Coverage extends GLState {

		public final float cov;
		public final boolean inv;

		public Coverage(float cov, boolean inv) {
			this.cov = cov;
			this.inv = inv;
		}

		@Override
		public void apply(GOut g) {
			BGL gl = g.gl;
			gl.glEnable(GL.GL_SAMPLE_COVERAGE);
			gl.glSampleCoverage(cov, inv);
		}

		@Override
		public void unapply(GOut g) {
			BGL gl = g.gl;
			gl.glSampleCoverage(1.0f, false);
			gl.glDisable(GL.GL_SAMPLE_COVERAGE);
		}

		@Override
		public void prep(Buffer buf) {
			buf.put(coverage, this);
		}
	};

	public static final StandAlone presdepth = new StandAlone(Slot.Type.GEOM, PView.proj) {
		@Override
		public void apply(GOut g) {
			g.gl.glDepthMask(false);
		}

		@Override
		public void unapply(GOut g) {
			g.gl.glDepthMask(true);
		}
	};

	public static final StandAlone prescolor = new StandAlone(Slot.Type.DRAW, PView.proj) {
		@Override
		public void apply(GOut g) {
			g.gl.glColorMask(false, false, false, false);
		}

		@Override
		public void unapply(GOut g) {
			g.gl.glColorMask(true, true, true, true);
		}
	};

	@Material.ResName("maskcol")
	public static class $colmask implements Material.ResCons {

		@Override
		public GLState cons(Resource res, Object... args) {
			return (prescolor);
		}
	}

	public static final Slot<Fog> fog = new Slot<>(Slot.Type.DRAW, Fog.class, PView.proj);

	public static class Fog extends GLState {

		public final Color c;
		public final float[] ca;
		public final float s, e;

		public Fog(Color c, float s, float e) {
			this.c = c;
			this.ca = Utils.c2fa(c);
			this.s = s;
			this.e = e;
		}

		@Override
		public void apply(GOut g) {
			BGL gl = g.gl;
			gl.glFogi(GL2.GL_FOG_MODE, GL2.GL_LINEAR);
			gl.glFogf(GL2.GL_FOG_START, s);
			gl.glFogf(GL2.GL_FOG_END, e);
			gl.glFogfv(GL2.GL_FOG_COLOR, ca, 0);
			gl.glEnable(GL2.GL_FOG);
		}

		@Override
		public void unapply(GOut g) {
			BGL gl = g.gl;
			gl.glDisable(GL2.GL_FOG);
		}

		@Override
		public void prep(Buffer buf) {
			buf.put(fog, this);
		}
	}

	public static final Slot<DepthOffset> depthoffset = new Slot<>(Slot.Type.GEOM, DepthOffset.class, PView.proj);

	public static class DepthOffset extends GLState {

		public final int mode;
		public final float factor, units;

		public DepthOffset(int mode, float factor, float units) {
			this.mode = mode;
			this.factor = factor;
			this.units = units;
		}

		public DepthOffset(float factor, float units) {
			this(GL.GL_POLYGON_OFFSET_FILL, factor, units);
		}

		@Override
		public void apply(GOut g) {
			BGL gl = g.gl;
			gl.glPolygonOffset(factor, units);
			gl.glEnable(mode);
		}

		@Override
		public void unapply(GOut g) {
			BGL gl = g.gl;
			gl.glDisable(mode);
		}

		@Override
		public void prep(Buffer buf) {
			buf.put(depthoffset, this);
		}
	}

	public static class PolygonMode extends GLState {

		public static final Slot<PolygonMode> slot = new Slot<>(Slot.Type.GEOM, PolygonMode.class, PView.proj);
		public final int mode;

		public PolygonMode(int mode) {
			this.mode = mode;
		}

		@Override
		public void apply(GOut g) {
			g.gl.glPolygonMode(GL.GL_FRONT_AND_BACK, mode);
		}

		@Override
		public void unapply(GOut g) {
			g.gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
		}

		@Override
		public void prep(Buffer buf) {
			buf.put(slot, this);
		}
	}

	public static final StandAlone nullprog = new StandAlone(Slot.Type.DRAW, PView.proj) {
		private final ShaderMacro[] sh = {};

		@Override
		public void apply(GOut g) {
		}

		@Override
		public void unapply(GOut g) {
		}

		@Override
		public ShaderMacro[] shaders() {
			return (sh);
		}
	};

	public static final Slot<GLState> adhoc = new Slot<>(Slot.Type.DRAW, GLState.class, PView.wnd);

	public static class AdHoc extends GLState {

		private final ShaderMacro[] sh;

		public AdHoc(ShaderMacro[] sh) {
			this.sh = sh;
		}

		public AdHoc(ShaderMacro sh) {
			this(new ShaderMacro[]{sh});
		}

		@Override
		public void apply(GOut g) {
		}

		@Override
		public void unapply(GOut g) {
		}

		@Override
		public ShaderMacro[] shaders() {
			return (sh);
		}

		@Override
		public void prep(Buffer buf) {
			buf.put(adhoc, this);
		}
	}

	public static final Slot<GLState> adhocg = new Slot<>(Slot.Type.GEOM, GLState.class, PView.wnd);

	public static class GeomAdHoc extends GLState {

		private final ShaderMacro[] sh;

		public GeomAdHoc(ShaderMacro[] sh) {
			this.sh = sh;
		}

		public GeomAdHoc(ShaderMacro sh) {
			this(new ShaderMacro[]{sh});
		}

		@Override
		public void apply(GOut g) {
		}

		@Override
		public void unapply(GOut g) {
		}

		@Override
		public ShaderMacro[] shaders() {
			return (sh);
		}

		@Override
		public void prep(Buffer buf) {
			buf.put(adhocg, this);
		}
	}

	public static final StandAlone normalize = new StandAlone(Slot.Type.GEOM, PView.proj) {
		@Override
		public void apply(GOut g) {
			g.gl.glEnable(GL2.GL_NORMALIZE);
		}

		@Override
		public void unapply(GOut g) {
			g.gl.glDisable(GL2.GL_NORMALIZE);
		}
	};

	public static final Slot<GLState> pointsize = new Slot<>(Slot.Type.GEOM, GLState.class, HavenPanel.global);

	public static class PointSize extends GLState {

		private final float sz;

		public PointSize(float sz) {
			this.sz = sz;
		}

		@Override
		public void apply(GOut g) {
			g.gl.glPointSize(sz);
		}

		@Override
		public void unapply(GOut g) {
		}

		@Override
		public void prep(Buffer buf) {
			buf.put(pointsize, this);
		}
	}

	public static class ProgPointSize extends GLState {

		public final ShaderMacro[] sh;

		public ProgPointSize(final ShaderMacro sh) {
			this.sh = new ShaderMacro[]{new ShaderMacro() {
				@Override
				public void modify(ProgramContext prog) {
					prog.vctx.ptsz.force();
					sh.modify(prog);
				}
			}};
		}

		public ProgPointSize(final Expression ptsz) {
			this(new ShaderMacro() {
				@Override
				public void modify(ProgramContext prog) {
					prog.vctx.ptsz.mod(new Macro1<Expression>() {
						@Override
						public Expression expand(Expression in) {
							return (ptsz);
						}
					}, 0);
				}
			});
		}

		@Override
		public void apply(GOut g) {
			g.gl.glEnable(GL3.GL_PROGRAM_POINT_SIZE);
		}

		@Override
		public void unapply(GOut g) {
			g.gl.glDisable(GL3.GL_PROGRAM_POINT_SIZE);
		}

		@Override
		public ShaderMacro[] shaders() {
			return (sh);
		}

		@Override
		public void prep(Buffer buf) {
			buf.put(pointsize, this);
		}
	}
}
