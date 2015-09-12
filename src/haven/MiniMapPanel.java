package haven;

public class MiniMapPanel extends ResizingWindow {

    private LocalMiniMap mmap;

    public MiniMapPanel(Coord sz) {
	super(sz, "Minimap");
	justclose = true;
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
