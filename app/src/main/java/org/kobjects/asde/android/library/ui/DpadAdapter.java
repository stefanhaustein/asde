package org.kobjects.asde.android.library.ui;

import android.view.MotionEvent;
import android.view.View;

import org.kobjects.asde.lang.type.Types;
import org.kobjects.graphics.Dpad;
import org.kobjects.asde.lang.classifier.Instance;
import org.kobjects.asde.lang.classifier.InstanceTypeImpl;
import org.kobjects.asde.lang.property.PhysicalProperty;
import org.kobjects.asde.lang.property.Property;
import org.kobjects.asde.lang.property.PropertyDescriptor;
import org.kobjects.asde.lang.type.Type;

import java.util.ArrayList;

public class DpadAdapter extends Instance {

    final Dpad dpad;
    final TouchProperty left;
    final TouchProperty up;
    final TouchProperty down;
    final TouchProperty right;
    final TouchProperty fire;
    final Property<Boolean> visible;
    private final ArrayList<Runnable> changeListeners = new ArrayList<>();

    static InstanceTypeImpl TYPE = new InstanceTypeImpl("Dpad (Singleton)",
        "Virtual directional pad that is displayed at the bottom of the screen"
            + "when the visible property is set. Other properties are true when the "
            + "corresponding key is pressed.") {
        @Override
        public boolean supportsChangeListeners() {
            return true;
        }

        @Override
        public void addChangeListener(final Object instance, Runnable changeListener) {
            ((DpadAdapter) instance).addChangeListener(changeListener);
        }

    };

    static {
        TYPE.addProperties(DpadMetaProperty.values());
    }


    public DpadAdapter(final Dpad dpad) {
        super(TYPE);
        this.dpad = dpad;
        left = new TouchProperty(dpad.left);
        up = new TouchProperty(dpad.up);
        down = new TouchProperty(dpad.down);
        right = new TouchProperty(dpad.right);
        fire = new TouchProperty(dpad.fire);
        visible = new Property<Boolean>() {
            @Override
            public boolean setImpl(Boolean visible) {
                return dpad.setVisible(visible);
            }

            @Override
            public Boolean get() {
                return dpad.getVisible();
            }
        };
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((DpadMetaProperty) property) {
            case up: return up;
            case left: return left;
            case right: return right;
            case down: return down;
            case fire: return fire;
            case visible: return visible;
        }
        throw new RuntimeException("Unrecognized property: " + property);
    }


    public void addChangeListener(Runnable changeListener) {
        changeListeners.add(changeListener);
    }

    enum DpadMetaProperty implements PropertyDescriptor {
        left(Types.BOOL),
        right(Types.BOOL),
        up(Types.BOOL),
        down(Types.BOOL),
        fire(Types.BOOL),
        visible(Types.BOOL);

        private final Type type;

        DpadMetaProperty(Type type) {
            this.type = type;
        }

        @Override
        public Type type() {
            return type;
        }
    }


    public void notifyChanged() {
        for (Runnable changeListener : changeListeners) {
            changeListener.run();
        }
    }


    class TouchProperty extends PhysicalProperty<Boolean> implements View.OnTouchListener {

        public TouchProperty(View view) {
            super(false);
            view.setOnTouchListener(this);

        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                set(true);
                DpadAdapter.this.notifyChanged();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                set(false);
                DpadAdapter.this.notifyChanged();
                return true;
            }
            return false;
        }
    }

}
