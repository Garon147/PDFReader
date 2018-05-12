package orion.garon.pdfreader;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.github.barteksc.pdfviewer.PDFView;

/**
 * Created by VKI on 24.04.2018.
 */

public class PDFViewLockable extends PDFView {

    private boolean mScrollable = true;

    public PDFViewLockable(Context context, AttributeSet set) {

        super(context, set);
    }



    public void setScrollingEnabled(boolean enabled) {
        mScrollable = enabled;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (isScrollable()) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false;
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (isScrollable()) {
                    return isScrollable();
                }

            default:
                return super.onTouchEvent(ev);
        }
    }
}
