package it.iziozi.iziozi.core.pdfcreator;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import it.iziozi.iziozi.R;
import it.iziozi.iziozi.core.IOApplication;
import it.iziozi.iziozi.core.IOBoard;
import it.iziozi.iziozi.core.IOLevel;
import it.iziozi.iziozi.core.IOSpeakableImageButton;
import it.iziozi.iziozi.helpers.IOHelper;

/**
 * Created by Daniel on 12/11/2015.
 */
public class PdfCreatorTask extends AsyncTask<IOLevel, Void, Integer> {

    private static final String TAG = "IziOzi";
    Context context;

    int pageNum;

    // A4 at about 100 dpi
    private static final int PDF_WIDTH = 894;
    private static final int PDF_HEIGHT = 1260;

    private static final String PDF_NAME = "page.pdf";

    IOHelper.Orientation orientation;

    private ImageLoader imageLoader = ImageLoader.getInstance();

    PdfDocument document;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public PdfCreatorTask(Context context, IOHelper.Orientation orientation) {
        this.context = context;
        pageNum = 0;
        this.orientation = orientation;
        document = new PdfDocument();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    @TargetApi(Build.VERSION_CODES.KITKAT)
    protected Integer doInBackground(IOLevel... level) {

        for (int i = 0; i < level[0].getInnerBoards().size(); i++) {
            IOBoard board = level[0].getBoardAtIndex(i);
            dfs(board);
        }

        OutputStream os;
        File file;

        try {
            file = new File(Environment.getExternalStorageDirectory() + "/" + IOApplication.APPLICATION_FOLDER, PDF_NAME);
            os = new BufferedOutputStream(new FileOutputStream(file));
            document.writeTo(os);
            os.close();

            return 0;

        } catch (IOException e) {
            Log.d(TAG, e.toString());

            return 1;
        }
    }

    @Override
    protected void onPostExecute(Integer returnCode) {
        if (returnCode != 0) {
            Toast.makeText(context, context.getString(R.string.pdf_create_error), Toast.LENGTH_LONG)
                    .show();
        }
        else Toast.makeText(context, context.getString(R.string.pdf_created), Toast.LENGTH_LONG).show();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private PdfDocument.Page initNewPdfPage() {
        PdfDocument.PageInfo pageInfo;

        if (orientation.equals(IOHelper.Orientation.HORIZONTAL)) {
            pageInfo = new PdfDocument.PageInfo.Builder(PDF_HEIGHT, PDF_WIDTH, ++pageNum).create();
        }
        else {
            pageInfo = new PdfDocument.PageInfo.Builder(PDF_WIDTH, PDF_HEIGHT, ++pageNum).create();
        }

        return document.startPage(pageInfo);
    }

    private void dfs(IOBoard board) {
        // first check if they are all empty pictograms on a board and short circuit
        boolean foundImg = false;
        for (int i = 0; i < board.getRows(); i++) {
            if (foundImg) break;

            for (int j = 0; j < board.getCols(); j++) {
                int index = i * board.getCols() + j;

                IOSpeakableImageButton img = board.getButtons().get(index);
                if (!img.getmImageFile().equals("")) {
                    foundImg = true;
                    break;
                }
            }
        }

        if (foundImg) {
            PdfDocument.Page page = initNewPdfPage();
            createPdfPage(board, page);
        }

        // now dfs for every child tree board
        for (int i=0; i < board.getButtons().size(); i++) {
            IOSpeakableImageButton btn = board.getButtons().get(i);
            if (btn.getIsMatrioska() && btn.getLevel() != null) {
                // it's a nested board
                IOLevel level = btn.getLevel();

                for (int j = 0; j < level.getInnerBoards().size(); j++) {
                    IOBoard innerBoard = level.getBoardAtIndex(j);
                    dfs(innerBoard);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void createPdfPage(IOBoard board, PdfDocument.Page page) {
        int pageWidth = page.getInfo().getPageWidth();
        int pageHeight = page.getInfo().getPageHeight();

        int sizeWidth = pageWidth / board.getCols();
        int sizeHeight = pageHeight / board.getRows();

        int picSize = sizeWidth < sizeHeight ? sizeWidth : sizeHeight;

        int marginLeft = (pageWidth - (picSize * board.getCols())) / (board.getCols() + 1);
        int marginTop = (pageHeight - (picSize * board.getRows())) / (board.getRows() + 1);

        for (int i = 0; i < board.getRows(); i++) {
            int top = (marginTop * (i+1)) + (picSize * i);

            for (int j=0; j < board.getCols(); j++) {
                int index = i * board.getCols() + j;
                IOSpeakableImageButton btn = board.getButtons().get(index);

                ImageSize size = new ImageSize(picSize, picSize);
                Bitmap b = null;

                if (!btn.getmImageFile().equals("")) {
                    DisplayImageOptions imgOpts = new DisplayImageOptions.Builder()
                            .imageScaleType(ImageScaleType.EXACTLY)
                            .build();

                    b = imageLoader.loadImageSync("file://" + btn.getmImageFile(), size, imgOpts);
                }

                if (b != null) {
                    int left = (marginLeft * (j+1)) + (picSize * j);

                    page.getCanvas().drawBitmap(b, left, top, null);
                }
            }
        }

        document.finishPage(page);
    }
}
