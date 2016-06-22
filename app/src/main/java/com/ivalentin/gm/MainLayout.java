package com.ivalentin.gm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

/**
 * Extension of LinearLayout to be used in the app. Contains the slider menu.
 * 
 * @author Iñigo Valentin
 *
 */
public class MainLayout extends LinearLayout {

	//The width of the view
	private int mainLayoutWidth;
	
	//The menu view
	private View menu;
	
	//The content view
	private View content;
	
	//The margin of the menu
	private static int menuRightMargin = 25;

	/**
	 * Possible states of the menu
	 */
	private enum MenuState {
		HIDING, HIDDEN, SHOWING, SHOWN,
	}

	private int contentXOffset;
	private MenuState currentMenuState = MenuState.HIDDEN;
	private Scroller menuScroller = new Scroller(this.getContext(), new EaseInInterpolator());
	private Runnable menuRunnable = new MenuRunnable();
	private Handler menuHandler = new Handler();
	private int prevX = 0;
	private boolean isDragging = false;
	private int lastDiffX = 0;

	/**
	 * Constructor.
	 * 
	 * @param context Context of the app.
	 * @param attrs Atributes.
	 */
	public MainLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Constructor.
	 * 
	 * @param context Context of the app.
	 */
	public MainLayout(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		mainLayoutWidth = MeasureSpec.getSize(widthMeasureSpec);
		menuRightMargin = mainLayoutWidth * 25 / 100;
	}
	
	@SuppressLint("ClickableViewAccessibility") //I don't need it.
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		menu = this.getChildAt(0);
		content = this.getChildAt(1);
		content.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return MainLayout.this.onContentTouch(event);
			}
		});
		menu.setVisibility(View.GONE);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (changed) {
			LayoutParams contentLayoutParams = (LayoutParams) content.getLayoutParams();
			contentLayoutParams.height = this.getHeight();
			contentLayoutParams.width = this.getWidth();
			LayoutParams menuLayoutParams = (LayoutParams) menu.getLayoutParams();
			menuLayoutParams.height = this.getHeight();
			menuLayoutParams.width = this.getWidth() - menuRightMargin;
		}
		this.requestDisallowInterceptTouchEvent(true);
		menu.layout(left, top, right - menuRightMargin, bottom);
		content.layout(left + contentXOffset, top, right + contentXOffset, bottom);

	}

	/**
	 * Changes the menu state. 
	 * Closes it if it's open. Opens it if it's closed.
	 */
	public void toggleMenu() {
		if (currentMenuState == MenuState.HIDING || currentMenuState == MenuState.SHOWING)
			return;
		
		switch (currentMenuState) {
			case HIDDEN:
				currentMenuState = MenuState.SHOWING;
				menu.setVisibility(View.VISIBLE);
				menuScroller.startScroll(0, 0, menu.getLayoutParams().width, 0, GM.MENU_SLIDING_DURATION);
				break;
			case SHOWN:
				currentMenuState = MenuState.HIDING;
				menuScroller.startScroll(contentXOffset, 0, -contentXOffset, 0, GM.MENU_SLIDING_DURATION);
				break;
			default:
				break;
		}
		menuHandler.postDelayed(menuRunnable, GM.MENU_QUERY_INTERVAL);
		this.invalidate();
	}
	
	/**
	 * Handles the animation of the sliding menu.
	 * 
	 * @author seavenois
	 * 
	 * @see Runnable
	 *
	 */
	protected class MenuRunnable implements Runnable {
		@Override
		public void run() {
			boolean isScrolling = menuScroller.computeScrollOffset();
			adjustContentPosition(isScrolling);
		}
	}
		
	/**
	 * Moves the content view when the menu is slided. 
	 * 
	 * @param isScrolling Indicates if the menu is being slided.
	 */
	private void adjustContentPosition(boolean isScrolling) {
		int scrollerXOffset = menuScroller.getCurrX();
		
		content.offsetLeftAndRight(scrollerXOffset - contentXOffset);
		
		contentXOffset = scrollerXOffset;
		this.invalidate();
		if (isScrolling)
			menuHandler.postDelayed(menuRunnable, GM.MENU_QUERY_INTERVAL);
		else
			this.onMenuSlidingComplete();
	}
	
	/**
	 * Called when the menu has been slided completely. 
	 * Fixes the position.
	 */
	private void onMenuSlidingComplete() {
		switch (currentMenuState) {
			case SHOWING:
				currentMenuState = MenuState.SHOWN;
				break;
			case HIDING:
				currentMenuState = MenuState.HIDDEN;
				menu.setVisibility(View.GONE);
				break;
		}
	}
	
	/**
	 * Defines the menu animation frame rate.
	 * 
	 * @author Iñigo Valentin
	 *
	 */
	protected class EaseInInterpolator implements Interpolator {
		@Override
		public float getInterpolation(float t) {
			return (float) Math.pow(t - 1, 5) + 1;
		}
	
	}

	/**
	 * Register touch events. 
	 * Used to show and hide the menu.
	 * 
	 * @param event The MotionEvent triggering it.
	 * @return True if the menu is fully opened or closed and a touch event is happening
	 */
	public boolean onContentTouch(MotionEvent event) {
		if (currentMenuState == MenuState.HIDING || currentMenuState == MenuState.SHOWING)
			return false;
		int curX = (int) event.getRawX();
		int diffX;
		
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				prevX = curX;
				return true;
			
			case MotionEvent.ACTION_MOVE:
				//int width = this.getWidth();
				//if (event.getX() <  (float) width / 4){
					
					if (!isDragging) {
						
						isDragging = true;
						menu.setVisibility(View.VISIBLE);
					}
					diffX = curX - prevX;
					if (contentXOffset + diffX <= 0) {
						diffX = -contentXOffset;
					}
					else if (contentXOffset + diffX > mainLayoutWidth - menuRightMargin) {
						diffX = mainLayoutWidth - menuRightMargin - contentXOffset;
					}
					content.offsetLeftAndRight(diffX);
					contentXOffset += diffX;
					this.invalidate();
					
					prevX = curX;
					lastDiffX = diffX;
				//}
				return true;
			
			case MotionEvent.ACTION_UP:
				
				if (lastDiffX > 0) {
					currentMenuState = MenuState.SHOWING;
					menuScroller.startScroll(contentXOffset, 0,	menu.getLayoutParams().width - contentXOffset, 0, GM.MENU_SLIDING_DURATION);
				}
				else if (lastDiffX < 0) {
					currentMenuState = MenuState.HIDING;
					menuScroller.startScroll(contentXOffset, 0, -contentXOffset, 0, GM.MENU_SLIDING_DURATION);
				}
				menuHandler.postDelayed(menuRunnable, GM.MENU_QUERY_INTERVAL);
				this.invalidate();
				isDragging = false;
				prevX = 0;
				lastDiffX = 0;
				return true;
				
			default:
				break;
		}
		
		return false;
	}
}

