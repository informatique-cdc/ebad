package fr.icdc.ebad.service.util;

/**
 * Created by DTROUILLET on 22/03/2018.
 */
public class EbadServiceException extends Exception {
    public EbadServiceException(){

    }

    public EbadServiceException(String message){
        super(message);
    }

    public EbadServiceException(String message, Throwable throwable){
        super(message,throwable);
    }

    public EbadServiceException(Throwable throwable){
        super(throwable);
    }
}
