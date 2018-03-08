package org.chat21.android.ui.messages.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.chat21.android.R;
import org.chat21.android.core.users.models.IChatUser;
import org.chat21.android.ui.ChatUI;
import org.chat21.android.ui.messages.listeners.OnAttachDocumentsClickListener;

import static org.chat21.android.ui.messages.activities.MessageListActivity._INTENT_ACTION_GET_PICTURE;

/**
 * Created by stefanodp91 on 28/09/17.
 */
public class BottomSheetAttach extends BottomSheetDialogFragment implements
        View.OnClickListener {

    private static final String DEBUG_TAG = BottomSheetAttach.class.getName();

    private static final String BOTTOM_SHEET_ATTACH_RECIPIENT = "BOTTOM_SHEET_ATTACH_RECIPIENT";
    private static final String BOTTOM_SHEET_ATTACH_CHANNEL_TYPE = "BOTTOM_SHEET_ATTACH_CHANNEL_TYPE";

    private IChatUser recipient;
    private String channelType;

    private Button mAttachImagesView;
    private Button mAttachDocumentsView;

    public static BottomSheetAttach newInstance(IChatUser recipient, String channelType) {
        BottomSheetAttach f = new BottomSheetAttach();
        Bundle args = new Bundle();
        args.putSerializable(BOTTOM_SHEET_ATTACH_RECIPIENT, recipient);
        args.putString(BOTTOM_SHEET_ATTACH_CHANNEL_TYPE, channelType);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recipient = (IChatUser) getArguments().getSerializable(BOTTOM_SHEET_ATTACH_RECIPIENT);
        channelType = getArguments().getString(BOTTOM_SHEET_ATTACH_CHANNEL_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_bottom_sheet_attach, container, false);

        registerViews(rootView);
        initViews();
        initListeners();

        return rootView;
    }

    private void registerViews(View rootView) {
        mAttachImagesView = rootView.findViewById(R.id.btn_attach_images);
        mAttachDocumentsView = rootView.findViewById(R.id.btn_attach_documents);
    }

    private void initViews() {
        // if the document click listener is null hides the document button view
        if (ChatUI.getInstance().getOnAttachDocumentsClickListener() == null) {
            mAttachDocumentsView.setVisibility(View.GONE);
        }
    }

    private void initListeners() {
        mAttachImagesView.setOnClickListener(this);
        mAttachDocumentsView.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btn_attach_images) {
            onAttachImagesActionListener();
        } else if (id == R.id.btn_attach_documents) {
            onAttachDocumentsActionListener();
        }
    }

    private void onAttachImagesActionListener() {
        Log.d(DEBUG_TAG, "BottomSheetAttach.onAttachImagesActionListener");

        showFilePickerDialog();
    }

    private void onAttachDocumentsActionListener() {
        Log.d(DEBUG_TAG, "BottomSheetAttach.onAttachDocumentsActionListener");

        // call the click listener defined in Chat.Configuration
        OnAttachDocumentsClickListener onAttachDocumentsClickListener =
                ChatUI.getInstance().getOnAttachDocumentsClickListener();

        if (onAttachDocumentsClickListener != null) {
            onAttachDocumentsClickListener.onAttachDocumentsClicked(recipient, channelType,null);
        }

        // dismiss the bottomsheet
        getDialog().dismiss();
    }


    private void showFilePickerDialog() {
        Log.d(DEBUG_TAG, "BottomSheetAttach.showFilePickerDialog");

        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }

        // show only local files
        // TODO: 07/09/17 settare a false per inviare anche files remoti
        // TODO: 07/09/17 fare un setting per l'invio di file remoti
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // set MIME type for image
//        intent.setType("*/*"); // all files
        intent.setType("image/*");

        getActivity().startActivityForResult(intent, _INTENT_ACTION_GET_PICTURE);

        // dismiss the bottomsheet
        getDialog().dismiss();
    }
}