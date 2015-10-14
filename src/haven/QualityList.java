package haven;

import me.ender.Reflect;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class QualityList {

	public static final String classname = "haven.res.ui.tt.q.qbuff.QBuff";
	public static final Comparator<Quality> QSORTER = new Comparator<Quality>() {
		@Override
		public int compare(Quality o1, Quality o2) {
			return o1.type.name().compareTo(o2.type.name());
		}
	};

	private final List<Quality> qualities;
	private Tex tex;
	private final Map<SingleType, Quality> singles = new HashMap<>();
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
		Collections.sort(qualities, QSORTER);
		isEmpty = qualities.isEmpty();
		if (!isEmpty) {
			SingleType[] types = SingleType.values();
			for (SingleType type : types) {
				singles.put(type, type.get(qualities));
			}
		}
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public Quality single() {
		switch (CFG.UI_ITEM_QUALITY_SHOW.vali()) {
			case 0:
				break;
			case 1:
				return single(SingleType.Average);
			case 2:
				return single(SingleType.Max);
			case 3:
				return single(SingleType.Essence);
			case 4:
				return single(SingleType.Substance);
			case 5:
				return single(SingleType.Vitality);
			case 6:
				break;
		}
		return single(SingleType.Max);
	}

	public Quality single(SingleType singleType) {
		return singles.get(singleType);
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

	public Tex tex(Quality quality) {
		return quality.tex();
	}

	public static class Quality implements Comparable<Quality> {

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

		@SuppressWarnings("NullableProblems")
		@Override
		public int compareTo(Quality o) {
			return Float.compare(value, o != null ? o.value : 0);
		}
	}

	enum QualityType {

		Essence(new Color(240, 140, 255)),
		Substance(new Color(255, 236, 115)),
		Vitality(new Color(152, 255, 140)),
		Quality(new Color(235, 255, 255));
		public final Color color, outline;

		QualityType(Color color) {
			this.color = color;
			this.outline = Color.BLACK;
		}
	}

	enum SingleType {

		Average {
							@Override
							public Quality get(List<Quality> qualities) {
								if (qualities.isEmpty()) {
									return null;
								}
								float sum = 0;
								for (Quality q : qualities) {
									sum += q.value * q.value;
								}
								return new Quality(Max.get(qualities).type, (float) Math.sqrt(sum / qualities.size()));
							}
						},
		Min {
							@Override
							public Quality get(List<Quality> qualities) {
								Quality min = Collections.min(qualities);
								if (equal(qualities)) {
									min = new Quality(QualityType.Quality, min.value);
								}
								return min;
							}
						},
		Max {
							@Override
							public Quality get(List<Quality> qualities) {
								Quality max = Collections.max(qualities);
								if (equal(qualities)) {
									max = new Quality(QualityType.Quality, max.value);
								}
								return max;
							}
						},
		Essence {
							@Override
							public Quality get(List<Quality> qualities) {
								for (Quality q : qualities) {
									if (q.type == QualityType.Essence) {
										return q;
									}
								}
								return null;
							}
						},
		Substance {
							@Override
							public Quality get(List<Quality> qualities) {
								for (Quality q : qualities) {
									if (q.type == QualityType.Substance) {
										return q;
									}
								}
								return null;
							}
						},
		Vitality {
							@Override
							public Quality get(List<Quality> qualities) {
								for (Quality q : qualities) {
									if (q.type == QualityType.Vitality) {
										return q;
									}
								}
								return null;
							}
						};

		private Tex tex;

		public abstract Quality get(List<Quality> qualities);

		public Tex tex() {
			if (tex == null) {
				tex = Text.render(name()).tex();
			}
			return tex;
		}

		private static boolean equal(List<Quality> qualities) {
			if (qualities.isEmpty()) {
				return true;
			}
			Quality q0 = qualities.get(0);
			for (Quality q : qualities) {
				if (q0.compareTo(q) != 0) {
					return false;
				}
			}
			return true;
		}
	}
}
