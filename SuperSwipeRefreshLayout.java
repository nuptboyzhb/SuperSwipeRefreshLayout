package net.mobctrl.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

/**
 * @Author Zheng Haibo
 * @PersonalWebsite http://www.mobctrl.net
 * @Description 自定义CustomeSwipeRefreshLayout<br>
 *              1.非侵入式下拉刷<br>
 *              2.支持RecyclerView<br>
 *              3.实时回调下拉的距离<br>
 */
@SuppressLint("ClickableViewAccessibility")
public class SuperSwipeRefreshLayout extends ViewGroup {
	private static final String LOG_TAG = "CustomeSwipeRefreshLayout";
	private static final int HEADER_VIEW_HEIGHT = 50;// HeaderView height (dp)

	private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
	private static final int INVALID_POINTER = -1;
	private static final float DRAG_RATE = .5f;

	private static final int SCALE_DOWN_DURATION = 150;
	private static final int ANIMATE_TO_TRIGGER_DURATION = 200;
	private static final int ANIMATE_TO_START_DURATION = 200;
	private static final int DEFAULT_CIRCLE_TARGET = 64;

	// SuperSwipeRefreshLayout内的目标View，比如RecyclerView,ListView,ScrollView,GridView
	// etc.
	private View mTarget;

	private OnPullRefreshListener mListener;

	private boolean mRefreshing = false;
	private int mTouchSlop;
	private float mTotalDragDistance = -1;
	private int mMediumAnimationDuration;
	private int mCurrentTargetOffsetTop;
	private boolean mOriginalOffsetCalculated = false;

	private float mInitialMotionY;
	private boolean mIsBeingDragged;
	private int mActivePointerId = INVALID_POINTER;
	private boolean mScale;

	private boolean mReturningToStart;
	private final DecelerateInterpolator mDecelerateInterpolator;
	private static final int[] LAYOUT_ATTRS = new int[] { android.R.attr.enabled };

	private HeadViewContainer mHeadViewContainer;
	private int mHeaderViewIndex = -1;

	protected int mFrom;

	private float mStartingScale;

	protected int mOriginalOffsetTop;

	private Animation mScaleAnimation;

	private Animation mScaleDownAnimation;

	private Animation mScaleDownToStartAnimation;

	// 最后停顿时的偏移量px，与DEFAULT_CIRCLE_TARGET正比
	private float mSpinnerFinalOffset;

	private boolean mNotify;

	private int mHeaderViewWidth;// headerView的高度

	private int mHeaderViewHeight;

	private boolean mUsingCustomStart;

	private boolean targetScrollWithLayout = true;

