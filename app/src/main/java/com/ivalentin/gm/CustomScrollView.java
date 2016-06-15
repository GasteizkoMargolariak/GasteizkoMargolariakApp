package com.ivalentin.gm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * An extended ScrollView. 
 * It allows to be swept from the left to open the application menu.
 * 
 * @author Iñigo Valentin
 * 
 * @see ScrollView
 *
 */
public class CustomScrollView extends ScrollView{
	
	/**
     * Constructor.
     * 
     * @param context The context of the app or Activity.
     */
	public CustomScrollView(Context context) {
		super(context);
	}
	
	/**
     * Constructor.
     * 
     * @param context The context of the app or Activity.
     * @param attrs Attributes.
     */
	public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor.
     * 
     * @param context The context of the app or Activity.
     * @param attrs Attributes.
     * @param defStyle Default style.
     */
    public CustomScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
	
	/**
	 * Overrides the default onTouchListener. 
	 * 
	 * If the motion is in the leftmost part of the screen, the event is not 
	 * passed along and the menu is shown.
	 * 
	 * @see android.widget.ScrollView#onTouchEvent(android.view.MotionEvent)
	 * 
	 * @param ev The MotionEvent triggering the action
	 * 
	 * @return True if the app handles the motion (i.e. to slide), false otherwise (normal event)
	 */
	@SuppressLint("ClickableViewAccessibility") //Disturbs the sliding process
	@Override
    public boolean onTouchEvent(MotionEvent ev) {
		int width = this.getWidth();
		super.onTouchEvent(ev);
		if (ev.getX() <  (float) width / 10){
			
			return false;
		}
		else{
			return true;
		}				
	}

}