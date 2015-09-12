package haven;

public class MiniMapPanel extends ResizingWindow {

    private LocalMiniMap mmap;

    public MiniMapPanel(Coord sz) {
	super(sz, "Minimap");
	justclose = true;
	addtwdg(add(new IButton("gfx/hud/mmap/claim", "", "-d", "-h"){
	    {tooltip = Text.render("Display personal claims");}
	    public void click() {
		if(ui != null && ui.gui != null && ui.gui.map != null) {
		    if(!ui.gui.map.visol(0))
			ui.gui.map.enol(0, 1);
		    else
			ui.gui.map.disol(0, 1);
		}
	    }
	}));
	addtwdg(add(new IButton("gfx/hud/mmap/vil", "", "-d", "-h"){
	    {tooltip = Text.render("Display village claims");}
	    public void click() {
		if(ui != null && ui.gui != null && ui.gui.map != null) {
		    if(!ui.gui.map.visol(2))
			ui.gui.map.enol(2, 3);
		    else
			ui.gui.map.disol(2, 3);
		}
	    }
	}));
    }

    @Override
    public void resize(Coord sz) {
	super.resize(sz);
	if(mmap != null) {
	    mmap.sz = sz;
	}
    }

    public Widget setmap(LocalMiniMap mmap) {
	this.mmap = add(mmap, Coord.z);
	mmap.sz = asz;
	return mmap;
    }
}
