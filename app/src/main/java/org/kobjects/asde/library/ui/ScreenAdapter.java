package org.kobjects.asde.library.ui;

import android.view.View;

import org.kobjects.asde.lang.Interpreter;
import org.kobjects.asde.lang.Method;
import org.kobjects.asde.lang.Types;
import org.kobjects.graphics.Screen;
import org.kobjects.typesystem.Classifier;
import org.kobjects.typesystem.FunctionType;
import org.kobjects.typesystem.Instance;
import org.kobjects.typesystem.PhysicalProperty;
import org.kobjects.typesystem.PropertyDescriptor;
import org.kobjects.typesystem.Property;
import org.kobjects.typesystem.Type;

public class ScreenAdapter extends Instance implements View.OnLayoutChangeListener{
    private final Screen screen;
    private float scale;

    // Hack; access from viewport instead.
    float width;
    float height;

    final PhysicalProperty<Double> widthProperty = new PhysicalProperty<>(0.0);
    final PhysicalProperty<Double> heightProperty = new PhysicalProperty<>(0.0);

    public static Classifier CLASSIFIER =
            new Classifier(ScreenMetaProperty.values()) {
        @Override
        public ScreenAdapter createInstance() {
            throw new RuntimeException("Singleton");
        }
    };

    public final Classifier spriteClassifier =
            new Classifier(SpriteAdapter.SpriteMetaProperty.values()) {
        @Override
        public SpriteAdapter createInstance() {
            return new SpriteAdapter(spriteClassifier, ScreenAdapter.this);
        }
    };

    public final Classifier textClassifier =
            new Classifier(TextBoxAdapter.TextMetaProperty.values()) {
        @Override
        public TextBoxAdapter createInstance() {
            return new TextBoxAdapter(textClassifier, ScreenAdapter.this);
        }
    };
    /*
    final Property<Classifier> spriteProperty = new Property<Classifier>() {
        @Override
        public boolean set(Classifier classifier) {
            return false;
        }

        @Override
        public Classifier get() {
            return spriteClassifier;
        }
    };*/

    public ScreenAdapter(Screen screen) {
        super(CLASSIFIER);
        this.screen = screen;
        screen.view.addOnLayoutChangeListener(this);
    }

    public void cls() {
        screen.cls();
    }

    @Override
    public Property getProperty(PropertyDescriptor property) {
        switch ((ScreenMetaProperty) property) {
            case width: return widthProperty;
            case height: return heightProperty;
            case createPen: return new Method((FunctionType) ScreenMetaProperty.createPen.type) {
                @Override
                public Object call(Interpreter interpreter, int paramCount) {
                    return new PenAdapter(screen.createPen());
                }
            };
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        int widthPx = right - left;
        int heightPx = bottom - top;

        scale = Math.min(widthPx, heightPx) / 200f;

        width = widthPx / scale;
        height = heightPx / scale;

        widthProperty.set(Double.valueOf(width));
        heightProperty.set(Double.valueOf(height));

    }

    public Screen getScreen() {
        return screen;
    }

    private enum ScreenMetaProperty implements PropertyDescriptor {
        width(Types.NUMBER), height(Types.NUMBER), createPen(new FunctionType(PenAdapter.CLASSIFIER));

        private final Type type;

        ScreenMetaProperty(Type type) {
            this.type = type;
        }

        @Override
        public Type type() {
            return type;
        }
    }
}
