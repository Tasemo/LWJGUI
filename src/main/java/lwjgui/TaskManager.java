/*
 * This file is part of Light Engine
 * 
 * Copyright (C) 2016-2019 Lux Vacuos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package lwjgui;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskManager {

	private Queue<Task<?>> tasksMainThread = new ConcurrentLinkedQueue<>(),
			tasksBackgroundThread = new ConcurrentLinkedQueue<>();
	private Thread mainThread, backgroundThread;
	private boolean syncInterrupt, runBackground = true;

	public TaskManager() {
	}

	public void init() {
		mainThread = Thread.currentThread(); // Let's assume init thread it's main
		backgroundThread = new Thread(() -> {
			while (runBackground) {
				if (!tasksBackgroundThread.isEmpty()) {
					while (!tasksBackgroundThread.isEmpty())
						tasksBackgroundThread.poll().callI();
				} else {
					syncInterrupt = false;
					ThreadUtils.sleep(Long.MAX_VALUE);
				}
			}
		});
		backgroundThread.setDaemon(true);
		//backgroundThread.setName("Window Background");
		backgroundThread.start();
	}
	
	public void dispose() {
		runBackground = false;
		backgroundThread.interrupt();
	}

	public void addTaskMainThread(Runnable task) {
		if (task == null)
			return;
		this.submitMainThread(new Task<Void>() {
			@Override
			protected Void call() {
				task.run();
				return null;
			}

		});
	}

	public void addTaskBackgroundThread(Runnable task) {
		if (task == null)
			return;
		this.submitBackgroundThread(new Task<Void>() {
			@Override
			protected Void call() {
				task.run();
				return null;
			}
		});
	}

	public <T> Task<T> submitMainThread(Task<T> t) {
		if (t == null)
			return null;

		if (Thread.currentThread().equals(mainThread))
			t.callI();
		else
			tasksMainThread.add(t);
		return t;
	}

	public <T> Task<T> submitBackgroundThread(Task<T> t) {
		if (t == null)
			return null;

		if (Thread.currentThread().equals(backgroundThread))
			t.callI();
		else {
			tasksBackgroundThread.add(t);
			if (!syncInterrupt) {
				syncInterrupt = true;
				backgroundThread.interrupt();
			}
		}
		return t;
	}

	public void updateMainThread() {
		if (!tasksMainThread.isEmpty())
			tasksMainThread.poll().callI();
	}

	public boolean isEmpty() {
		return tasksMainThread.isEmpty() && tasksBackgroundThread.isEmpty();
	}

}
