package com.pluseq.coreader.android;

import java.util.ArrayList;
import java.util.Hashtable;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.pluseq.coreader.BookElement;
import com.pluseq.coreader.CoConfig;
import com.pluseq.coreader.CoTheme;
import com.pluseq.coreader.R;


/**
 * Describes part of settings which directly affects current FrameMapping
 * (in fact, if you change any of those, all mappings will be reset)
 * @author or
 */
public class FontSettings {
	
	public static final Integer NORMAL = new Integer(1);
	public static final Integer BOOKVICA = new Integer(2);
	public static final Integer BOOK_TITLE = new Integer(3);
	public static final Integer CHAPTER_TITLE = new Integer(4);
	public static final Integer STRONG = new Integer(5);
		
	public int currentFactor = 3;
	private int normalFontHeight = 17;
	
	protected Hashtable<Integer, Font> fonts;
	protected Hashtable<Integer, Paint> paints;
	protected CoTheme theme;
	

	public static ArrayList<String> getFactorList() {
		ArrayList<String> factors = new ArrayList<String>();
		for (int i=10; i<30; i++) {
			factors.add(String.valueOf(i) + " px");
		}
		return factors;
	}
	
	public void setCoTheme(CoTheme theme) {
		this.theme = theme;
	}
	
	public void initFonts() {
		paints = new Hashtable<Integer, Paint>();
		fonts = new Hashtable<Integer, Font>();
		fonts.put(FontSettings.NORMAL, new Font("Helvetica", Typeface.NORMAL, normalFontHeight, theme.color.get(BookElement.NORMAL)));
	}
	
	public FontSettings(CoTheme theme) {
		setCoTheme(theme);
		initFonts();
	}
	
	public Font get(Integer fontType) {
		return fonts.get(fontType);
	}
	
	public Paint getPaint(Integer fontType) {
		Paint paint = paints.get(fontType);
		if (null == paint) {
			Font font = get(fontType);
			paint = new Paint();
	        paint.setAntiAlias(true);
	        paint.setTextSize(font.textSize);
	        paint.setTypeface(Typeface.create(font.familyName, font.style));
	        paint.setColor(Color.parseColor(font.color));
	        paints.put(fontType, paint);
		}
        return paint;
	}
	
	public class Font {
		public String color = "#777";
		public int textSize = 17;
		public String familyName = "Sans Serif";
		public int style = Typeface.NORMAL;
		
		public Font(){}
		
		public Font(String familyName, int style, int textSize, String color) {
			this.familyName = familyName;
			this.style = style;
			this.textSize = textSize;
			this.color = color;
		}
	}
	
	public int getFontHeight() {
		return normalFontHeight;
	}
	public int getLineSpacing() {
		return 2;
	}
	
	public int getParagraphSpacing() {
		return 3;
	}
	
	public int getParagraphOffset() {
		return 20;
	}

	public void setFontFactor(int fontFactor) {
		currentFactor = fontFactor;
		normalFontHeight = fontFactor;		
		initFonts();
	}
}