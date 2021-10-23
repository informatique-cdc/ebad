package fr.icdc.ebad.service.util;


public class EbadNotFoundException extends RuntimeException {
    public EbadNotFoundException(){

    }

    public EbadNotFoundException(String message){
        super(message);
    }

    public EbadNotFoundException(String message, Throwable throwable){
        super(message,throwable);
    }

    public EbadNotFoundException(Throwable throwable){
        super(throwable);
    }
}
