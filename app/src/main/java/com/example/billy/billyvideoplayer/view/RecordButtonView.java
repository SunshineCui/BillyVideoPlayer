package com.example.billy.billyvideoplayer.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.billy.billyvideoplayer.R;

import java.lang.ref.WeakReference;


/**
 * Created by Billy_Cui on 2018/11/13.
 * Describe:
 */

public class RecordButtonView extends View {

    private static final int WHAT_LONG_CLICK = 1;
    private long longClickTime = 500;//长按最短时间(毫秒)
    private long maxTime = 5 * 1000;//录制最大时间
    private long minTime = 3 * 1000;//录制最短时间
    private long startTime;
    private long endTime;

    private int progressColor;//进度条颜色
    private Context context;

    private Paint outPaint; //外圆画笔
    private Paint insidePaint;
    private Paint progressPaint;

    private int height;
    private int width;
    private float initOutRedius; //外圆初始化半径
    private float initInsideRedius;
    private float outRedius;
    private float insideRedius;
    private float progressWidth = 18f;//圆环宽度
    private float currentProgress;//当前进度

    private boolean isRecording;//录制状态
    private boolean isPressed;//屏幕按压状态
    private boolean isMaxTime;//达到最大录制时间
    private ValueAnimator progressAnimator;//进度条动画
    private static ViewHandler handler;


    public RecordButtonView(Context context) {
        super(context);
        init(context, null);
    }

    public RecordButtonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RecordButtonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;
        handler = new ViewHandler(this);
        //获取自定义属性值
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CircleButtonView);
        minTime = array.getInteger(R.styleable.CircleButtonView_minTime, 0);
        maxTime = array.getInteger(R.styleable.CircleButtonView_maxTime, 10 * 1000);
        progressWidth = array.getDimension(R.styleable.CircleButtonView_progressWidth, 12f);
        progressColor = array.getColor(R.styleable.CircleButtonView_progressColor, Color.parseColor("#6ABF66"));
        array.recycle();
        //初始化画笔
        outPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outPaint.setColor(Color.parseColor("#dddddd"));

        insidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        insidePaint.setColor(Color.parseColor("#ffffff"));

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(progressColor);

        //设置动画
        progressAnimator = ValueAnimator.ofFloat(0, 360f);
        progressAnimator.setDuration(maxTime);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //初始化 宽 高 半径
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        initOutRedius = outRedius = width / 2 * 0.75f;
        initInsideRedius = insideRedius = outRedius * 0.75f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制外圆
        canvas.drawCircle(width / 2, height / 2, outRedius, outPaint);
        //绘制内圆
        canvas.drawCircle(width / 2, height / 2, insideRedius, insidePaint);
        //录制的过程中绘制进度条
        if (isRecording) {
            drawProgress(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPressed = true;
                startTime = System.currentTimeMillis();
                Message message = Message.obtain();
                message.what = WHAT_LONG_CLICK;
                handler.sendMessageDelayed(message,longClickTime);
                break;
            case MotionEvent.ACTION_UP:
                isPressed = false;
                isRecording = false;
                endTime = System.currentTimeMillis();
                if (endTime - startTime < longClickTime){
                    handler.removeMessages(WHAT_LONG_CLICK);
                    //可以添加点击事件 onClickListener.onClick();
                }else {
                    startAnimation(outRedius,initOutRedius,insideRedius,initInsideRedius);
                    if (progressAnimator!=null&&progressAnimator.getCurrentPlayTime()/1000<minTime&&!isMaxTime){
                        if (onLongClickListener!=null){
                            onLongClickListener.onNoMinRecord((int) minTime);
                        }
                        progressAnimator.cancel();
                    }else {
                        //录制完成
                        if(onLongClickListener != null && !isMaxTime){
                            onLongClickListener.onRecordFinishedListener();
                        }
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 绘制圆形进度
     *
     * @param canvas
     */
    private void drawProgress(Canvas canvas) {
        progressPaint.setStrokeWidth(progressWidth);
        progressPaint.setStyle(Paint.Style.STROKE);
        //用于定义的圆弧的形状和大小的界限
        RectF oval = new RectF(width / 2 - (outRedius - progressWidth / 2),
                height / 2 - (outRedius - progressWidth / 2),
                width / 2 + (outRedius - progressWidth / 2),
                height / 2 + (outRedius - progressWidth / 2));
        //根据进度画圆弧
        canvas.drawArc(oval, -90, currentProgress, false, progressPaint);
    }

    /**
     * 开启动画
     *
     * @param outStart
     * @param outEnd
     * @param insideStart
     * @param insideEnd
     */
    private void startAnimation(float outStart, float outEnd, float insideStart, float insideEnd) {
        ValueAnimator outAnimator = ValueAnimator.ofFloat(outStart, outEnd);
        outAnimator.setDuration(150);
        outAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                outRedius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        ValueAnimator insideAnimator = ValueAnimator.ofFloat(insideStart, insideEnd);
        insideAnimator.setDuration(150);
        insideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                insideRedius = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        outAnimator.start();
        insideAnimator.start();

        insideAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isRecording = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //开始绘制圆形进度条
                if (isPressed) {
                    isRecording = true;
                    isMaxTime = false;
                    startProgressAnimation();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

    }

    /**
     * 进度条 变化动画
     */
    private void startProgressAnimation() {
        progressAnimator.start();
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentProgress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        progressAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //录制动画结束时,即为录制完成
                if (onLongClickListener != null && isPressed) {
                    isPressed = false;
                    isMaxTime = true;
                    onLongClickListener.onRecordFinishedListener();
                    startAnimation(outRedius, initOutRedius, insideRedius, initInsideRedius);
                    //隐藏进度条进度
                    currentProgress = 0;
                    invalidate();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private static class ViewHandler extends Handler {

        private WeakReference<RecordButtonView> wr;

        public ViewHandler(RecordButtonView view) {
            wr = new WeakReference<RecordButtonView>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT_LONG_CLICK:
                    RecordButtonView view = wr.get();
                    if (view == null) break;
                    view.onLongClick();
            }
        }
    }

    private void onLongClick() {
        //长按事件触发
        if (onLongClickListener != null) {
            onLongClickListener.onLongClick();
        }
        //内外圆动画，内圆缩小，外圆放大
        startAnimation(outRedius, outRedius * 1.33f, insideRedius, insideRedius * 0.7f);
    }

    /**
     * 长按监听器
     */
    public interface OnLongClickListener {
        void onLongClick();

        //未达到最小录制时间
        void onNoMinRecord(int currentTime);

        //录制完成
        void onRecordFinishedListener();
    }

    public OnLongClickListener onLongClickListener;

    public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

}
