package org.kobjects.graphics;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public abstract class PositionedViewHolder<T extends View> extends ViewHolder<AnchorLayout<T>> {
    protected float x;
    protected float y;
    protected float z;

    protected boolean visible = true;

    final Screen screen;
    boolean syncRequested;

    ViewHolder<?> anchor;

    PositionedViewHolder(Screen screen, T view) {
        super(new AnchorLayout<>(view));
        synchronized (screen.widgets) {
            screen.widgets.add(this);
        }
        this.screen = screen;
        this.anchor = screen;
        view.setTag(this);
        screen.view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (PositionedViewHolder.this.view.getParent() != null) {
                    requestSync();
                }
            }
        });
    }


    abstract void syncUi();

    void requestSync() {
        if (!syncRequested) {
            syncRequested = true;
            screen.activity.runOnUiThread(() -> {
                syncRequested = false;
                view.setVisibility(visible ? View.VISIBLE : View.GONE);
                if (anchor.view != view.getParent()) {
                    if (view.getParent() != null) {
                        ((ViewGroup) view.getParent()).removeView(view);
                    }
                    anchor.view.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                syncUi();
            });
        }
    }

    public ViewHolder<?> getAnchor() {
        return anchor;
    }

    public Screen getScreen() {
        return screen;
    }

    public float getScreenX() {
        return anchor instanceof PositionedViewHolder<?> ? x + ((PositionedViewHolder) anchor).getScreenX() : x;
    }

    public float getScreenY() {
        return anchor instanceof PositionedViewHolder<?> ? y + ((PositionedViewHolder) anchor).getScreenX() : y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public boolean setX(float x) {
        if (x == this.x) {
            return false;
        }
        this.x = x;
        requestSync();
        return true;
    }

    public boolean setY(float y) {
        if (y == this.y) {
            return false;
        }
         this.y = y;
         requestSync();
        return true;
    }

    public boolean setAnchor(ViewHolder<?> anchor) {
        if (this.anchor == anchor) {
            return false;
        }
        this.anchor = anchor;
        requestSync();
        return true;
    }

    public boolean setZ(float z) {
        if (z == this.z) {
            return false;
        }
            this.z = z;
            requestSync();

        return true;
    }

    public boolean getVisible() {
        return visible;
    }

    public boolean setVisible(boolean value) {
        if (value == visible) {
            return false;
        }
        visible = value;
        requestSync();
        return true;
    }
}
