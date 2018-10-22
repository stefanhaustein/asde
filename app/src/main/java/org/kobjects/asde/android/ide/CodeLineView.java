package org.kobjects.asde.android.ide;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vanniktech.emoji.EmojiTextView;

import org.kobjects.annotatedtext.AnnotatedStringBuilder;
import org.kobjects.annotatedtext.Span;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.node.Node;

import java.util.HashMap;

public class CodeLineView extends LinearLayout {
    TextView lineNumberView;
    TextView statementView;
    boolean odd;
    boolean highlighted;
    int lineNumber;

    public CodeLineView(Context context, boolean odd) {
        super(context);
        this.odd = odd;

        lineNumberView = new AppCompatTextView(context);
        lineNumberView.setGravity(Gravity.TOP | Gravity.RIGHT);
        lineNumberView.setTypeface(Typeface.MONOSPACE);

        statementView = new EmojiTextView(context);
        statementView.setTypeface(Typeface.MONOSPACE);

        addView(lineNumberView, new LayoutParams(Math.round(lineNumberView.getTextSize() * 3f), ViewGroup.LayoutParams.MATCH_PARENT));
        addView(statementView, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1));

        updateColor();
    }

    void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
        lineNumberView.setText(lineNumber + " ");
    }

    void setCodeLine(CodeLine codeLine, HashMap<Node, Exception> errors) {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        codeLine.toString(asb, errors);

        SpannableString s = new SpannableString(asb.toString());
        for (final Span span : asb.spans()) {
            s.setSpan(new BackgroundColorSpan(Colors.SECONDARY_LIGHT), span.start, span.end, 0);
            s.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Error");
                    builder.setMessage(span.annotation.toString());
                    builder.show();
                }
            }, span.start, span.end, 0);
            statementView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        statementView.setText(s);
    }

    void setHighlighted(boolean highlighted) {
        if (highlighted != this.highlighted) {
            this.highlighted = highlighted;
            updateColor();
        }
    }

    void updateColor() {
        if (highlighted) {
            setBackgroundColor(Colors.SECONDARY_LIGHT);
        } else if (odd) {
            setBackgroundColor(Colors.PRIMARY_LIGHT);
        } else {
            setBackgroundColor(0);
        }

    }

    @Override
    public String toString() {
        return lineNumberView.getText().toString().trim() + " " + statementView.getText().toString().trim();
    }
}
