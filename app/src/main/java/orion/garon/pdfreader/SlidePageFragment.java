package orion.garon.pdfreader;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import junit.framework.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static orion.garon.pdfreader.MainActivity.FILE_PATH_KEY;

/**
 * Created by VKI on 03.04.2018.
 */

public class SlidePageFragment extends Fragment implements OnLoadCompleteListener {

    final String CURRENT_PAGE_KEY = "current_page";

//    public PDFView pdfView;
    public PDFViewLockable pdfView;
    private PDFFileState mCurrentFileState;
    private int mCurrentPageNumber;

    private String filePath;
    private boolean touchEnabled;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle receivedBundle = getArguments();
        filePath = receivedBundle.getString("File_path");
        loadPreferences();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_page,
                                                          container,
                                                         false);
        pdfView = rootView.findViewById(R.id.pdf_view);
        openPdf();
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();

        savePreferences();
    }

    void openPdf() {

        File pdfFile = new File(filePath);

        if (pdfFile.exists()) {

            pdfView.fromFile(pdfFile).defaultPage(mCurrentPageNumber).load();

            setCurrentFileState(PDFFileState.ALL_PAGES);
            savePreferences();
        }
    }

    public void setCurrentFileState (PDFFileState newFileState) {

        if (mCurrentFileState != newFileState) {

            mCurrentFileState = newFileState;

            switch (newFileState) {

                case ALL_PAGES:
                    pdfView.setScrollingEnabled(true);
                    break;
                case SINGLE_PAGE:
                    pdfView.setScrollingEnabled(false);
                    break;
            }
        }
    }

    public PDFFileState getCurrentFileState() {

        return mCurrentFileState;
    }

    @Override
    public void loadComplete(int nbPages) {

        android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.detach(this).attach(this).commit();
    }

    private void savePreferences() {

        SharedPreferences.Editor editor = getActivity().getSharedPreferences(FILE_PATH_KEY, MODE_PRIVATE).edit();
        editor.putString(FILE_PATH_KEY, filePath);
        int currentPage = pdfView.getCurrentPage();
        editor.putInt(CURRENT_PAGE_KEY, currentPage);
        editor.apply();
    }

    private void loadPreferences() {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(FILE_PATH_KEY, MODE_PRIVATE);
        mCurrentPageNumber = sharedPreferences.getInt(CURRENT_PAGE_KEY, 0);
    }
}
