package orion.garon.pdfreader;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import static android.app.Activity.RESULT_OK;

/**
 * Created by VKI on 03.04.2018.
 */

public class SlidePageFragment extends Fragment implements OnLoadCompleteListener{

    public PDFView pdfView;
    private PDFFileState mCurrentFileState;

    private String filePath;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle receivedBundle = getArguments();
        filePath = receivedBundle.getString("File_path");
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

    void openPdf() {

        File pdfFile = new File(filePath);

        if (pdfFile.exists()) {
            pdfView.fromFile(pdfFile)
                    .load();
            setCurrentFileState(PDFFileState.ALL_PAGES);
        }
    }

    public void setCurrentFileState (PDFFileState newFileState) {

        if (mCurrentFileState != newFileState) {

            mCurrentFileState = newFileState;

            switch (newFileState) {

                case ALL_PAGES:
                    pdfView.enableSwipe(true);
                    break;
                case SINGLE_PAGE:
                    pdfView.enableSwipe(false);
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
}
