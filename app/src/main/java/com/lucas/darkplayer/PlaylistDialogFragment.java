package com.lucas.darkplayer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

public class PlaylistDialogFragment extends DialogFragment {
    Context mContext;
    String playlistName;
    PlaylistDBController db;
    public PlaylistDialogFragment(){
        mContext=getActivity();
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        Bundle arguments = getArguments();
        try {
            playlistName = arguments.getString("playlistName");
        }catch(NullPointerException e){
            e.printStackTrace();
        }

        alertDialogBuilder.setTitle("Delete?");
        alertDialogBuilder.setMessage("Are you sure you want to delete playlist?");
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.deletePlaylist(getActivity(),playlistName);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });


        return alertDialogBuilder.create();
    }
}
