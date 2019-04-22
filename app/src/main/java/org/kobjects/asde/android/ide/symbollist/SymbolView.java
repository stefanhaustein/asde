package org.kobjects.asde.android.ide.symbollist;

import android.widget.LinearLayout;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.android.ide.widget.SymbolTitleView;
import org.kobjects.asde.lang.GlobalSymbol;

import java.util.ArrayList;
import java.util.List;

public abstract class SymbolView extends LinearLayout {
    final MainActivity mainActivity;

    SymbolTitleView titleView;
    List<ExpandListener> expandListeners = new ArrayList<>();
    boolean expanded;
    public GlobalSymbol symbol;

    private ExpandableList contentView;


    SymbolView(MainActivity mainActivity, GlobalSymbol symbol) {
        super(mainActivity);
        this.mainActivity = mainActivity;
        this.symbol = symbol;
        setOrientation(VERTICAL);

        titleView = new SymbolTitleView(mainActivity, symbol.getName().isEmpty() ? "Main" : symbol.getName());
        addView(titleView);
        titleView.setOnClickListener(clicked -> {
            setExpanded(!expanded, true);
        });
    }

    public void addExpandListener(ExpandListener expandListener) {
        expandListeners.add(expandListener);
    }

    public abstract void syncContent();

    public void refresh() {
        titleView.setBackgroundColor(symbol.getErrors().size() > 0 ? mainActivity.colors.accentLight : expanded ? mainActivity.colors.primaryLight : 0);
    }

    public void setExpanded(final boolean expand, boolean animated) {
        if (expanded == expand) {
            return;
        }
        if (animated) {
            getContentView().animateNextChanges();
        }
        expanded = expand;
        for (ExpandListener expandListener : expandListeners) {
            expandListener.notifyExpanding(this, animated);
        }
        syncContent();
    }

    public ExpandableList getContentView() {
        if (mainActivity.codeView != null) {
            return mainActivity.codeView;
        }

        if (contentView == null) {
            contentView = new ExpandableList(mainActivity);
            addView(contentView);
        }

        return contentView;
    }


}