	private Animation.AnimationListener mRefreshListener = new Animation.AnimationListener() {
		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if (mRefreshing) {
				if (mNotify) {
					if (mListener != null) {
						mListener.onRefresh();
					}
				}
			} else {
				mHeadViewContainer.setVisibility(View.GONE);
				if (mScale) {
					setAnimationProgress(0);
				} else {
					setTargetOffsetTopAndBottom(mOriginalOffsetTop
							- mCurrentTargetOffsetTop, true);
				}
			}
			mCurrentTargetOffsetTop = mHeadViewContainer.getTop();
			updateListenerCallBack();
		}
	};

	/**
	 * 更新回调
	 */
	private void updateListenerCallBack() {
		int distance = mCurrentTargetOffsetTop + mHeadViewContainer.getHeight();
		if (mListener != null) {
			mListener.onPullDistance(distance);
		}
	}

	/**
	 * 添加头布局
	 * 
	 * @param child
	 */
	public void setHeaderView(View child) {
		if (child == null) {
			return;
		}
		if (mHeadViewContainer == null) {
			return;
		}
		mHeadViewContainer.removeAllViews();
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				mHeaderViewWidth, mHeaderViewHeight);
		mHeadViewContainer.addView(child, layoutParams);
	}

	public SuperSwipeRefreshLayout(Context context) {
		this(context, null);
	}

	@SuppressWarnings("deprecation")
	public SuperSwipeRefreshLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		/**
		 * getScaledTouchSlop是一个距离，表示滑动的时候，手的移动要大于这个距离才开始移动控件。如果小于这个距离就不触发移动控件
		 */
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

		mMediumAnimationDuration = getResources().getInteger(
				android.R.integer.config_mediumAnimTime);

		setWillNotDraw(false);
		mDecelerateInterpolator = new DecelerateInterpolator(
				DECELERATE_INTERPOLATION_FACTOR);

		final TypedArray a = context
				.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
		setEnabled(a.getBoolean(0, true));
		a.recycle();

		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		final DisplayMetrics metrics = getResources().getDisplayMetrics();
		mHeaderViewWidth = (int) display.getWidth();
		mHeaderViewHeight = (int) (HEADER_VIEW_HEIGHT * metrics.density);
		createHeaderViewContainer();
		ViewCompat.setChildrenDrawingOrderEnabled(this, true);
		mSpinnerFinalOffset = DEFAULT_CIRCLE_TARGET * metrics.density;
		mTotalDragDistance = mSpinnerFinalOffset;
	}

	protected int getChildDrawingOrder(int childCount, int i) {
		if (mHeaderViewIndex < 0) {
			return i;
		} else if (i == childCount - 1) {
			return mHeaderViewIndex;
		} else if (i >= mHeaderViewIndex) {
			return i + 1;
		} else {
			return i;
		}
	}

	/**
	 * 创建头布局的容器
	 */
	private void createHeaderViewContainer() {
		mHeadViewContainer = new HeadViewContainer(getContext());
		mHeadViewContainer.setVisibility(View.GONE);
		addView(mHeadViewContainer);
	}

	public void setOnPullRefreshListener(OnPullRefreshListener listener) {
		mListener = listener;
	}

	/**
	 * Notify the widget that refresh state has changed. Do not call this when
	 * refresh is triggered by a swipe gesture.
	 *
	 * @param refreshing
	 *            Whether or not the view should show refresh progress.
	 */
	public void setRefreshing(boolean refreshing) {
		if (refreshing && mRefreshing != refreshing) {
			// scale and show
			mRefreshing = refreshing;
			int endTarget = 0;
			if (!mUsingCustomStart) {
				endTarget = (int) (mSpinnerFinalOffset + mOriginalOffsetTop);
			} else {
				endTarget = (int) mSpinnerFinalOffset;
			}
			setTargetOffsetTopAndBottom(endTarget - mCurrentTargetOffsetTop,
					true /* requires update */);
			mNotify = false;
			startScaleUpAnimation(mRefreshListener);
		} else {
			setRefreshing(refreshing, false /* notify */);
		}
	}

	private void startScaleUpAnimation(AnimationListener listener) {
		mHeadViewContainer.setVisibility(View.VISIBLE);
		mScaleAnimation = new Animation() {
			@Override
			public void applyTransformation(float interpolatedTime,
					Transformation t) {
				setAnimationProgress(interpolatedTime);
			}
		};
		mScaleAnimation.setDuration(mMediumAnimationDuration);
		if (listener != null) {
			mHeadViewContainer.setAnimationListener(listener);
		}
		mHeadViewContainer.clearAnimation();
		mHeadViewContainer.startAnimation(mScaleAnimation);
	}

	private void setAnimationProgress(float progress) {
		ViewCompat.setScaleX(mHeadViewContainer, progress);
		ViewCompat.setScaleY(mHeadViewContainer, progress);
	}

	private void setRefreshing(boolean refreshing, final boolean notify) {
		if (mRefreshing != refreshing) {
			mNotify = notify;
			ensureTarget();
			mRefreshing = refreshing;
			if (mRefreshing) {
				animateOffsetToCorrectPosition(mCurrentTargetOffsetTop,
						mRefreshListener);
			} else {
				startScaleDownAnimation(mRefreshListener);
			}
		}
	}

	private void startScaleDownAnimation(Animation.AnimationListener listener) {
		mScaleDownAnimation = new Animation() {
			@Override
			public void applyTransformation(float interpolatedTime,
					Transformation t) {
				setAnimationProgress(1 - interpolatedTime);
			}
		};
		mScaleDownAnimation.setDuration(SCALE_DOWN_DURATION);
		mHeadViewContainer.setAnimationListener(listener);
		mHeadViewContainer.clearAnimation();
		mHeadViewContainer.startAnimation(mScaleDownAnimation);
	}

	public boolean isRefreshing() {
		return mRefreshing;
	}

	/**
	 * 确保mTarget不为空<br>
	 * mTarget一般是可滑动的ScrollView,ListView,RecyclerView等
	 */
	private void ensureTarget() {
		if (mTarget == null) {
			for (int i = 0; i < getChildCount(); i++) {
				View child = getChildAt(i);
				if (!child.equals(mHeadViewContainer)) {
					mTarget = child;
					break;
				}
			}
		}
	}

	/**
	 * Set the distance to trigger a sync in dips
	 *
	 * @param distance
	 */
	public void setDistanceToTriggerSync(int distance) {
		mTotalDragDistance = distance;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		final int width = getMeasuredWidth();
		final int height = getMeasuredHeight();
		if (getChildCount() == 0) {
			return;
		}
		if (mTarget == null) {
			ensureTarget();
		}
		if (mTarget == null) {
			return;
		}
		int distance = mCurrentTargetOffsetTop + mHeadViewContainer.getHeight();
		if (!targetScrollWithLayout) {
			// 判断标志位，如果目标View不跟随手指的滑动而滑动，将下拉偏移量设置为0
			distance = 0;
		}
		final View child = mTarget;
		final int childLeft = getPaddingLeft();
		final int childTop = getPaddingTop() + distance;// 根据偏移量distance更新
		final int childWidth = width - getPaddingLeft() - getPaddingRight();
		final int childHeight = height - getPaddingTop() - getPaddingBottom();
		child.layout(childLeft, childTop, childLeft + childWidth, childTop
				+ childHeight);// 更新目标View的位置
		int headViewWidth = mHeadViewContainer.getMeasuredWidth();
		int headViewHeight = mHeadViewContainer.getMeasuredHeight();
		mHeadViewContainer.layout((width / 2 - headViewWidth / 2),
				mCurrentTargetOffsetTop, (width / 2 + headViewWidth / 2),
				mCurrentTargetOffsetTop + headViewHeight);// 更新头布局的位置
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mTarget == null) {
			ensureTarget();
		}
		if (mTarget == null) {
			return;
		}
		mTarget.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth()
				- getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
				MeasureSpec.makeMeasureSpec(getMeasuredHeight()
						- getPaddingTop() - getPaddingBottom(),
						MeasureSpec.EXACTLY));
		mHeadViewContainer.measure(MeasureSpec.makeMeasureSpec(
				mHeaderViewWidth, MeasureSpec.EXACTLY), MeasureSpec
				.makeMeasureSpec(mHeaderViewHeight, MeasureSpec.EXACTLY));
		if (!mUsingCustomStart && !mOriginalOffsetCalculated) {
			mOriginalOffsetCalculated = true;
			mCurrentTargetOffsetTop = mOriginalOffsetTop = -mHeadViewContainer
					.getMeasuredHeight();
			updateListenerCallBack();
		}
		mHeaderViewIndex = -1;
		for (int index = 0; index < getChildCount(); index++) {
			if (getChildAt(index) == mHeadViewContainer) {
				mHeaderViewIndex = index;
				break;
			}
		}
	}

	/**
	 * 判断目标View是否滑动到顶部-还能否继续滑动
	 * 
	 * @return
	 */
	public boolean canChildScrollUp() {
		if (android.os.Build.VERSION.SDK_INT < 14) {
			if (mTarget instanceof AbsListView) {
				final AbsListView absListView = (AbsListView) mTarget;
				return absListView.getChildCount() > 0
						&& (absListView.getFirstVisiblePosition() > 0 || absListView
								.getChildAt(0).getTop() < absListView
								.getPaddingTop());
			} else {
				return mTarget.getScrollY() > 0;
			}
		} else {
			return ViewCompat.canScrollVertically(mTarget, -1);
		}
	}

	/**
	 * 主要判断是否应该拦截子View的事件<br>
	 * 如果拦截，则交给自己的OnTouchEvent处理<br>
	 * 否者，交给子View处理<br>
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		ensureTarget();

		final int action = MotionEventCompat.getActionMasked(ev);

		if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
			mReturningToStart = false;
		}

		if (!isEnabled() || mReturningToStart || canChildScrollUp()
				|| mRefreshing) {
			// 如果子View可以滑动，不拦截事件，交给子View处理
			return false;
		}

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			setTargetOffsetTopAndBottom(
					mOriginalOffsetTop - mHeadViewContainer.getTop(), true);// 恢复HeaderView的初始位置
			mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
			mIsBeingDragged = false;
			final float initialMotionY = getMotionEventY(ev, mActivePointerId);
			if (initialMotionY == -1) {
				return false;
			}
			mInitialMotionY = initialMotionY;// 记录按下的位置

		case MotionEvent.ACTION_MOVE:
			if (mActivePointerId == INVALID_POINTER) {
				Log.e(LOG_TAG,
						"Got ACTION_MOVE event but don't have an active pointer id.");
				return false;
			}

			final float y = getMotionEventY(ev, mActivePointerId);
			if (y == -1) {
				return false;
			}
			final float yDiff = y - mInitialMotionY;// 计算下拉距离
			if (yDiff > mTouchSlop && !mIsBeingDragged) {// 判断是否下拉的距离足够
				mIsBeingDragged = true;// 正在下拉
			}
			break;

		case MotionEventCompat.ACTION_POINTER_UP:
			onSecondaryPointerUp(ev);
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			mIsBeingDragged = false;
			mActivePointerId = INVALID_POINTER;
			break;
		}

		return mIsBeingDragged;// 如果正在拖动，则拦截子View的事件
	}

	private float getMotionEventY(MotionEvent ev, int activePointerId) {
		final int index = MotionEventCompat.findPointerIndex(ev,
				activePointerId);
		if (index < 0) {
			return -1;
		}
		return MotionEventCompat.getY(ev, index);
	}

	@Override
	public void requestDisallowInterceptTouchEvent(boolean b) {
		// Nope.
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = MotionEventCompat.getActionMasked(ev);

		if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
			mReturningToStart = false;
		}

		if (!isEnabled() || mReturningToStart || canChildScrollUp()) {
			// 如果子View可以滑动，不拦截事件，交给子View处理
			return false;
		}

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
			mIsBeingDragged = false;
			break;

		case MotionEvent.ACTION_MOVE: {
			final int pointerIndex = MotionEventCompat.findPointerIndex(ev,
					mActivePointerId);
			if (pointerIndex < 0) {
				Log.e(LOG_TAG,
						"Got ACTION_MOVE event but have an invalid active pointer id.");
				return false;
			}

			final float y = MotionEventCompat.getY(ev, pointerIndex);
			final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
			if (mIsBeingDragged) {
				float originalDragPercent = overscrollTop / mTotalDragDistance;
				if (originalDragPercent < 0) {
					return false;
				}
				float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
				float extraOS = Math.abs(overscrollTop) - mTotalDragDistance;
				float slingshotDist = mUsingCustomStart ? mSpinnerFinalOffset
						- mOriginalOffsetTop : mSpinnerFinalOffset;
				float tensionSlingshotPercent = Math.max(0,
						Math.min(extraOS, slingshotDist * 2) / slingshotDist);
				float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math
						.pow((tensionSlingshotPercent / 4), 2)) * 2f;
				float extraMove = (slingshotDist) * tensionPercent * 2;

				int targetY = mOriginalOffsetTop
						+ (int) ((slingshotDist * dragPercent) + extraMove);
				if (mHeadViewContainer.getVisibility() != View.VISIBLE) {
					mHeadViewContainer.setVisibility(View.VISIBLE);
				}
				if (!mScale) {
					ViewCompat.setScaleX(mHeadViewContainer, 1f);
					ViewCompat.setScaleY(mHeadViewContainer, 1f);
				}
				if (overscrollTop < mTotalDragDistance) {
					if (mScale) {
						setAnimationProgress(overscrollTop / mTotalDragDistance);
					}
					mListener.onPullEnable(false);
				} else {
					mListener.onPullEnable(true);
				}
				setTargetOffsetTopAndBottom(targetY - mCurrentTargetOffsetTop,
						true);
			}
			break;
		}
		case MotionEventCompat.ACTION_POINTER_DOWN: {
			final int index = MotionEventCompat.getActionIndex(ev);
			mActivePointerId = MotionEventCompat.getPointerId(ev, index);
			break;
		}

		case MotionEventCompat.ACTION_POINTER_UP:
			onSecondaryPointerUp(ev);
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL: {
			if (mActivePointerId == INVALID_POINTER) {
				if (action == MotionEvent.ACTION_UP) {
					Log.e(LOG_TAG,
							"Got ACTION_UP event but don't have an active pointer id.");
				}
				return false;
			}
			final int pointerIndex = MotionEventCompat.findPointerIndex(ev,
					mActivePointerId);
			final float y = MotionEventCompat.getY(ev, pointerIndex);
			final float overscrollTop = (y - mInitialMotionY) * DRAG_RATE;
			mIsBeingDragged = false;
			if (overscrollTop > mTotalDragDistance) {
				setRefreshing(true, true /* notify */);
			} else {
				mRefreshing = false;
				Animation.AnimationListener listener = null;
				if (!mScale) {
					listener = new Animation.AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {
						}

						@Override
						public void onAnimationEnd(Animation animation) {
							if (!mScale) {
								startScaleDownAnimation(null);
							}
						}

						@Override
						public void onAnimationRepeat(Animation animation) {
						}

					};
				}
				animateOffsetToStartPosition(mCurrentTargetOffsetTop, listener);
			}
			mActivePointerId = INVALID_POINTER;
			return false;
		}
		}

		return true;
	}

	private void animateOffsetToCorrectPosition(int from,
			AnimationListener listener) {
		mFrom = from;
		mAnimateToCorrectPosition.reset();
		mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
		mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
		if (listener != null) {
			mHeadViewContainer.setAnimationListener(listener);
		}
		mHeadViewContainer.clearAnimation();
		mHeadViewContainer.startAnimation(mAnimateToCorrectPosition);
	}

	private void animateOffsetToStartPosition(int from,
			AnimationListener listener) {
		if (mScale) {
			startScaleDownReturnToStartAnimation(from, listener);
		} else {
			mFrom = from;
			mAnimateToStartPosition.reset();
			mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
			mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
			if (listener != null) {
				mHeadViewContainer.setAnimationListener(listener);
			}
			mHeadViewContainer.clearAnimation();
			mHeadViewContainer.startAnimation(mAnimateToStartPosition);
		}
	}

	private final Animation mAnimateToCorrectPosition = new Animation() {
		@Override
		public void applyTransformation(float interpolatedTime, Transformation t) {
			int targetTop = 0;
			int endTarget = 0;
			if (!mUsingCustomStart) {
				endTarget = (int) (mSpinnerFinalOffset - Math
						.abs(mOriginalOffsetTop));
			} else {
				endTarget = (int) mSpinnerFinalOffset;
			}
			targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
			int offset = targetTop - mHeadViewContainer.getTop();
			setTargetOffsetTopAndBottom(offset, false /* requires update */);
		}
	};

	private void moveToStart(float interpolatedTime) {
		int targetTop = 0;
		targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
		int offset = targetTop - mHeadViewContainer.getTop();
		setTargetOffsetTopAndBottom(offset, false /* requires update */);
	}

	private final Animation mAnimateToStartPosition = new Animation() {
		@Override
		public void applyTransformation(float interpolatedTime, Transformation t) {
			moveToStart(interpolatedTime);
		}
	};

	private void startScaleDownReturnToStartAnimation(int from,
			Animation.AnimationListener listener) {
		mFrom = from;
		mStartingScale = ViewCompat.getScaleX(mHeadViewContainer);
		mScaleDownToStartAnimation = new Animation() {
			@Override
			public void applyTransformation(float interpolatedTime,
					Transformation t) {
				float targetScale = (mStartingScale + (-mStartingScale * interpolatedTime));
				setAnimationProgress(targetScale);
				moveToStart(interpolatedTime);
			}
		};
		mScaleDownToStartAnimation.setDuration(SCALE_DOWN_DURATION);
		if (listener != null) {
			mHeadViewContainer.setAnimationListener(listener);
		}
		mHeadViewContainer.clearAnimation();
		mHeadViewContainer.startAnimation(mScaleDownToStartAnimation);
	}

	private void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
		mHeadViewContainer.bringToFront();
		mHeadViewContainer.offsetTopAndBottom(offset);
		mCurrentTargetOffsetTop = mHeadViewContainer.getTop();
		if (requiresUpdate && android.os.Build.VERSION.SDK_INT < 11) {
			invalidate();
		}
		updateListenerCallBack();
	}

	private void onSecondaryPointerUp(MotionEvent ev) {
		final int pointerIndex = MotionEventCompat.getActionIndex(ev);
		final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
		if (pointerId == mActivePointerId) {
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mActivePointerId = MotionEventCompat.getPointerId(ev,
					newPointerIndex);
		}
	}

	/**
	 * @Description 下拉刷新布局头部的容器
	 */
	private class HeadViewContainer extends RelativeLayout {

		private Animation.AnimationListener mListener;

		public HeadViewContainer(Context context) {
			super(context);
		}

		public void setAnimationListener(Animation.AnimationListener listener) {
			mListener = listener;
		}

		@Override
		public void onAnimationStart() {
			super.onAnimationStart();
			if (mListener != null) {
				mListener.onAnimationStart(getAnimation());
			}
		}

		@Override
		public void onAnimationEnd() {
			super.onAnimationEnd();
			if (mListener != null) {
				mListener.onAnimationEnd(getAnimation());
			}
		}
	}

	/**
	 * 判断子View是否跟随手指的滑动而滑动，默认跟随
	 * 
	 * @return
	 */
	public boolean isTargetScrollWithLayout() {
		return targetScrollWithLayout;
	}

	/**
	 * 设置子View是否跟谁手指的滑动而滑动
	 * 
	 * @param targetScrollWithLayout
	 */
	public void setTargetScrollWithLayout(boolean targetScrollWithLayout) {
		this.targetScrollWithLayout = targetScrollWithLayout;
	}

	public interface OnPullRefreshListener {
		public void onRefresh();

		public void onPullDistance(int distance);

		public void onPullEnable(boolean enable);
	}

}
