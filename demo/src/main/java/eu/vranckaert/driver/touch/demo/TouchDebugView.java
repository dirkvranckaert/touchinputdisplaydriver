package eu.vranckaert.driver.touch.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dirkvranckaert on 12/10/2017.
 */

public class TouchDebugView extends View {
    private final Paint p = new Paint();
    private final Rect r = new Rect();

    private float x = 0;
    private float y = 0;

    private final List<Long> touchDebugClicks = new ArrayList<>();
    private Toast touchDebugToast;

    public TouchDebugView(Context context) {
        super(context);
        init();
    }

    public TouchDebugView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchDebugView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TouchDebugView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = motionEvent.getX();
                float y = motionEvent.getY();

                TouchDebugView.this.x = x;
                TouchDebugView.this.y = y;
                boolean click = motionEvent.getAction() == MotionEvent.ACTION_DOWN;

                synchronized (touchDebugClicks) {
                    if (click) {
                        final int necessaryClicks = 10;
                        touchDebugClicks.add(System.currentTimeMillis());

                        if (touchDebugClicks.size() >= 4 && touchDebugClicks.size() < 10) {
                            int neededClicks = necessaryClicks - touchDebugClicks.size();
                            if (touchDebugToast != null) {
                                touchDebugToast.cancel();
                            }
                            touchDebugToast = Toast.makeText(getContext(), neededClicks == 1 ? "1 more touches to close touch testing!" : neededClicks + " more touches to close touch testing!", Toast.LENGTH_LONG);
                            touchDebugToast.show();
                        } else if (touchDebugClicks.size() == 10) {
                            touchDebugClicks.clear();
                            if (touchDebugToast != null) {
                                touchDebugToast.cancel();
                            }
                            touchDebugToast = Toast.makeText(getContext(), "Quitting touch test mode!", Toast.LENGTH_LONG);
                            touchDebugToast.show();
                            setVisibility(View.GONE);
                        }
                    }
                }

                invalidate();
                requestLayout();

                return true;
            }
        });
    }


    @Override
    protected void onDraw(Canvas canvas) {
        p.setColor(getResources().getColor(R.color.red_transparant));
        r.set(0, 0, getWidth(), getHeight());
        canvas.drawRect(r, p);

        p.setAntiAlias(true);
        p.setColor(getResources().getColor(R.color.blue));
        canvas.drawCircle(x, y, 10, p);
        p.setColor(getResources().getColor(android.R.color.white));
        canvas.drawCircle(x, y, 2, p);
    }
}
