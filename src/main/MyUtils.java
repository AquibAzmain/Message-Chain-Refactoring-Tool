package main;

import java.util.ArrayList;
import java.util.List;

public class MyUtils {

    public static String[] splitScope(String scope){
        List<String> scopeList = new ArrayList<>();

        int openBracket = 0;
        int latestSplitIndex = 0;
        for(int i=0; i<scope.length(); i++){
            char c = scope.charAt(i);

            if(c == '(') openBracket++;
            else if(c == ')') openBracket--;
            else if(c == '.' && openBracket==0){
                scopeList.add(scope.substring(latestSplitIndex, i));
                latestSplitIndex = i+1;
            }

            if(i==scope.length()-1){
                scopeList.add(scope.substring(latestSplitIndex, i+1));
            }
        }

        System.out.println("> " + scopeList);
        String[] scopes = new String[scopeList.size()];
        return scopeList.toArray(scopes);
    }
}
