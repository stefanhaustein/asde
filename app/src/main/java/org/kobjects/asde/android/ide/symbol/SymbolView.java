package org.kobjects.asde.android.ide.symbol;

import android.widget.LinearLayout;

import org.kobjects.asde.Colors;
import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.lang.StaticSymbol;

import java.util.ArrayList;
import java.util.List;

public abstract class SymbolView extends LinearLayout {
    protected final MainActivity mainActivity;

    public SymbolTitleView titleView;
    List<ExpandListener> expandListeners = new ArrayList<>();
    public boolean expanded;
    public StaticSymbol symbol;

    protected ExpandableList contentView;


    protected SymbolView(MainActivity mainActivity, StaticSymbol symbol) {
        super(mainActivity);
        this.mainActivity = mainActivity;
        this.symbol = symbol;
        setOrientation(VERTICAL);

        titleView = new SymbolTitleView(mainActivity, symbol.getName().isEmpty() ? "Main" : symbol.getName());
        addView(titleView);
        titleView.setOnClickListener(clicked -> {
            setExpanded(!expanded, true);

            if (!expanded && mainActivity.sharedCodeViewAvailable()) {
                mainActivity.outputView.syncContent();
            }

        });
        refresh();
    }

    public void addExpandListener(ExpandListener expandListener) {
        expandListeners.add(expandListener);
    }

    public abstract void syncContent();

    public void refresh() {
        titleView.setBackgroundColor(symbol.getErrors().size() > 0 ? Colors.RED : expanded ? Colors.PRIMARY_LIGHT_FILTER : 0);
        if (symbol.getErrors().size() > 0) {
            System.out.println(symbol.getErrors());
        }
    }

    public void setExpanded(final boolean expand, boolean animated) {
        if (expanded == expand) {
            return;
        }
        if (animated && contentView == getContentView()) {
            contentView.animateNextChanges();
        }
        expanded = expand;
        for (ExpandListener expandListener : expandListeners) {
            expandListener.notifyExpanding(this, animated);
        }
        syncContent();
    }

    public LinearLayout getContentView() {
        if (mainActivity.sharedCodeViewAvailable()) {
            return mainActivity.obtainSharedCodeView(this);
        }

        if (contentView == null) {
            contentView = new ExpandableList(mainActivity);
            addView(contentView);
        }

        return contentView;
    }


}