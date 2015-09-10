package haven;

import me.ender.timer.Timer;
import me.ender.timer.TimerController;

public class TimerPanel extends Window {
    
    private final TimerController controller;
    private Button btnnew;
    
    public TimerPanel() {
	super(Coord.z, "Timers");
	justclose = true;
	btnnew = add(new Button(100, "Add timer"));

	controller = new TimerController();
	synchronized(controller.lock) {
	    for(Timer timer : controller.timers) {
		timer.controller = controller;
		add(new TimerWdg(timer));
	    }
	}
	pack();
	visible = false;
    }

    public void toggle() {
	visible = !visible;
    }

    @Override
    public void tick(double dt) {
	super.tick(dt);
	controller.update(dt);
    }

    @Override
    public void pack() {
	int n, i = 0, h = 0;
	synchronized(controller.lock) {
	    n = controller.timers.size();
	}
	n = (int) Math.ceil(Math.sqrt((double) n / 3));
	for(Widget wdg = child; wdg != null; wdg = wdg.next) {
	    if(!(wdg instanceof TimerWdg))
		continue;
	    wdg.c = new Coord((i % n) * wdg.sz.x, (i / n) * wdg.sz.y);
	    h = wdg.c.y + wdg.sz.y;
	    i++;
	}
	
	btnnew.c = new Coord(0, h);
	super.pack();
    }

    @Override
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == btnnew) {
	    ui.gui.add(new TimerAddWdg(this), c);
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
    
    @Override
    public void close() {
	visible = false;
    }

    class TimerAddWdg extends Window {
	
	private TextEntry name, hours, minutes, seconds;
	private Button btnadd;
	private TimerPanel panel;
	
	public TimerAddWdg(TimerPanel panel) {
	    super(Coord.z, "Add timer");
	    justclose = true;
	    this.panel = panel;
	    name = add(new TextEntry(150, "timer"));
	    add(new Label("hours"), 0, 25);
	    add(new Label("min"), 50, 25);
	    add(new Label("sec"), 100, 25);
	    hours = add(new TextEntry(45, "0"), 0, 40);
	    minutes = add(new TextEntry(45, "00"), 50, 40);
	    seconds = add(new TextEntry(45, "00"), 100, 40);
	    btnadd = add(new Button(100, "Add"), 0, 60);
	    pack();
	}

	@Override
	public void wdgmsg(Widget sender, String msg, Object... args) {
	    if(sender == btnadd) {
		try {
		    long time = 0;
		    time += Integer.parseInt(seconds.text);
		    time += Integer.parseInt(minutes.text) * 60;
		    time += Integer.parseInt(hours.text) * 3600;
		    Timer timer = new Timer();
		    timer.controller = controller;
		    timer.setDuration(1000 * time);
		    timer.setName(name.text);
		    controller.add(timer);
		    panel.add(new TimerWdg(timer));
		    panel.pack();
		    ui.destroy(this);
		} catch(Exception e) {
		    System.out.println(e.getMessage());
		    e.printStackTrace();
		}
	    } else {
		super.wdgmsg(sender, msg, args);
	    }
	}

	@Override
	public void destroy() {
	    panel = null;
	    super.destroy();
	}
	
    }
}
