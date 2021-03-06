package me.kt;

import haven.BGL;
import haven.CFG;
import haven.Coord;
import haven.Coord3f;
import haven.GOut;
import haven.Loading;
import haven.Location;
import haven.MCache;
import haven.RenderList;
import haven.Rendered;
import haven.States;
import haven.Utils;
import java.nio.FloatBuffer;
import javax.media.opengl.GL2;

public class GridOutline implements Rendered {

	private final MCache map;
	private final Buffer[] buffers;
	private final int area;
	private final Coord size;
	private final States.ColState color;
	private Location location;
	private Coord ul;
	private int curIndex;

	static class Buffer {

		public FloatBuffer vertex;
		public FloatBuffer color;

		public Buffer(int area) {
			vertex = Utils.mkfbuf(area * 3 * 4);
			color = Utils.mkfbuf(area * 4 * 4);
		}

		void rewind() {
			vertex.rewind();
			color.rewind();
		}

		void putVertex(float x, float y, float z) {
			vertex.put(x).put(y).put(z);
		}

		void putColor(float r, float g, float b, float a) {
			color.put(r).put(g).put(b).put(a);
		}
	}

	public GridOutline(MCache map, Coord size) {
		this.map = map;
		this.size = size;
		this.area = (size.x + 1) * (size.y + 1);
		this.color = new States.ColState(255, 255, 255, 128);

		// double-buffer to prevent flickering
		buffers = new Buffer[2];
		for (int i = 0; i < buffers.length; i++) {
			buffers[i] = new Buffer(area);
		}
		curIndex = 0;
	}

	@Override
	public void draw(GOut g) {
		g.apply();
		BGL gl = g.gl;
		Buffer buf = getCurrentBuffer();
		buf.rewind();
		gl.glHint(GL2.GL_LINE_SMOOTH_HINT, GL2.GL_NICEST);
		gl.glLineWidth(CFG.DISPLAY_GRID_THICKNESS.valf());
		gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL2.GL_COLOR_ARRAY);
		gl.glVertexPointer(3, GL2.GL_FLOAT, 0, buf.vertex);
		gl.glColorPointer(4, GL2.GL_FLOAT, 0, buf.color);
		gl.glDrawArrays(GL2.GL_LINES, 0, area * 4);
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
	}

	@Override
	public boolean setup(RenderList rl) {
		if (location == null) {
			return false;
		}
		rl.prepo(location);
		rl.prepo(color);
		return true;
	}

	public void update(Coord ul) {
		try {
			this.ul = ul;
			this.location = Location.xlate(new Coord3f(ul.x * MCache.tilesz.x, -ul.y * MCache.tilesz.y, 0.0F));
			swapBuffers();
			Coord c = new Coord();
			for (c.y = ul.y; c.y <= ul.y + size.y; c.y++) {
				for (c.x = ul.x; c.x <= ul.x + size.x; c.x++) {
					addLineStrip(mapToScreen(c), mapToScreen(c.add(1, 0)), mapToScreen(c.add(1, 1)));
				}
			}
		} catch (Loading e) {
		}
	}

	private Coord3f mapToScreen(Coord c) {
		return new Coord3f((c.x - ul.x) * MCache.tilesz.x, -(c.y - ul.y) * MCache.tilesz.y, map.getz(c));
	}

	private void addLineStrip(Coord3f... vertices) {
		Buffer buf = getCurrentBuffer();
		for (int i = 0; i < vertices.length - 1; i++) {
			Coord3f a = vertices[i];
			Coord3f b = vertices[i + 1];
			buf.putVertex(a.x, a.y, a.z + 0.1F);
			buf.putVertex(b.x, b.y, b.z + 0.1F);
			if (a.z == b.z) {
				buf.putColor(0F, 1F, 0F, 0.5F);
				buf.putColor(0F, 1F, 0F, 0.5F);
			} else {
				buf.putColor(1F, 0F, 0F, 0.5F);
				buf.putColor(1F, 0F, 0F, 0.5F);
			}
		}
	}

	private Buffer getCurrentBuffer() {
		return buffers[curIndex];
	}

	private void swapBuffers() {
		curIndex = (curIndex + 1) % 2;
	}
}
