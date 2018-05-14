package orion.garon.pdfreader;

/**
 * Created by VKI on 17.04.2018.
 */

public enum PDFFileState {
    ALL_PAGES,
    SINGLE_PAGE;

    public static PDFFileState stateFromInt(int value) {

        PDFFileState resultState;

        switch (value) {

            case 0:
                resultState = ALL_PAGES;
                break;
            case 1:
                resultState = SINGLE_PAGE;
                break;
            default:
                resultState = ALL_PAGES;
                break;
        }

        return resultState;
    }

    public static int intFromState(PDFFileState state) {

        int result;

        switch (state) {

            case ALL_PAGES:
                result = 0;
                break;
            case SINGLE_PAGE:
                result = 1;
                break;
            default:
                result = 0;
                break;
        }

        return result;
    }
}
