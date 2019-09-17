package lwjgui;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import lwjgui.scene.Context;
import lwjgui.scene.Scene;
import lwjgui.scene.Window;
import lwjgui.scene.layout.StackPane;
import lwjgui.transition.TransitionManager;

public class LWJGUI {
	private static HashMap<Long, Window> windows = new HashMap<Long, Window>();
	private static List<Runnable> runnables = Collections.synchronizedList(new ArrayList<Runnable>());

	private static List<Window> toRemove = new ArrayList<>();
	
	private static ClientSync sync = new ClientSync();

	/**
	 * Initializes a LWJGUI window from a GLFW window handle. The window contains a
	 * Scene class.<br>
	 * Rendering components can be added to the scene. However, to set initial
	 * rendering, the scene's root node must first be set.
	 * 
	 * @param window
	 * @return Returns a LWJGUI Window that contains a rendering Context and a
	 *         Scene.
	 */
	public static Window initialize(long window) {
		if (windows.containsKey(window)) {
			System.err.println("Failed to initialize this LWJGUI Window. Already initialized.");
			return null;
		}
		Context context = new Context(window);
		Scene scene = new Scene(new StackPane());
		Window wind = new Window(context, scene);
		windows.put(window, wind);
		return wind;
	}

	/**
	 * This method renders all of the initialized LWJGUI windows.<br>
	 * It will loop through each window, set the context, and then render.<br>
	 * <br>
	 * You can set a rendering callback to a window if you want your own rendering
	 * at the start of a render-pass.
	 */
	public static void update(int ups) {
		// poll events to callbacks
		glfwPollEvents();

		TransitionManager.tick();

		// Get list of runnables
		List<Runnable> newRunnable = new ArrayList<Runnable>();
		synchronized (runnables) {
			while (runnables.size() > 0) {
				newRunnable.add(runnables.get(0));
				runnables.remove(0);
			}
		}

		// Execute Runnables
		for (int i = 0; i < newRunnable.size(); i++) {
			newRunnable.get(i).run();
		}
		newRunnable.clear();

		for (Window window : windows.values()) {
			if (!window.isCreated())
				continue;
			if (window.isShouldClose()) {
				window.closeWindow();
				toRemove.add(window);
			}
		}

		for (Window window : toRemove) {
			windows.remove(window.getContext().getWindowHandle());
		}
		sync.sync(ups);
	}

	public static void closeAllWindows() {
		for (Window window : windows.values()) {
			if (!window.isCreated())
				continue;
			window.closeWindow();
		}
	}

	public static boolean hasAnyWindow() {
		return !windows.isEmpty();
	}

	/**
	 * Runs the specified runnable object at the end of the current LWJGUI frame.
	 * 
	 * @param runnable
	 */
	public static void runLater(Runnable runnable) {
		synchronized (runnables) {
			runnables.add(runnable);
		}
	}

}
