package lwjgui;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

import lwjgui.font.Font;
import lwjgui.scene.Scene;
import lwjgui.scene.Window;
import lwjgui.scene.control.Label;
import lwjgui.scene.layout.StackPane;

public class HelloWorldManual {
	public static final int WIDTH = 320;
	public static final int HEIGHT = 240;

	public static void main(String[] args) {
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize GLFW");

		Font.initFonts();

		// Create a standard opengl 3.2 window. You can do this yourself.
		long window = LWJGUIUtil.createOpenGLCoreWindow("Hello World", WIDTH, HEIGHT, true, false);

		// Initialize lwjgui for this window
		Window lwjguiWindow = LWJGUI.initialize(window);
		lwjguiWindow.show();

		// Add some components
		addComponents(lwjguiWindow.getScene());
		// Create a standard opengl 3.2 window. You can do this yourself.
		window = LWJGUIUtil.createOpenGLCoreWindow("Hello World", WIDTH, HEIGHT, true, false);

		// Initialize lwjgui for this window
		lwjguiWindow = LWJGUI.initialize(window);
		lwjguiWindow.show();

		// Add some components
		addComponents(lwjguiWindow.getScene());
		// Create a standard opengl 3.2 window. You can do this yourself.
		window = LWJGUIUtil.createOpenGLCoreWindow("Hello World", WIDTH, HEIGHT, true, false);

		// Initialize lwjgui for this window
		lwjguiWindow = LWJGUI.initialize(window);
		lwjguiWindow.show();

		// Add some components
		addComponents(lwjguiWindow.getScene());

		// Game Loop
		while (LWJGUI.hasAnyWindow()) {
			LWJGUI.update(60);
		}
		Font.disposeFonts();

		// Stop GLFW
		glfwTerminate();
	}

	private static void addComponents(Scene scene) {
		// Create a simple pane
		StackPane pane = new StackPane();

		// Set the pane as the scenes root
		scene.setRoot(pane);

		// Put a label in the pane
		pane.getChildren().add(new Label("Hello World!"));
	}
}