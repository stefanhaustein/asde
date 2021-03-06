package org.kobjects.asde.android.ide;

import android.app.AlertDialog;
import android.widget.LinearLayout;

import org.kobjects.asde.R;
import org.kobjects.asde.android.ide.widget.IconButton;
import org.kobjects.asde.lang.classifier.Property;
import org.kobjects.asde.lang.exceptions.ForcedStopException;
import org.kobjects.asde.lang.runtime.StartStopListener;
import org.kobjects.asde.lang.io.Console;

public class RunControlView extends LinearLayout {
  private final IconButton startButton;
  private final IconButton pauseButton;
  private final IconButton resumeButton;
  private final IconButton stopButton;
  private final IconButton stepButton;
  private final IconButton closeButton;
  private final MainActivity mainActivity;
  //  private final FloatingActionButton runButton;

  public RunControlView(MainActivity mainActivity) /*, FloatingActionButton runButton) */ {
    super(mainActivity);
    this.mainActivity = mainActivity;
    //    this.runButton = runButton;

        /*
        runButton.setOnClickListener( item -> {
            hideControlButtons();
            mainActivity.shell.mainControl.start();
        });
        */

    startButton = new IconButton(mainActivity, R.drawable.baseline_send_24, item -> {
      for (Property property : mainActivity.program.mainModule.getProperties()) {
        if (!property.getErrors().isEmpty()) {
          new AlertDialog.Builder(mainActivity).setTitle("Error").setMessage("Error in '" + property.getName() + "'.").setPositiveButton("Dismiss", null).show();
          return;
        }
      }
      hideControlButtons();
      mainActivity.shell.mainControl.start();
    });


    stopButton = new IconButton(mainActivity, R.drawable.baseline_stop_24, item -> {
      hideControlButtons();
      mainActivity.shell.mainControl.abort();
    });
    resumeButton = new IconButton(mainActivity, R.drawable.baseline_play_arrow_24, item -> {
      hideControlButtons();
      mainActivity.shell.mainControl.resume();
    });
    stepButton = new IconButton(mainActivity, R.drawable.baseline_skip_next_24, item -> {
      item.setEnabled(false);
      mainActivity.shell.mainControl.step();
    });
    pauseButton = new IconButton(mainActivity, R.drawable.baseline_pause_24, item -> {
      hideControlButtons();

      resumeButton.setVisibility(VISIBLE);
      stepButton.setVisibility(VISIBLE);
      stepButton.setEnabled(false);
      stopButton.setVisibility(VISIBLE);

      mainActivity.fullScreenMode = false;
      mainActivity.arrangeUi();
      mainActivity.programTitleView.refresh();
      mainActivity.programView.refresh();

      mainActivity.programView.highlightImpl(
          mainActivity.shell.mainControl.lastCreatedContext.function,
          mainActivity.shell.mainControl.lastCreatedContext.currentLine);

      mainActivity.shell.mainControl.pause();
    });
    closeButton = new IconButton(mainActivity, R.drawable.baseline_clear_24, item -> {
      hideControlButtons();
//            runButton.show();
      startButton.setVisibility(VISIBLE);
      mainActivity.console.clearScreen(Console.ClearScreenType.PROGRAM_CLOSED);
      mainActivity.fullScreenMode = false;
      mainActivity.arrangeUi();
    });

    addView(stepButton);
    addView(resumeButton);
    addView(pauseButton);
    addView(stopButton);
    addView(closeButton);
    //    runButton.hide();
    addView(startButton);

    hideControlButtons();
    //  runButton.show();

    startButton.setVisibility(VISIBLE);

    mainActivity.shell.mainControl.addStartStopListener(new StartStopListener() {
      @Override
      public void programStarted() {
        mainActivity.runOnUiThread(() -> {
          hideControlButtons();
          if (!mainActivity.runningFromShortcut) {
            pauseButton.setVisibility(VISIBLE);
            stopButton.setVisibility(VISIBLE);
          }
          mainActivity.fullScreenMode = true;
          mainActivity.arrangeUi();
        });
      }
      @Override
      public void programAborted(Exception cause) {
        mainActivity.runOnUiThread(() -> {
          hideControlButtons();
          mainActivity.console.clearScreen(Console.ClearScreenType.PROGRAM_CLOSED);
//                    runButton.show();
          startButton.setVisibility(VISIBLE);
          mainActivity.fullScreenMode = false;
          mainActivity.arrangeUi();

          if (cause != null && !(cause instanceof ForcedStopException)) {
            mainActivity.console.showError(null, cause);
          }

        });
      }
      @Override
      public void programEnded() {
        mainActivity.runOnUiThread(() -> {
          hideControlButtons();
          closeButton.setVisibility(VISIBLE);
        });
      }
      @Override
      public void programPaused() {
        mainActivity.runOnUiThread(() -> {
          hideControlButtons();
          resumeButton.setVisibility(VISIBLE);
          stopButton.setVisibility(VISIBLE);
          stepButton.setVisibility(VISIBLE);
          stepButton.setEnabled(true);
        });
      }
    });
  }


  private void hideControlButtons() {
    pauseButton.setVisibility(GONE);
    resumeButton.setVisibility(GONE);
    startButton.setVisibility(GONE);
//        runButton.hide();
    stepButton.setVisibility(GONE);
    stopButton.setVisibility(GONE);
    closeButton.setVisibility(GONE);
    mainActivity.programView.unHighlight();
  }
}
