/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.krush;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Keith
 */
public class DurationEstimate {

	public String name;

	private double current = -1.0;
	private double previous = -1.0;
	private final double max;

	private Date lastChange = null;

	private final List<Double> changePerSecond = new ArrayList<>();
	private int count = -1;

	private double averageSeconds = 0.0;
	private double totalSeconds = 0.0;

	public DurationEstimate(String name, double max) {
		this.name = name;
		this.max = max;
	}

	private void calculateAverage() {
		Double sum = 0.0;
		if (!changePerSecond.isEmpty()) {
			for (Double value : changePerSecond) {
				sum += value;
			}
			averageSeconds = sum / changePerSecond.size();
		}
		averageSeconds = sum;
	}

	public void update(double value) {
		Date meterPrevUpdate = lastChange;

		previous = current;
		current = value;

		if (previous != value) {
			if (current == 0) {
				count = -1;
				meterPrevUpdate = null;
				changePerSecond.clear();
			} else {
				lastChange = new Date();
			}
			if (meterPrevUpdate != null) {
				if (count++ <= 0) {
				} else {
					double a = current - previous;

					long secondsSinceUpdate = (lastChange.getTime() - meterPrevUpdate.getTime()) / 1000;
					double meterPerSec = a * secondsSinceUpdate;

					changePerSecond.add(meterPerSec);
					calculateAverage();
					updated();
				}
			}
		}
	}

	public void updated() {

	}

	@Override
	public String toString() {
		long totalSecs = (long) ((double) (max - current) * averageSeconds);
		long hours = totalSecs / 3600;
		long minutes = (totalSecs % 3600) / 60;
		long seconds = totalSecs % 60;
		return String.format("[%s] avgSecs: %.2f | time remaining: %dh, %dm, %ds", name, averageSeconds, hours, minutes, seconds);
	}
}
