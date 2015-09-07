package me.cristiangomez.placefinder.ui.activity.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;

import com.orhanobut.logger.Logger;

import fr.tvbarthel.lib.blurdialogfragment.BlurDialogEngine;
import me.cristiangomez.placefinder.R;

/**
 * Created by cristianjgomez on 9/6/15.
 */
public class LocationEnableDialog extends AppCompatDialogFragment {
    private LocationEnableDialogCallbacks mCallbacks;
    private BlurDialogEngine mBlurEngine;

    //region Overriden methods
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallbacks = (LocationEnableDialogCallbacks) context;
        } catch (ClassCastException e) {
            Logger.e(
                    context.getClass().getCanonicalName() + " Should implement" +
                            LocationEnableDialogCallbacks.class.getCanonicalName()
            );
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBlurEngine = new BlurDialogEngine(getActivity());
        mBlurEngine.debug(false);
        mBlurEngine.setBlurRadius(8);
        mBlurEngine.setDownScaleFactor(8f);
        mBlurEngine.setBlurActionBar(true);
        mBlurEngine.setUseRenderScript(true);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mBlurEngine.onResume(getRetainInstance());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBlurEngine.onDestroy();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppCompatDialog dialogFragment = new AlertDialog.Builder(getContext())
                .setTitle(R.string.activate_location)
                .setMessage(R.string.location_needed)
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mCallbacks != null) {
                            mCallbacks.onLocationEnableAccept();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mCallbacks != null) {
                            mCallbacks.onLocationEnableCancel();
                        }
                    }
                })
                .create();
        return dialogFragment;
    }

    //endregion

    //region Interfaces
    public interface LocationEnableDialogCallbacks {
        void onLocationEnableAccept();
        void onLocationEnableCancel();
    }
    //endregion
}
