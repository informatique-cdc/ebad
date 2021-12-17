package fr.icdc.ebad.domain.util;

import lombok.Data;

/**
 * Created by dtrouillet on 24/02/2016.
 */
@Data
public class RetourBatch {

    private String logOut;
    private String logErr;
    private int returnCode;
    private Long executionTime;

    public RetourBatch(){
    }

    public RetourBatch(String pLogOut,String pLogErr, int pReturnCode, Long pExecutionTime){
        logOut = pLogOut;
        logErr = pLogErr;
        returnCode = pReturnCode;
        executionTime = pExecutionTime;
    }
}
