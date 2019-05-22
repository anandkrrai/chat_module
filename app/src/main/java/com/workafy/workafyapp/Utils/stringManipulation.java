package com.workafy.workafyapp.Utils;

public class stringManipulation {

    public static String compress(String input){
        String rv;
        rv=input.replace(' ','_');
        rv=input.replace('.','-');
        return rv;
    }


    public static String expand(String input){
        String rv;
        rv=input.replace('_',' ');
        return rv;
    }




}
