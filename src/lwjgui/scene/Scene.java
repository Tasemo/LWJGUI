package lwjgui.scene;

import org.lwjgl.glfw.GLFW;

import lwjgui.collections.ObservableList;
import lwjgui.scene.control.PopupWindow;

public class Scene extends Node {
	private Node root;
	
	private ObservableList<PopupWindow> popups = new ObservableList<PopupWindow>();

	@Override
	public double getAbsoluteX() {
		return 0;
	}
	
	@Override
	public double getAbsoluteY() {
		return 0;
	}
	
	@Override
	public double getWidth() {
		return this.getPrefWidth();
	}
	
	@Override
	public double getHeight() {
		return this.getPrefHeight();
	}
	
	public void setRoot(Node node) {
		this.children.clear();
		this.children.add(node);
		this.root = node;
	}

	public Node getRoot() {
		return this.root;
	}
	
	@Override
	public boolean isResizeable() {
		return false;
	}

	@Override
	public void render(Context context) {
		if ( root == null )
			return;
		
		if ( root instanceof Region ) {
			((Region) root).setFillToParentHeight(true);
			((Region) root).setFillToParentWidth(true);
		}
		
		// Position elements
		this.flag_clip = true;
		for (int i = 0; i < 4; i++) {
			position(null);
			root.position(this);
		}
		
		// Render normal
		root.render(context);
		
		// Render popups
		clip(context);
		for (int i = 0; i < popups.size(); i++) {
			PopupWindow p = popups.get(i);
			p.render(context);
		}
	}

	public void showPopup(PopupWindow popup) {
		popups.add(popup);
	}
	
	public void closePopup(PopupWindow popup) {
		popups.remove(popup);
	}

	public ObservableList<PopupWindow> getPopups() {
		return this.popups;
	}

	public void setCursor(Cursor cursor) {
		GLFW.glfwSetCursor(cached_context.getWindowHandle(), cursor.getCursor(cached_context.getWindowHandle()));
	}
}
