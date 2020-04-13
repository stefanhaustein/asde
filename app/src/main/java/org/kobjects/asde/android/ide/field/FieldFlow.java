package org.kobjects.asde.android.ide.field;

import android.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputLayout;

import org.kobjects.asde.android.ide.MainActivity;
import org.kobjects.asde.android.ide.text.ExpressionValidator;
import org.kobjects.asde.android.ide.text.TextValidator;
import org.kobjects.asde.android.ide.widget.InputFlowBuilder;
import org.kobjects.asde.android.ide.property.PropertyNameValidator;
import org.kobjects.asde.android.ide.widget.TypeSpinner;
import org.kobjects.asde.lang.classifier.Classifier;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.classifier.GenericProperty;
import org.kobjects.asde.lang.classifier.Trait;
import org.kobjects.asde.lang.classifier.TraitProperty;
import org.kobjects.asde.lang.node.Node;
import org.kobjects.asde.lang.program.ProgramListener;
import org.kobjects.asde.lang.type.Type;

public class FieldFlow {



  public static void editProperties(final MainActivity mainActivity, final Property symbol) {
    new FieldFlow(mainActivity, symbol.getOwner(), symbol, symbol.isInstanceField(), symbol.isMutable()).showInitializerDialog();
  }

  public static void createStaticProperty(final MainActivity mainActivity, final Classifier owner, boolean isMutable) {
    new FieldFlow(mainActivity, owner, null, false, isMutable).showNameDialog();
  }

  public static void createInstanceProperty(final MainActivity mainActivity, final Classifier owner) {
    new FieldFlow(mainActivity, owner, null, true, false).showNameDialog();
  }


  private final MainActivity mainActivity;
  private final Classifier owner;
  private final Property symbol;
  private final boolean isInstanceField;
  private final boolean isMutable;

  private String name;
  private final String title;

  FieldFlow(MainActivity mainActivity, Classifier owner, Property symbol, boolean isInstanceField, boolean isMutable) {
    this.mainActivity = mainActivity;
    this.isInstanceField = isInstanceField;
    this.isMutable = isMutable;
    this.owner = owner;
    this.symbol = symbol;
    name = symbol == null ? "" : symbol.getName();
    title = isInstanceField
        ? ("Property")
        : (isMutable ? "Variable" : "Constant");
  }


  private void showNameDialog() {
    new InputFlowBuilder(mainActivity, "Add " + title)
        .addInput("Name", name, new PropertyNameValidator(owner))
        .setPositiveLabel("Next")
        .start( result -> {
          this.name = result[0];
          showInitializerDialog();
        });
  }


  private void showInitializerDialog() {

    boolean isTrait = owner instanceof Trait;

    LinearLayout inputLayout = new LinearLayout(mainActivity);
    inputLayout.setOrientation(LinearLayout.VERTICAL);


    final EditText editText = new EditText(mainActivity);
    if (symbol != null && symbol.getInitializer() != null) {
      editText.setText(symbol.getInitializer().toString());
    }

    final TextInputLayout textInputLayout = new TextInputLayout(mainActivity);
    textInputLayout.addView(editText);

    final TextValidator.TextInputLayoutValidator validator = new ExpressionValidator(mainActivity).attach(textInputLayout);

    final TypeSpinner typeSpinner = new TypeSpinner(mainActivity, isTrait ? null  : "Initializer Expression:");
    if (isInstanceField) {
      inputLayout.addView(typeSpinner);
      typeSpinner.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (!isTrait) {
              textInputLayout.setEnabled(position == 0);
              validator.setEnabled(position == 0);
            }
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {

          }
        });
      if (symbol != null && symbol.getInitializer() == null) {
        typeSpinner.selectType(symbol.getType());
        textInputLayout.setEnabled(false);
      }
    } else {
      TextView label = new TextView(mainActivity);
      label.setText("Initializer Expression:");
      inputLayout.addView(label);
    }
    if (!isTrait) {
      inputLayout.addView(textInputLayout);
    }

    CheckBox mutableCheckbox = new CheckBox(mainActivity);
    if (isInstanceField) {
      mutableCheckbox.setText("Mutable");
      inputLayout.addView(mutableCheckbox);
    }

    LinearLayout mainLayout = new LinearLayout(mainActivity);
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.leftMargin = 56;
    layoutParams.rightMargin = 56;
    layoutParams.topMargin = 28;

    mainLayout.addView(inputLayout, layoutParams);

    AlertDialog.Builder alert = new AlertDialog.Builder(mainActivity);
    alert.setTitle(title + " '" + name + "'");
    alert.setView(mainLayout);

    alert.setNegativeButton("Cancel", null);
    alert.setPositiveButton("Ok", (a, b) -> {
      Type fixedType = typeSpinner.getSelectedType();

        Node initializer = fixedType == null ? mainActivity.program.parser.parseExpression(editText.getText().toString()) : null;
        if (symbol == null) {
          if (fixedType == null) {
            owner.putProperty(GenericProperty.createWithInitializer(owner, isInstanceField, isMutable, name, initializer));
          } else {
            owner.putProperty(GenericProperty.createUninitialized(owner, isMutable, name, fixedType));
          }
        } else {
          if (fixedType == null) {
            symbol.setInitializer(initializer);
          } else {
            symbol.setFixedType(fixedType);
          }
          symbol.setMutable(mutableCheckbox.isChecked());
        }

      mainActivity.program.sendProgramEvent(ProgramListener.Event.CHANGED);
    });

    alert.show();


    /*
    InputFlowBuilder builder = new InputFlowBuilder(mainActivity, "Property " + name);
    builder.addInput("Initial value", symbol != null ? symbol.getInitializer().toString() : null, new ExpressionValidator(mainActivity));
    builder.start(result -> {
      Node parsed =  mainActivity.program.parser.parseExpression(result[0]);
      if (symbol == null) {
        owner.putProperty(GenericProperty.createWithInitializer(owner, isInstanceField, isMutable, name, parsed));
        mainActivity.program.sendProgramEvent(ProgramListener.Event.CHANGED);
      } else {
        symbol.setInitializer(parsed);
        mainActivity.program.notifySymbolChanged(symbol);
      }
    }); */
  }


}
