package com.clarksoft.max;

import com.google.android.material.textfield.TextInputLayout;
import java.util.regex.Pattern;

class InputValidation {

    static final Pattern PWD_PATTERN =  Pattern.compile("^" +
            "(?=.*[0-9])" +         //at least 1 digit
            "(?=.*[a-z])" +         //at least 1 lower case letter
            "(?=.*[A-Z])" +         //at least 1 upper case letter
            "(?=.*[!@#$%^&*()_+={}<>.,?/|';:`~\\[\\]-])" +    //at least 1 special character
            "(?=\\S+$)" +           //no white spaces
            ".{8,}" +               //at least 8 characters
            "$");
    static final Pattern AGE_PATTERN = Pattern.compile("^(0?[1-9]|[1-9][0-9])$");


    Boolean matchPattern(String input, Pattern pattern){
        return pattern.matcher(input).matches();
    }

    void updateFieldError(Boolean ok, TextInputLayout view, String msg) {
        if(ok) {
            view.setError(null);
        }
        else {
            view.setError(msg);
        }
    }

    Boolean comparePwd(String pwd1, String pwd2) {
        return pwd1.equals(pwd2);
    }
}