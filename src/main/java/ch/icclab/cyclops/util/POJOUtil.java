package ch.icclab.cyclops.util;

import java.util.ArrayList;

/**
 * Created by Konstantin on 19.10.2015.
 */
public class POJOUtil {

    public ArrayList<ArrayList<Object>> populateList(ArrayList objList, Object... objs){
        ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();
        for (int i = 0; i < objList.size(); i++) {//resourceUsageStr.get(usage).size()
            ArrayList<Object> objArrNode = new ArrayList<Object>();
            for(Object obj : objs){
                objArrNode.add(obj);
            }
            objArr.add(objArrNode);
        }
        return objArr;
    }

    public ArrayList<ArrayList<Object>> populateList(ArrayList objList, ArrayList objArr, Object... objs){
        //ArrayList<ArrayList<Object>> objArr = new ArrayList<ArrayList<Object>>();
        for (int i = 0; i < objList.size(); i++) {//resourceUsageStr.get(usage).size()
            ArrayList<Object> objArrNode = new ArrayList<Object>();
            for(Object obj : objs){
                objArrNode.add(obj);
            }
            objArr.add(objArrNode);
        }
        return objArr;
    }
}
