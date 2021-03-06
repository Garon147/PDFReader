package orion.garon.pdfreader;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.link.DefaultLinkHandler;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener;

import java.io.File;
import static android.content.Context.MODE_PRIVATE;
import static orion.garon.pdfreader.MainActivity.FILE_PATH_KEY;

/**
 * Created by VKI on 03.04.2018.
 */

public class SlidePageFragment extends Fragment implements OnLoadCompleteListener {

    final String CURRENT_PAGE_KEY = "current_page";
    final String CURRENT_STATE = "current_state";

    public PDFView pdfView;
    public ImageView pdfSingleView;
    private PDFFileState mCurrentFileState;
    public int mCurrentPageNumber;
    public File pdfFile;

    private String mFilePath;
    private MainActivity mainActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle receivedBundle = getArguments();
        loadPreferences();
        setFilePath(receivedBundle.getString("File_path"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        mainActivity = (MainActivity) getActivity();

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_page,
                                                          container,
                                                         false);
        pdfView = rootView.findViewById(R.id.pdf_view);
        pdfSingleView = rootView.findViewById(R.id.pdf_single_view);
        mainActivity.changeViewsVisbility(View.INVISIBLE);
        openPdf();
        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();

        savePreferences();
    }

    void openPdf() {

        pdfFile = new File(mFilePath);

        if (pdfFile.exists()) {

            setPdfViewParameters(getCurrentFileState());

            savePreferences();
        }
    }

    public void setCurrentFileState (PDFFileState newFileState) {

        if (mCurrentFileState != newFileState) {

            mCurrentPageNumber = pdfView.getCurrentPage();

            mCurrentFileState = newFileState;

            setPdfViewParameters(newFileState);
        }
    }

    private void setFilePath (String filePath) {

        if (mFilePath == null || !mFilePath.equals(filePath)) {

            mFilePath = filePath;
        }
    }

    private void setPdfViewParameters (PDFFileState fileState) {

        switch (fileState) {

            case ALL_PAGES:
                mainActivity.changeViewsVisbility(View.INVISIBLE);
                pdfView.fromFile(pdfFile).
                        defaultPage(mCurrentPageNumber).
                        linkHandler(new DefaultLinkHandler(pdfView)).
                        pageFling(false).
                        pageSnap(false).
                        autoSpacing(false).
                        onPageScroll(new OnPageScrollListener() {
                            @Override
                            public void onPageScrolled(int page, float positionOffset) {
                                mainActivity.setTextCounter();
                            }
                        }).
                        onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {
                                mainActivity.setTextCounter();
                            }
                        }).
                        load();
                break;
            case SINGLE_PAGE:
                mainActivity.changeViewsVisbility(View.VISIBLE);
                pdfView.fromFile(pdfFile).
                        defaultPage(mCurrentPageNumber).
                        linkHandler(new DefaultLinkHandler(pdfView)).
                        pageFling(true).
                        pageSnap(true).
                        autoSpacing(true).
                        onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {
                                mCurrentPageNumber = pdfView.getCurrentPage();
                                mainActivity.setPageNumber(mCurrentPageNumber, false);
                            }
                        }).
                        onPageChange(new OnPageChangeListener() {
                            @Override
                            public void onPageChanged(int page, int pageCount) {
                                mainActivity.setTextCounter();
                            }
                        }).
                        onLoad(new OnLoadCompleteListener() {
                            @Override
                            public void loadComplete(int nbPages) {
                                mainActivity.setTextCounter();
                            }
                        }).
                        load();
                break;
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
        editor.putString(FILE_PATH_KEY, mFilePath);
        int currentPage = pdfView.getCurrentPage();
        editor.putInt(CURRENT_PAGE_KEY, currentPage);
        PDFFileState currentState = getCurrentFileState();
        editor.putInt(CURRENT_STATE, PDFFileState.intFromState(currentState));
        editor.apply();
    }

    private void loadPreferences() {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(FILE_PATH_KEY, MODE_PRIVATE);
        mCurrentPageNumber = sharedPreferences.getInt(CURRENT_PAGE_KEY, 0);
        mCurrentFileState = PDFFileState.stateFromInt(sharedPreferences.getInt(CURRENT_STATE, 0));
    }
}
