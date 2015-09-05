package haven;

import me.ender.Reflect;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class QualityList {

	public static final String classname = "haven.res.ui.tt.q.qbuff.QBuff";

	private final List<Quality> qualities;
	private Tex tex;
	private final Quality max, average;
	private final boolean isEmpty;

	public QualityList(List<ItemInfo> list) {
		qualities = new LinkedList<Quality>();
		for (ItemInfo inf : list) {
			if (inf.getClass().getName().equals(classname)) {
				String name = Reflect.getFieldValueString(inf, "name");
				int q = Reflect.getFieldValueInt(inf, "q");
				try {
					qualities.add(new Quality(QualityType.valueOf(name), q));
				} catch (IllegalArgumentException ignored) {
				}
			}
		}
		Collections.sort(qualities, new Comparator<Quality>() {
			@Override
			public int compare(Quality o1, Quality o2) {
				return o1.type.name().compareTo(o2.type.name());
			}
		});
		int size = qualities.size();
		isEmpty = size == 0;
		if (!isEmpty) {
			boolean equal = true;
			Quality cmax = qualities.get(0);
			float sum = cmax.value;
			for (int i = 1; i < size; i++) {
				Quality current = qualities.get(i);
				equal = equal && (current.value == cmax.value);
				cmax = (cmax.value >= current.value) ? cmax : current;
				sum += current.value;
			}
			max = equal ? new Quality(QualityType.Quality, cmax.value) : cmax;
			average = new Quality(max.type, sum / size);
		} else {
			max = null;
			average = null;
		}
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public Quality single() {
		return CFG.Q_MAX_SINGLE.valb() ? max : average;
	}

	public Tex tex() {
		if (tex == null) {
			BufferedImage[] imgs = new BufferedImage[qualities.size()];
			for (int i = 0; i < qualities.size(); i++) {
				imgs[i] = qualities.get(i).tex().back;
			}
			tex = new TexI(ItemInfo.catimgs(-6, true, imgs));
		}
		return tex;
	}

	public static class Quality {

		public static final DecimalFormat format = new DecimalFormat("#.#");
		private final QualityType type;
		public final float value;
		private TexI tex;

		public Quality(QualityType type, float value) {
			this.type = type;
			this.value = value;
		}

		public TexI tex() {
			if (tex == null) {
				String text = String.format("%s", format.format(value));
				BufferedImage img = Text.render(text, type.color).img;
				tex = new TexI(Utils.outline2(img, type.outline, true));
			}
			return tex;
		}
	}

	enum QualityType {

		Essence(new Color(240, 140, 255)),
		Substance(new Color(255, 240, 140)),
		Vitality(new Color(152, 255, 140)),
		Quality(new Color(235, 255, 255));
		public final Color color, outline;

		QualityType(Color color) {
			this.color = color;
			this.outline = Utils.contrast(color);
		}
	}
}
