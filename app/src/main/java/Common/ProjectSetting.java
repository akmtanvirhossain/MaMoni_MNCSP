package Common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TanvirHossain on 19/07/2016.
 */
public class ProjectSetting {
    public static String ProjectName    = "mamonimncsp";
    public static String Namespace      = "http://chu.icddrb.org/";

    public static String apiName        = ProjectName.toLowerCase();
    public static String NewVersionName = ProjectName.toLowerCase() +"_update";
    public static String DatabaseFolder = ProjectName.toUpperCase() +"DB";
    public static String DatabaseName   = ProjectName.toUpperCase() +"Database.db";
    public static String zipDatabaseName= ProjectName.toUpperCase() +"Database.zip";
    public static String Organization   = "ICDDR,B";

    public static String VersionDate    = "21082016"; //Format: DDMMYYYY


    //Data Sync: Background Service
    //----------------------------------------------------------------------------------------------
    public static List<String> TableList_Upload(){
        List<String> tableList_Upload   = new ArrayList<String>();
        //tableList_Upload.add("");
        return tableList_Upload;
    }

    /*public static List<String> TableList_Download(){
        List<String> tableList_Download   = new ArrayList<String>();
        tableList_Download.add("");
        return tableList_Download;
    }*/

}
