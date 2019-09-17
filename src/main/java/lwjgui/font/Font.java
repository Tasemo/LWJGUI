package lwjgui.font;

import static org.lwjgl.nanovg.NanoVG.nvgCreateFontMem;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.system.MemoryUtil.memRealloc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.nanovg.NanoVG;

import lwjgui.scene.Context;

public class Font {
	public static Font SANS;
	public static Font COURIER;
	public static Font CONSOLAS;
	public static Font ARIAL;
	public static Font DINGBAT;

	private static ByteBuffer fallbackSansEmoji;
	private static ByteBuffer fallbackRegularEmoji;
	private static ByteBuffer fallbackArial;
	private static ByteBuffer fallbackEntypo;

	public static void initFonts() {
		try {
			fallbackSansEmoji = ioResourceToByteBuffer("lwjgui/scene/layout/OpenSansEmoji.ttf", 561512 + 5);
			fallbackRegularEmoji = ioResourceToByteBuffer("lwjgui/scene/layout/NotoEmoji-Regular.ttf", 418804 + 5);
			fallbackArial = ioResourceToByteBuffer("lwjgui/scene/layout/Arial-Unicode.ttf", 23275812 + 5);
			fallbackEntypo = ioResourceToByteBuffer("lwjgui/scene/layout/entypo.ttf", 35392 + 5);
		} catch (IOException e) {
			e.printStackTrace();
		}

		SANS = new Font("lwjgui/scene/layout/", "Roboto-Regular.ttf", "Roboto-Bold.ttf", "Roboto-Italic.ttf",
				"Roboto-Light.ttf");
		COURIER = new Font("lwjgui/scene/layout/", "Courier-New-Regular.ttf", "Courier-New-Bold.ttf",
				"Courier-New-Italic.ttf", null);
		CONSOLAS = new Font("lwjgui/scene/layout/", "Consolas-Regular.ttf", "Consolas-Bold.ttf", "Consolas-Italic.ttf",
				null);
		ARIAL = new Font("lwjgui/scene/layout/", "Arial-Unicode.ttf");
		DINGBAT = new Font("lwjgui/scene/layout/", "ErlerDingbats.ttf");
	}

	public static void disposeFonts() {
		SANS.dispose();
		COURIER.dispose();
		CONSOLAS.dispose();
		ARIAL.dispose();
		DINGBAT.dispose();
		memFree(fallbackSansEmoji);
		memFree(fallbackRegularEmoji);
		memFree(fallbackArial);
		memFree(fallbackEntypo);
	}
	
	private String fontPath;
	private String fontNameRegular;
	private String fontNameBold;
	private String fontNameLight;
	private String fontNameItalic;
	private Map<String, ByteBuffer> fontsBuffer = new HashMap<>();

	/**
	 * Creates a new font with the given settings.
	 * 
	 * @param fontPath        - the folder path of the font files
	 * @param regularFileName - the filename of the regular font TTF (e.g.
	 *                        fontName.ttf)
	 * @param boldFileName    - the filename of the bold font TTF
	 * @param italicFileName  - the filename of the italic font TTF
	 * @param lightFileName   - the filename of the light font TTF
	 */
	public Font(String fontPath, String regularFileName, String boldFileName, String italicFileName,
			String lightFileName) {
		this.fontPath = fontPath;
		this.fontNameRegular = regularFileName;
		this.fontNameBold = boldFileName;
		this.fontNameLight = lightFileName;
		this.fontNameItalic = italicFileName;
		this.loadFonts();
	}

	/**
	 * Creates a new font with the given settings. Only the regular font is set and
	 * made available.
	 * 
	 * @param fontPath        - the folder path of the font files
	 * @param regularFileName - the filename of the regular font TTF (e.g.
	 *                        fontName.ttf)
	 */

	public Font(String fontPath, String regularFileName) {
		this.fontPath = fontPath;
		this.fontNameRegular = regularFileName;
		this.loadFonts();
	}

	public void dispose() {
		for (ByteBuffer byteBuffer : fontsBuffer.values()) {
			memFree(byteBuffer);
		}
	}

	public void loadIntoContext(Context context) {
		long vg = context.getNVG();
		for (Entry<String, ByteBuffer> font : fontsBuffer.entrySet()) {
			int fontCallback = nvgCreateFontMem(vg, font.getKey(), font.getValue(), 0);
			addFallback(vg, fontCallback, "sansemoji", fallbackSansEmoji);
			addFallback(vg, fontCallback, "regularemoji", fallbackRegularEmoji);
			addFallback(vg, fontCallback, "arial", fallbackArial);
			addFallback(vg, fontCallback, "entypo", fallbackEntypo);
		}
	}

	private void addFallback(long vg, int fontCallback, String name, ByteBuffer fontData) {
		NanoVG.nvgAddFallbackFontId(vg, fontCallback, nvgCreateFontMem(vg, name, fontData, 0));
	}

	public void loadFonts() {
		loadFont(fontPath, fontNameRegular);
		loadFont(fontPath, fontNameBold);
		loadFont(fontPath, fontNameLight);
		loadFont(fontPath, fontNameItalic);
	}

	private void loadFont(String fontPath, String loadName) {
		if (loadName == null)
			return;
		String path = fontPath + loadName;
		try {
			ByteBuffer buf = ioResourceToByteBuffer(path, 1024 * 1024);
			fontsBuffer.put(loadName, buf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFont() {
		return getFont(FontStyle.REGULAR);
	}

	/**
	 * Gets the font with the given style. If the font hasn't been loaded yet, this
	 * function will do so.
	 * 
	 * @param style
	 * @return
	 */
	public String getFont(FontStyle style) {
		switch (style) {
		case BOLD:
			return fontNameBold;
		case ITALIC:
			return fontNameItalic;
		case LIGHT:
			return fontNameLight;
		case REGULAR:
			return fontNameRegular;
		}
		return "";
	}

	public float[] getTextBounds(Context context, String string, FontStyle style, double size, float[] bounds) {
		if (context == null) {
			return bounds;
		}

		String font = getFont(style);

		if (font == null) {
			return bounds;
		}

		NanoVG.nvgFontSize(context.getNVG(), (float) size);
		NanoVG.nvgFontFace(context.getNVG(), font);
		NanoVG.nvgTextAlign(context.getNVG(), NanoVG.NVG_ALIGN_LEFT | NanoVG.NVG_ALIGN_TOP);

		if (string != null) {
			NanoVG.nvgTextBounds(context.getNVG(), 0, 0, string, bounds);
		}

		return bounds;
	}

	public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
		ByteBuffer buffer;

		File file = new File(resource);
		if (file.isFile()) {
			FileInputStream fis = new FileInputStream(file);
			FileChannel fc = fis.getChannel();

			buffer = memAlloc((int) fc.size() + 1);

			while (fc.read(buffer) != -1)
				;

			fis.close();
			fc.close();
		} else {
			int size = 0;
			buffer = memAlloc(bufferSize);
			try (InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
				if (source == null)
					throw new FileNotFoundException(resource);
				try (ReadableByteChannel rbc = Channels.newChannel(source)) {
					while (true) {
						int bytes = rbc.read(buffer);
						if (bytes == -1)
							break;
						size += bytes;
						if (!buffer.hasRemaining())
							buffer = memRealloc(buffer, size * 2);
					}
				}
			}
			buffer = memRealloc(buffer, size + 1);
		}
		buffer.put((byte) 0);
		buffer.flip();
		return buffer;
	}

}