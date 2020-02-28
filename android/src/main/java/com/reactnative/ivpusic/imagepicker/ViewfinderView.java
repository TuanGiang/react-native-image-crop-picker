package com.reactnative.ivpusic.imagepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 */
public final class ViewfinderView extends View {


    private final Paint paint;
    private final int maskColor;


    private final Paint textPaint;

    private final String message;
    private final String messageLeveled;

    private final float textSize;

    private int left, top, right, bottom;

    public Rect getRect() {
        return new Rect(left, top, right, bottom);
    }


    // This constructor is used when the class is built from an XML resource.
    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Initialize these once for performance rather than calling them every time in onDraw().

        textSize = this.getContext().getResources().getDimension(R.dimen.text_size);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Resources resources = getResources();
        maskColor = resources.getColor(R.color.viewfinder_mask);

        textPaint = new Paint();
        textPaint.setColor(this.getContext().getResources().getColor(R.color.viewfinder_text_color));
        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        message = this.getContext().getString(R.string.capture_image_message);
        messageLeveled = this.getContext().getString(R.string.capture_image_message_leveled);

    }

    @SuppressLint("DrawAllocation")
    @Override
    public void onDraw(Canvas canvas) {

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if (this.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            left = 0;
            top = height / 3;
            right = width;
            bottom = (height * 2) / 3;
        } else {
            left = width / 4;
            top = height / 5;
            right = (width * 3) / 4;
            bottom = (height * 4) / 5;
        }

        Rect frame = new Rect(left, top, right, bottom);

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.setColor(maskColor);
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);

        drawMessage(message, messageLeveled, canvas);

        super.onDraw(canvas);

    }

    private void drawMessage(String message, String messageLeveled, Canvas canvas) {
        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((top / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));
        //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.

        canvas.drawText(message, xPos, yPos, textPaint);

        canvas.drawText(messageLeveled, xPos, (float) (yPos + textSize + 0.1 * textSize), textPaint);
    }


}