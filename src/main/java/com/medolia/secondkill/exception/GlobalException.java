package com.medolia.secondkill.exception;

import com.medolia.secondkill.result.CodeMsg;

public class GlobalException extends Exception {

    private static final long serialVersionUID = 1L;

    private CodeMsg cm;

    public GlobalException(CodeMsg cm) {
        super(cm.toString());
        this.cm = cm;
    }

    public CodeMsg getCm() {
        return cm;
    }
}
