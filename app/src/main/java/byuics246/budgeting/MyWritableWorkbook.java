package byuics246.budgeting;

import java.io.File;
import java.lang.Boolean;

import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.*;
import jxl.write.Number;

public class MyWritableWorkbook {
    String filePath = "";
    String file_name_inp = "ExcelTable2-4.xls";
    String file_name_out = "ExcelTable2-4.xls";
    File inp;
    File out;
    public WritableWorkbook wb;
    private static final String TAG = "Creating";

    public MyWritableWorkbook(){}
    public MyWritableWorkbook(WritableWorkbook workbook){wb = workbook;}
    boolean checkPermissions(){return true;}
    void requestPermissions(){}
    boolean createDirectory(){return true;}
    boolean downloadTemplete(){return true;}
    boolean createWorkbook(){return true;}
    boolean createSheet(){return true;}
    boolean updateCell(int row, int column, String value){return true;}


}
