package org.kobjects.asde.android.ide.symbollist;

import android.view.View;
import android.widget.PopupMenu;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.editor.DeleteFlow;
import org.kobjects.asde.android.ide.editor.FunctionSignatureFlow;
import org.kobjects.asde.android.ide.editor.RenameFlow;
import org.kobjects.asde.android.ide.widget.ExpandableList;
import org.kobjects.asde.android.ide.widget.SymbolTitleView;
import org.kobjects.asde.lang.CallableUnit;
import org.kobjects.asde.lang.CodeLine;
import org.kobjects.asde.lang.Types;

import java.util.ArrayList;
import java.util.Map;

public class FunctionView extends SymbolView {
    public CallableUnit callableUnit;
    OnLongClickListener lineClickListener;

    public FunctionView(final MainActivity mainActivity, String name, final CallableUnit callableUnit) {
        super(mainActivity, name);
        this.callableUnit = callableUnit;

        boolean isMain = callableUnit == callableUnit.program.main;
        boolean isVoid = callableUnit.getType().getReturnType() == Types.VOID;

        titleView.setTypeIndicator(
                isMain ? 'M' : isVoid ? 'S' : 'F',
                isMain ? mainActivity.colors.primary : isVoid ? mainActivity.colors.purple : mainActivity.colors.cyan);

        titleView.setMoreClickListener(clicked -> {
            PopupMenu popupMenu = new PopupMenu(mainActivity, clicked);
            popupMenu.getMenu().add("Rename").setOnMenuItemClickListener(item -> {
                new RenameFlow(mainActivity, name).start();
               return true;
            });
            popupMenu.getMenu().add("Change Signature").setOnMenuItemClickListener(item -> {
                new FunctionSignatureFlow(mainActivity).changeSignature(name, callableUnit);
                return true;
            });
            popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(item -> {
                new DeleteFlow(mainActivity, name).start();
                return true;
            });
            popupMenu.show();
        });

        ArrayList<String> subtitles = new ArrayList<>();
        for (int i = 0; i < callableUnit.getType().getParameterCount(); i++) {
            subtitles.add(" " + callableUnit.parameterNames[i] + ": " + callableUnit.getType().getParameterType(i));
        }
        if (!isVoid) {
            subtitles.add("-> " + callableUnit.getType().getReturnType());
        }

        titleView.setSubtitles(subtitles);

        titleView.setOnClickListener(clicked -> {
            setExpanded(!expanded, true);
        });
    }


    public void syncContent() {
        titleView.setBackgroundColor(callableUnit.errors.size() > 0 ? mainActivity.colors.accentLight : expanded ? mainActivity.colors.primaryLight : 0);

        ExpandableList codeView = getContentView();

        if (!expanded) {
            codeView.removeAllViews();
            return;
        }
        int index = 0;
        for (Map.Entry<Integer, CodeLine> entry : callableUnit.entrySet()) {
            CodeLineView codeLineView;
            if (index < codeView.getChildCount()) {
                codeLineView = (CodeLineView) codeView.getChildAt(index);
            } else {
                codeLineView = new CodeLineView(mainActivity, index % 2 == 1);
                codeView.addView(codeLineView);
            }
            codeLineView.setLineNumber(entry.getKey());
            codeLineView.setCodeLine(entry.getValue(), callableUnit.errors);
            codeLineView.setOnLongClickListener(lineClickListener);
            index++;
        }
        while (index < codeView.getChildCount()) {
            codeView.removeViewAt(codeView.getChildCount() - 1);
        }
    }


    CodeLineView findLine(int lineNumber) {
        ExpandableList codeView = getContentView();
        for (int i = 0; i < codeView.getChildCount(); i++) {
            CodeLineView codeLineView = (CodeLineView) codeView.getChildAt(i);
            if (codeLineView.lineNumber == lineNumber) {
                return codeLineView;
            }
        }
        return null;
    }

}
