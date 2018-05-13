package orion.garon.pdfreader;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.link.DefaultLinkHandler;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

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

    public PDFView pdfView;
    public ImageView pdfSingleView;
    private PDFFileState mCurrentFileState;
    public int mCurrentPageNumber;
    public PdfRenderer mPdfRenderer;
    public File pdfFile;

    private String filePath;
    private MainActivity mainActivity;
    private OnPageChangeListener onPageChangeListener;
    private boolean isStateChanging;
    private boolean needSetPage;

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

        mainActivity = (MainActivity) getActivity();
        onPageChangeListener = new OnPageChangeListener() {
            @Override
            public void onPageChanged(int page, int pageCount) {

                if (!isStateChanging) {
                    mCurrentPageNumber = page;
                }

                if (needSetPage) {
                    mainActivity.setPageNumber(page);
                }
            }
        };

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_page,
                                                          container,
                                                         false);
        pdfView = rootView.findViewById(R.id.pdf_view);
        pdfSingleView = rootView.findViewById(R.id.pdf_single_view);
//        mCurrentFileState = PDFFileState.ALL_PAGES;
        mainActivity.changeButtonsVisbility(View.INVISIBLE);
        openPdf();
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();

        savePreferences();
    }

    void openPdf() {

        pdfFile = new File(filePath);

        if (pdfFile.exists()) {

            try {

                mPdfRenderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile,
                        ParcelFileDescriptor.MODE_READ_ONLY));
            } catch (Exception e) {

                e.printStackTrace();
            }



            switch (getCurrentFileState()) {

                case ALL_PAGES:
                    mainActivity.changeButtonsVisbility(View.INVISIBLE);
                    pdfView.fromFile(pdfFile).
                            defaultPage(mCurrentPageNumber).
                            linkHandler(new DefaultLinkHandler(pdfView)).
//                            swipeHorizontal(false).
                            pageFling(false).
                            pageSnap(false).
                            autoSpacing(false).
                            load();
                    break;
                case SINGLE_PAGE:
                    mainActivity.changeButtonsVisbility(View.VISIBLE);
                    mCurrentPageNumber = pdfView.getCurrentPage();
                    pdfView.fromFile(pdfFile).
                            defaultPage(mCurrentPageNumber).
                            linkHandler(new DefaultLinkHandler(pdfView)).
//                            swipeHorizontal(true).
                            pageFling(true).
                            pageSnap(true).
                            autoSpacing(true).
                            onPageChange(onPageChangeListener).
                            load();
                    break;
            }



            setCurrentFileState(PDFFileState.ALL_PAGES);
            savePreferences();
        }
    }

    public void setCurrentFileState (PDFFileState newFileState) {

        if (mCurrentFileState != newFileState) {

            mCurrentFileState = newFileState;

            switch (newFileState) {

                case ALL_PAGES:
                    mainActivity.changeButtonsVisbility(View.INVISIBLE);
                    pdfView.fromFile(pdfFile).
                            defaultPage(mCurrentPageNumber).
                            linkHandler(new DefaultLinkHandler(pdfView)).
//                            swipeHorizontal(false).
                            pageFling(false).
                            pageSnap(false).
                            autoSpacing(false).
                            load();
                    break;
                case SINGLE_PAGE:
                    isStateChanging = true;
                    needSetPage = false;
                    pdfView.fromFile(pdfFile).
                            defaultPage(mCurrentPageNumber).
                            linkHandler(new DefaultLinkHandler(pdfView)).
//                            swipeHorizontal(true).
                            pageFling(true).
                            pageSnap(true).
                            autoSpacing(true).
                            onPageChange(onPageChangeListener).
                            onLoad(new OnLoadCompleteListener() {
                                @Override
                                public void loadComplete(int nbPages) {
                                    isStateChanging = false;
                                    mCurrentPageNumber = pdfView.getCurrentPage();
                                    mainActivity.setPageNumber(mCurrentPageNumber);
                                    needSetPage = true;
                                }
                            }).
                            load();
                    mainActivity.changeButtonsVisbility(View.VISIBLE);
                    break;
            }
        }
    }

    public PDFFileState getCurrentFileState() {

        if (mCurrentFileState == null) {

            mCurrentFileState = PDFFileState.ALL_PAGES;
        }

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
