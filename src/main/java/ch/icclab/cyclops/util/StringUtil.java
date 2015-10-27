package ch.icclab.cyclops.util;

import java.util.ArrayList;

/**
 * Created by Konstantin on 07.10.2015.
 */
public class StringUtil {

    public static ArrayList<String> strArr(String... strings){
        ArrayList<String> result = new ArrayList<String>();
        for(String string : strings){
            result.add(string);
        }
        return result;
    }
}
