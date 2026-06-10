package com.example.crumbsofcomfort.User.Helper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class DrawableUtils {

    public static Bitmap createInitialsBitmap(String initials, int size, int bgColor, int textColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Draw circular background
        paint.setColor(bgColor);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        // Draw initials
        paint.setColor(textColor);
        paint.setTextSize(size / 2.5f);
        paint.setTextAlign(Paint.Align.CENTER);
        Rect bounds = new Rect();
        paint.getTextBounds(initials, 0, initials.length(), bounds);

        float x = size / 2f;
        float y = size / 2f - bounds.exactCenterY();

        canvas.drawText(initials, x, y, paint);
        return bitmap;
    }
}
