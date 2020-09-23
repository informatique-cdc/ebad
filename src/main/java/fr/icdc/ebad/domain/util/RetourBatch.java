package fr.icdc.ebad.domain.util;

import lombok.Data;

/**
 * Created by dtrouillet on 24/02/2016.
 */
@Data
public class RetourBatch {

    private String logOut;
    private int returnCode;
    private Long executionTime;

    public RetourBatch(){
    }

    public RetourBatch(String pLogOut, int pReturnCode, Long pExecutionTime){
        logOut = pLogOut;
        returnCode = pReturnCode;
        executionTime = pExecutionTime;
    }
}
