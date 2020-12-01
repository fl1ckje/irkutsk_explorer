//package com.fl1ckjedev.irkutskexplorer;
//
//import android.app.AlertDialog;
//import android.app.Dialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.provider.Settings;
//
//import androidx.fragment.app.DialogFragment;
//
//import static com.fl1ckjedev.irkutskexplorer.MainActivity.LOCATION_SETTINGS_REQUEST;
//
//public class LocationDialogFragment extends DialogFragment {
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        // Use the Builder class for convenient dialog construction
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(R.string.open_location_settings_dialog_title)
//                .setMessage(R.string.open_location_settings_dialog_message)
//                .setPositiveButton(R.string.yes, (dialog, id) ->
//                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
//                                LOCATION_SETTINGS_REQUEST))
//                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User cancelled the dialog
//                    }
//                });
//        // Create the AlertDialog object and return it
//        return builder.create();
//    }
//}
